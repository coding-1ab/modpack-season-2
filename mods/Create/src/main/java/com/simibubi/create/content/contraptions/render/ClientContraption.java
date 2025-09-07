package com.simibubi.create.content.contraptions.render;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.content.contraptions.Contraption;
import com.simibubi.create.content.contraptions.Contraption.RenderedBlocks;
import com.simibubi.create.foundation.virtualWorld.VirtualRenderWorld;

import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.createmod.catnip.render.ShadedBlockSbbBuilder;
import net.createmod.catnip.render.SuperByteBuffer;
import net.createmod.catnip.render.SuperByteBufferCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;

import net.minecraftforge.client.model.data.ModelData;

public class ClientContraption {
	public static final SuperByteBufferCache.Compartment<Pair<Contraption, RenderType>> CONTRAPTION = new SuperByteBufferCache.Compartment<>();
	private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

	// TODO: block updates submitted to the level should automatically invalidate the client data
	private final VirtualRenderWorld renderLevel;
	/**
	 * The block entities that should be rendered.
	 * This will exclude e.g. drills and deployers which are rendered in contraptions as actors.
	 * All block entities are created with {@link #renderLevel} as their level.
	 */
	private final ImmutableList<BlockEntity> renderedBlockEntities;
	private final ContraptionMatrices matrices = new ContraptionMatrices();
	private final Contraption contraption;

	public ClientContraption(Contraption contraption) {
		var level = contraption.entity.level();
		this.contraption = contraption;

		BlockPos origin = contraption.anchor;
		int minY = VirtualRenderWorld.nextMultipleOf16(Mth.floor(contraption.bounds.minY - 1));
		int height = VirtualRenderWorld.nextMultipleOf16(Mth.ceil(contraption.bounds.maxY + 1)) - minY;
		renderLevel = new VirtualRenderWorld(level, minY, height, origin) {
			@Override
			public boolean supportsVisualization() {
				return VisualizationManager.supportsVisualization(level);
			}
		};

		var renderedBlockEntities = new ImmutableList.Builder<BlockEntity>();

		for (StructureBlockInfo info : contraption.getBlocks().values()) {
			renderLevel.setBlock(info.pos(), info.state(), 0);

			BlockEntity blockEntity = contraption.readBlockEntity(renderLevel, info, contraption.getIsLegacy().getBoolean(info.pos()));

			if (blockEntity != null) {
				renderLevel.setBlockEntity(blockEntity);

				// Don't render block entities that have an actor renderer registered in the MovementBehaviour.
				MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(info.state());
				if (movementBehaviour == null || !movementBehaviour.disableBlockEntityRendering()) {
					renderedBlockEntities.add(blockEntity);
				}
			}
		}

		this.renderedBlockEntities = renderedBlockEntities.build();

		renderLevel.runLightEngine();
	}

	public VirtualRenderWorld getRenderLevel() {
		return renderLevel;
	}

	public ContraptionMatrices getMatrices() {
		return matrices;
	}

	public RenderedBlocks getRenderedBlocks() {
		return new RenderedBlocks(pos -> {
			StructureBlockInfo info = contraption.getBlocks().get(pos);
			if (info == null) {
				return Blocks.AIR.defaultBlockState();
			}
			return info.state();
		}, contraption.getBlocks().keySet());
	}

	/**
	 * Get the model data for a block in the contraption's render world.
	 * @param pos The local position of the block.
	 * @return The model data for the block, or {@link ModelData#EMPTY} if there is no block entity at the position.
	 */
	public ModelData getModelData(BlockPos pos) {
		var blockEntity = renderLevel.getBlockEntity(pos);
		if (blockEntity != null) {
			return blockEntity.getModelData();
		}
		return ModelData.EMPTY;
	}

	@Nullable
	public BlockEntity getBlockEntity(BlockPos localPos) {
		return renderLevel.getBlockEntity(localPos);
	}

	@Unmodifiable
	public List<BlockEntity> renderedBlockEntities() {
		return renderedBlockEntities;
	}

	public static SuperByteBuffer getBuffer(Contraption contraption, VirtualRenderWorld renderWorld, RenderType renderType) {
		return SuperByteBufferCache.getInstance().get(CONTRAPTION, Pair.of(contraption, renderType), () -> buildStructureBuffer(contraption, renderWorld, renderType));
	}

	public static void invalidate(Contraption contraption) {
		for (RenderType renderType : RenderType.chunkBufferLayers()) {
			SuperByteBufferCache.getInstance().invalidate(CONTRAPTION, Pair.of(contraption, renderType));
		}
	}

	private static SuperByteBuffer buildStructureBuffer(Contraption contraption, VirtualRenderWorld renderWorld, RenderType layer) {
		BlockRenderDispatcher dispatcher = Minecraft.getInstance().getBlockRenderer();
		ModelBlockRenderer renderer = dispatcher.getModelRenderer();
		ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();

		PoseStack poseStack = objects.poseStack;
		RandomSource random = objects.random;
		var clientContraption = contraption.getClientSideData();
		RenderedBlocks blocks = clientContraption.getRenderedBlocks();

		ShadedBlockSbbBuilder sbbBuilder = objects.sbbBuilder;
		sbbBuilder.begin();

		ModelBlockRenderer.enableCaching();
		for (BlockPos pos : blocks.positions()) {
			BlockState state = blocks.lookup().apply(pos);
			if (state.getRenderShape() == RenderShape.MODEL) {
				BakedModel model = dispatcher.getBlockModel(state);
				ModelData modelData = clientContraption.getModelData(pos);
				modelData = model.getModelData(renderWorld, pos, state, modelData);
				long randomSeed = state.getSeed(pos);
				random.setSeed(randomSeed);
				if (model.getRenderTypes(state, random, modelData).contains(layer)) {
					poseStack.pushPose();
					poseStack.translate(pos.getX(), pos.getY(), pos.getZ());
					renderer.tesselateBlock(renderWorld, model, state, pos, poseStack, sbbBuilder, true, random, randomSeed, OverlayTexture.NO_OVERLAY, modelData, layer);
					poseStack.popPose();
				}
			}
		}
		ModelBlockRenderer.clearCache();

		return sbbBuilder.end();
	}

	private static class ThreadLocalObjects {
		public final PoseStack poseStack = new PoseStack();
		public final RandomSource random = RandomSource.createNewThreadLocalInstance();
		public final ShadedBlockSbbBuilder sbbBuilder = ShadedBlockSbbBuilder.create();
	}
}
