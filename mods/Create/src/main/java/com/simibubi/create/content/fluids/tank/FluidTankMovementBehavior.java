package com.simibubi.create.content.fluids.tank;

import com.simibubi.create.AllPackets;
import com.simibubi.create.api.contraption.storage.fluid.MountedFluidStorage;
import com.simibubi.create.content.contraptions.AbstractContraptionEntity;
import com.simibubi.create.content.contraptions.behaviour.MovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.content.contraptions.sync.ContraptionFluidPacket;
import com.simibubi.create.content.fluids.tank.storage.FluidTankMountedStorage;

import net.createmod.catnip.utility.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.network.PacketDistributor;

// the contents of the mounted storage need to be synced to the block entity for rendering
public class FluidTankMovementBehavior implements MovementBehaviour {
	@Override
	public boolean mustTickWhileDisabled() {
		return true;
	}

	@Override
	public void tick(MovementContext context) {
		if (!context.world.isClientSide) {
			MountedFluidStorage storage = context.getFluidStorage();
			if (!(storage instanceof FluidTankMountedStorage tank))
				return;

			FluidStack fluid = tank.getTank().getFluid();
			if (!(context.temporaryData instanceof SyncState))
				context.temporaryData = new SyncState();

			SyncState state = (SyncState) context.temporaryData;
			if (state.cooldown > 0) {
				state.cooldown--;
			} else if (!fluid.isFluidStackIdentical(state.stack)) {
				// fluid has changed, sync it
				state.cooldown = 8;
				state.stack = fluid.copy();

				AbstractContraptionEntity entity = context.contraption.entity;
				ContraptionFluidPacket packet = new ContraptionFluidPacket(entity.getId(), context.localPos, fluid);
				AllPackets.getChannel().send(PacketDistributor.TRACKING_ENTITY.with(() -> entity), packet);
			}
		} else {
			BlockEntity be = context.contraption.presentBlockEntities.get(context.localPos);
			if (be instanceof FluidTankBlockEntity tank) {
				tank.getFluidLevel().tickChaser();
			}
		}
	}

	public static void handlePacket(AbstractContraptionEntity entity, BlockPos localPos, FluidStack fluid) {
		BlockEntity be = entity.getContraption().presentBlockEntities.get(localPos);
		if (!(be instanceof FluidTankBlockEntity tank))
			return;

		FluidTank inv = tank.getTankInventory();
		inv.setFluid(fluid);
		float fillLevel = inv.getFluidAmount() / (float) inv.getCapacity();
		if (tank.getFluidLevel() == null) {
			tank.setFluidLevel(LerpedFloat.linear().startWithValue(fillLevel));
		}
		tank.getFluidLevel().chase(fillLevel, 0.5, LerpedFloat.Chaser.EXP);
	}

	private static final class SyncState {
		private int cooldown = 0;
		private FluidStack stack = FluidStack.EMPTY;
	}
}
