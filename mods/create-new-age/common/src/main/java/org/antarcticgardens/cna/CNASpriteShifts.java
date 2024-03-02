package org.antarcticgardens.cna;

import com.simibubi.create.foundation.block.connected.AllCTTypes;
import com.simibubi.create.foundation.block.connected.CTSpriteShiftEntry;
import com.simibubi.create.foundation.block.connected.CTSpriteShifter;
import com.simibubi.create.foundation.block.connected.CTType;
import net.minecraft.resources.ResourceLocation;

public class CNASpriteShifts {
    public static CTSpriteShiftEntry REACTOR_CASING = omni("reactor_casing");
    public static CTSpriteShiftEntry REACTOR_GLASS = omni("reactor_glass");
    public static CTSpriteShiftEntry REDSTONE_MAGNET = omni("redstone_magnet");

    public static CTSpriteShiftEntry BATTERY_TOP = rect("battery_top");
    public static CTSpriteShiftEntry BATTERY_SIDE = rect("battery_side");

    private static CTSpriteShiftEntry omni(String name) {
        return getCT(AllCTTypes.OMNIDIRECTIONAL, name);
    }

    private static CTSpriteShiftEntry rect(String name) {
        return getCT(AllCTTypes.RECTANGLE, name);
    }

    private static CTSpriteShiftEntry getCT(CTType type, String name) {
        return CTSpriteShifter.getCT(type, new ResourceLocation(CreateNewAge.MOD_ID, "block/" + name),
                new ResourceLocation(CreateNewAge.MOD_ID, "block/" + name + "_connected"));
    }
}
