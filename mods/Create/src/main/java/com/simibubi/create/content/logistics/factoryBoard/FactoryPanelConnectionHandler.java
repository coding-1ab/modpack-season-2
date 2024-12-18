package com.simibubi.create.content.logistics.factoryBoard;

import javax.annotation.Nullable;

import com.simibubi.create.AllPackets;
import com.simibubi.create.content.logistics.factoryBoard.FactoryPanelBlock.PanelSlot;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkBlockEntity;
import com.simibubi.create.content.redstone.link.RedstoneLinkBlockEntity;
import com.simibubi.create.foundation.utility.CreateLang;

import net.createmod.catnip.CatnipClient;
import net.createmod.catnip.utility.AnimationTickHolder;
import net.createmod.catnip.utility.VecHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class FactoryPanelConnectionHandler {

	static FactoryPanelPosition connectingFrom;
	static AABB connectingFromBox;

	public static boolean panelClicked(LevelAccessor level, Player player, FactoryPanelBehaviour panel) {
		if (connectingFrom == null)
			return false;

		FactoryPanelBehaviour at = FactoryPanelBehaviour.at(level, connectingFrom);
		if (panel.getPanelPosition()
			.equals(connectingFrom) || at == null) {
			player.displayClientMessage(Component.empty(), true);
			connectingFrom = null;
			connectingFromBox = null;
			return true;
		}

		String checkForIssues = checkForIssues(at, panel);
		if (checkForIssues != null) {
			player.displayClientMessage(CreateLang.translate(checkForIssues)
				.style(ChatFormatting.RED)
				.component(), true);
			connectingFrom = null;
			connectingFromBox = null;
			return true;
		}

		ItemStack filterFrom = panel.getFilter();
		ItemStack filterTo = at.getFilter();

		AllPackets.getChannel()
			.sendToServer(new FactoryPanelConnectionPacket(panel.getPanelPosition(), connectingFrom));

		player.displayClientMessage(CreateLang.translate("factory_panel.panels_connected", filterFrom.getHoverName()
			.getString(),
			filterTo.getHoverName()
				.getString())
			.style(ChatFormatting.GREEN)
			.component(), true);

		connectingFrom = null;
		connectingFromBox = null;
		return true;
	}

	@Nullable
	private static String checkForIssues(FactoryPanelBehaviour from, FactoryPanelBehaviour to) {
		BlockState state1 = to.blockEntity.getBlockState();
		BlockState state2 = from.blockEntity.getBlockState();
		BlockPos diff = to.getPos()
			.subtract(from.getPos());

		if (state1.setValue(FactoryPanelBlock.WATERLOGGED, false)
			.setValue(FactoryPanelBlock.POWERED, false) != state2.setValue(FactoryPanelBlock.WATERLOGGED, false)
				.setValue(FactoryPanelBlock.POWERED, false))
			return "factory_panel.same_orientation";

		if (FactoryPanelBlock.connectedDirection(state1)
			.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return "factory_panel.same_surface";

		if (!diff.closerThan(BlockPos.ZERO, 16))
			return "factory_panel.too_far_apart";

		if (to.panelBE().restocker)
			return "factory_panel.input_in_restock_mode";

		if (to.getFilter()
			.isEmpty()
			|| from.getFilter()
				.isEmpty())
			return "factory_panel.no_item";

		return null;
	}

	public static void clientTick() {
		if (connectingFrom == null || connectingFromBox == null)
			return;

		Minecraft mc = Minecraft.getInstance();
		FactoryPanelBehaviour at = FactoryPanelBehaviour.at(mc.level, connectingFrom);

		if (!connectingFrom.pos()
			.closerThan(mc.player.blockPosition(), 16) || at == null) {
			connectingFrom = null;
			connectingFromBox = null;
			mc.player.displayClientMessage(Component.empty(), true);
			return;
		}

		CatnipClient.OUTLINER.showAABB(connectingFrom, connectingFromBox)
			.colored(AnimationTickHolder.getTicks() % 16 > 8 ? 0x38b764 : 0xa7f070)
			.lineWidth(1 / 16f);

		mc.player.displayClientMessage(CreateLang.translate("factory_panel.click_second_panel")
			.component(), true);
	}

	public static boolean onRightClick() {
		if (connectingFrom == null || connectingFromBox == null)
			return false;
		Minecraft mc = Minecraft.getInstance();
		boolean missed = false;

		if (mc.hitResult instanceof BlockHitResult bhr && bhr.getType() != Type.MISS) {
			BlockEntity blockEntity = mc.level.getBlockEntity(bhr.getBlockPos());

			// Connecting redstone or display links
			if (blockEntity instanceof RedstoneLinkBlockEntity || blockEntity instanceof DisplayLinkBlockEntity) {
				FactoryPanelPosition bestPosition = null;
				double bestDistance = Double.POSITIVE_INFINITY;

				for (PanelSlot slot : PanelSlot.values()) {
					FactoryPanelPosition panelPosition = new FactoryPanelPosition(blockEntity.getBlockPos(), slot);
					FactoryPanelConnection connection = new FactoryPanelConnection(panelPosition, 1);
					Vec3 diff =
						connection.calculatePathDiff(mc.level.getBlockState(connectingFrom.pos()), connectingFrom);
					if (bestDistance < diff.lengthSqr())
						continue;
					bestDistance = diff.lengthSqr();
					bestPosition = panelPosition;
				}

				AllPackets.getChannel()
					.sendToServer(new FactoryPanelConnectionPacket(bestPosition, connectingFrom));

				mc.player.displayClientMessage(CreateLang.translate("factory_panel.link_connected")
					.style(ChatFormatting.GREEN)
					.component(), true);

				connectingFrom = null;
				connectingFromBox = null;
				return true;
			}

			if (!(blockEntity instanceof FactoryPanelBlockEntity))
				missed = true;
		}

		if (!mc.player.isShiftKeyDown() && !missed)
			return false;
		connectingFrom = null;
		connectingFromBox = null;
		mc.player.displayClientMessage(CreateLang.translate("factory_panel.connection_aborted")
			.component(), true);
		return true;
	}

	public static void startConnection(FactoryPanelBehaviour behaviour) {
		connectingFrom = behaviour.getPanelPosition();
		BlockState blockState = behaviour.blockEntity.getBlockState();
		Vec3 location = behaviour.getSlotPositioning()
			.getLocalOffset(behaviour.getWorld(), behaviour.getPos(), blockState)
			.add(Vec3.atLowerCornerOf(behaviour.getPos()));
		Vec3 plane = VecHelper.axisAlingedPlaneOf(FactoryPanelBlock.connectedDirection(blockState));
		connectingFromBox =
			new AABB(location, location).inflate(plane.x * 3 / 16f, plane.y * 3 / 16f, plane.z * 3 / 16f);
	}

}
