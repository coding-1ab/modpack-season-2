package org.antarcticgardens.cna.content.electricity.wire;

import net.minecraft.world.item.ItemStack;
import org.antarcticgardens.cna.CNAItems;
import org.antarcticgardens.cna.config.CNAConfig;

public enum WireType {
    COPPER(1024, new int[] { 158, 88, 75, 255 }, new int[] { 173, 108, 92, 255 }, CNAItems.COPPER_WIRE::asStack),
    IRON(2048, new int[] { 210, 213, 216, 255 }, new int[] { 253, 254, 254, 255 }, CNAItems.OVERCHARGED_IRON_WIRE::asStack),
    GOLD(4096, new int[] { 244, 184, 28, 255 }, new int[] { 254, 240, 90, 255 }, CNAItems.OVERCHARGED_GOLDEN_WIRE::asStack),
    DIAMOND(8192, new int[] { 45, 196, 178, 255 }, new int[] { 107, 243, 227, 255 }, CNAItems.OVERCHARGED_DIAMOND_WIRE::asStack);

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
        return (long) (conductivity * CNAConfig.getCommon().conductivityMultiplier.get());
    }

    public int[] getColor1() {
        return color1.clone();
    }

    public int[] getColor2() {
        return color2.clone();
    }

    public ItemStack getDroppedItem() {
        return dropProvider.getDroppedItem();
    }
}
