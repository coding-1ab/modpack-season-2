package org.antarcticgardens.newage.content.electricity.wire;

public enum WireType {
    COPPER(1024);

    private final long conductivity;

    WireType(int conductivity) {
        this.conductivity = conductivity;
    }

    public long getConductivity() {
        return conductivity;
    }
}
