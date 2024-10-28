package com.simibubi.create.content.logistics.factoryBoard;

import javax.annotation.Nullable;

import com.simibubi.create.AllPackets;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
			player.displayClientMessage(CreateLang.temporaryText(checkForIssues)
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

		player.displayClientMessage(CreateLang.temporaryText("Now using " + filterFrom.getHoverName()
			.getString() + " to create "
			+ filterTo.getHoverName()
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
			return "Panels must have the same orientation";

		if (FactoryPanelBlock.connectedDirection(state1)
			.getAxis()
			.choose(diff.getX(), diff.getY(), diff.getZ()) != 0)
			return "Panels must be on the same surface";

		if (!diff.closerThan(BlockPos.ZERO, 16))
			return "Panels are too far away from each other";

		if (to.panelBE().restocker)
			return "Input panel cannot be in restock mode";

		if (to.getFilter()
			.isEmpty()
			|| from.getFilter()
				.isEmpty())
			return "Input panel must have an item first";

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

		mc.player.displayClientMessage(CreateLang.temporaryText("Click a second panel to connect...")
			.component(), true);
	}

	public static boolean onRightClick() {
		if (connectingFrom == null || connectingFromBox == null)
			return false;
		Minecraft mc = Minecraft.getInstance();
		if (!mc.player.isShiftKeyDown())
			return false;
		connectingFrom = null;
		connectingFromBox = null;
		mc.player.displayClientMessage(CreateLang.temporaryText("Input connection aborted")
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
