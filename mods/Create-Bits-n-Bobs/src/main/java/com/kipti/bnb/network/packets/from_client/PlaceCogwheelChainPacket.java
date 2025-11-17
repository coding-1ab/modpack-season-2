package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.cogwheel_chain.graph.ChainPathCogwheelNode;
import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.cogwheel_chain.graph.PartialCogwheelChain;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record PlaceCogwheelChainPacket(
        PartialCogwheelChain partialChain,
        int priorityChainTakeHand
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceCogwheelChainPacket> STREAM_CODEC =
            StreamCodec.composite(
                    PartialCogwheelChain.STREAM_CODEC,
                    PlaceCogwheelChainPacket::partialChain,
                    ByteBufCodecs.INT,
                    PlaceCogwheelChainPacket::priorityChainTakeHand,
                    PlaceCogwheelChainPacket::new
            );

    @Override
    public void handle(ServerPlayer player) {
        //Server side validation of the chain
        if (partialChain.maxBounds() > PartialCogwheelChain.MAX_CHAIN_BOUNDS)
            return;

        if (!partialChain.checkMatchingNodesInLevel(player.level()))
            return;

        //TODO: check item cost and take the chains as necessary

        final Pair<List<CogwheelChainPathfinder.PathNode>, List<ChainPathCogwheelNode>> chainGeometry = CogwheelChainPathfinder.buildChainPath(partialChain);
        if (chainGeometry == null)
            return;

        final CogwheelChain chain = new CogwheelChain(chainGeometry);

        chain.placeInLevel(player.level(), partialChain);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.PLACE_COGWHEEL_CHAIN;
    }

}
