package org.antarcticgardens.newage.content.electricity.wire;

public enum WireType {
    COPPER(1024, new int[] { 158, 88, 75, 255 }, new int[] { 173, 108, 92, 255 });

    private final long conductivity;
    private final int[] color1;
    private final int[] color2;

    WireType(int conductivity, int[] color1, int[] color2) {
        this.conductivity = conductivity;
        this.color1 = color1;
        this.color2 = color2;
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
}
