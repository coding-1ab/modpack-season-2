package dev.eriksonn.aeronautics.network.packets;

import dev.eriksonn.aeronautics.Aeronautics;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.CrystalPropagationContext;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.LevititeBlendHelper;
import dev.eriksonn.aeronautics.index.AeroLevititeBlendPropagationContexts;
import dev.eriksonn.aeronautics.index.AeroTags;
import foundry.veil.api.network.handler.ServerPacketContext;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public record LevititeCatalystCrystallizationPacket(BlockPos pos, InteractionHand hand, ItemStack item) implements CustomPacketPayload {
	public static final Type<LevititeCatalystCrystallizationPacket> TYPE = new Type<>(Aeronautics.path("levitite_blend_crystallize"));

	public static final StreamCodec<RegistryFriendlyByteBuf, LevititeCatalystCrystallizationPacket> STREAM_CODEC = StreamCodec.composite(
			BlockPos.STREAM_CODEC, LevititeCatalystCrystallizationPacket::pos,
			CatnipStreamCodecs.HAND, LevititeCatalystCrystallizationPacket::hand,
			ItemStack.STREAM_CODEC,LevititeCatalystCrystallizationPacket::item,
			LevititeCatalystCrystallizationPacket::new);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}

	public void handle(final ServerPacketContext context) {
		final ServerPlayer player = context.player();

		if (!this.item.is(AeroTags.ItemTags.LEVITITE_CATALYZER_NO_CONSUME)) {
			if (this.item.isDamageableItem()) {
				this.item.hurtAndBreak(1, player, this.hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
			} else if (this.item.isStackable() && !context.player().hasInfiniteMaterials()) {
				this.item.shrink(1);
			}
		}
		player.swing(this.hand);
		context.player().setItemInHand(this.hand, this.item);
		final CrystalPropagationContext itemContext = this.item.is(AeroTags.ItemTags.LEVITITE_SOUL_CATALYZER) ?
				AeroLevititeBlendPropagationContexts.SOUL_CONTEXT.get() :
				AeroLevititeBlendPropagationContexts.STANDARD_CONTEXT.get();
		//LevititeBlendHelper.crystallizeLevititeBlend(context.level(), this.pos, itemContext.getContextForSpread(context.level(), this.pos));
		LevititeBlendHelper.addLevititeBlendTicker(context.level(), this.pos, false, false, itemContext.getContextForSpread(context.level(), this.pos));
	}
}
