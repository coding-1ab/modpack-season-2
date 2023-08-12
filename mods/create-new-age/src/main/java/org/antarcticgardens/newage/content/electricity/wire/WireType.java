package org.antarcticgardens.newage.content.electricity.wire;

import net.minecraft.world.item.ItemStack;
import org.antarcticgardens.newage.NewAgeItems;

public enum WireType {
    COPPER(1024, new int[] { 158, 88, 75, 255 }, new int[] { 173, 108, 92, 255 }, NewAgeItems.COPPER_WIRE::asStack),
    GOLDEN(2048, new int[] { 244, 184, 28, 255 }, new int[] { 254, 240, 90, 255 }, NewAgeItems.GOLDEN_WIRE::asStack),
    DIAMOND(4096, new int[] { 45, 196, 178, 255 }, new int[] { 107, 243, 227, 255 }, NewAgeItems.DIAMOND_WIRE::asStack);

    private final long conductivity;
    private final int[] color1;
    private final int[] color2;
    private final IRegistrateIsAFuckingShitNeverUseIt dropProvider;

    WireType(int conductivity, int[] color1, int[] color2, IRegistrateIsAFuckingShitNeverUseIt dropProvider) {
        this.conductivity = conductivity;
        this.color1 = color1;
        this.color2 = color2;
        this.dropProvider = dropProvider;
    }

    public long getConductivity() {
        return conductivity;
    }

    public int[] getColor1() {
        return color1;
    }

    public int[] getColor2() {
        return color2;
    }

    public ItemStack getDroppedItem() {
        return dropProvider.getDroppedItem();
    }
}
