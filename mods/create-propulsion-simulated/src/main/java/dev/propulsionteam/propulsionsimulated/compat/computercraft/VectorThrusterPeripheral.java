package dev.propulsionteam.propulsionsimulated.compat.computercraft;

import com.simibubi.create.compat.computercraft.implementation.peripherals.SyncedPeripheral;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.lua.LuaFunction;
import dev.propulsionteam.propulsionsimulated.content.thruster.creative_vector_thruster.CreativeVectorThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterBlockEntity;
import net.minecraft.util.Mth;

public class VectorThrusterPeripheral extends SyncedPeripheral<VectorThrusterBlockEntity> {
    public VectorThrusterPeripheral(VectorThrusterBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public String getType() {
        return "vector_thruster";
    }

    // --- Vector control (float -1..1 coordinates) --------------------------

    @LuaFunction
    public final double getVectorX() {
        return blockEntity.getCurrentVectorX();
    }

    @LuaFunction
    public final double getVectorY() {
        return blockEntity.getCurrentVectorY();
    }

    @LuaFunction
    public final double getTargetVectorX() {
        return blockEntity.getTargetVectorX();
    }

    @LuaFunction
    public final double getTargetVectorY() {
        return blockEntity.getTargetVectorY();
    }

    @LuaFunction(mainThread = true)
    public final void setVectorX(double x) {
        blockEntity.setVectorCoordinates((float) Mth.clamp(x, -1.0, 1.0), blockEntity.getTargetVectorY());
    }

    @LuaFunction(mainThread = true)
    public final void setVectorY(double y) {
        blockEntity.setVectorCoordinates(blockEntity.getTargetVectorX(), (float) Mth.clamp(y, -1.0, 1.0));
    }

    @LuaFunction(mainThread = true)
    public final void setVector(double x, double y) {
        blockEntity.setVectorCoordinates((float) Mth.clamp(x, -1.0, 1.0), (float) Mth.clamp(y, -1.0, 1.0));
    }

    // --- Throttle ----------------------------------------------------------

    @LuaFunction(mainThread = true)
    public final void setThrust(int power) {
        blockEntity.setRedstonePower(Mth.clamp(power, 0, 15));
    }

    @LuaFunction
    public final int getThrust() {
        return blockEntity.getLegacyPowerInt();
    }

    @LuaFunction(mainThread = true)
    public final void setThrustOutput(double thrustOutputPn) throws LuaException {
        if (blockEntity instanceof CreativeVectorThrusterBlockEntity creativeVectorThruster) {
            creativeVectorThruster.setThrustOutput((float) Math.max(0.0d, thrustOutputPn));
            return;
        }
        throw new LuaException("setThrustOutput is only available on creative vector thrusters");
    }
}
