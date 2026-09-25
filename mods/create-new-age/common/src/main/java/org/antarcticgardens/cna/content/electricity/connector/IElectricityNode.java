package org.antarcticgardens.cna.content.electricity.connector;

import net.minecraft.core.BlockPos;
import org.antarcticgardens.cna.content.electricity.network.ElectricalNetwork;

public interface IElectricityNode {
    void setNetwork(ElectricalNetwork network);
    ElectricalNetwork getNetwork();
    BlockPos getBlockPos();
    boolean isValid();
    void onNearConnectionChanged();

    class FakeNode implements IElectricityNode {
        private final BlockPos blockPos;
        private ElectricalNetwork network;

        public FakeNode(boolean isSource) {
            if (isSource) {
                blockPos = new BlockPos(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
            } else {
                blockPos = new BlockPos(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            }
        }

        @Override
        public void setNetwork(ElectricalNetwork network) {
            this.network = network;
        }

        @Override
        public ElectricalNetwork getNetwork() {
            return network;
        }

        @Override
        public BlockPos getBlockPos() {
            return blockPos;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public void onNearConnectionChanged() {}
    }
}
