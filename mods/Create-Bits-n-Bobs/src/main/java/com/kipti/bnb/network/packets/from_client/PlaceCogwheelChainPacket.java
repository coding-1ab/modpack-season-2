package com.kipti.bnb.network.packets.from_client;

import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChain;
import com.kipti.bnb.content.cogwheel_chain.graph.CogwheelChainPathfinder;
import com.kipti.bnb.content.cogwheel_chain.graph.PathedCogwheelNode;
import com.kipti.bnb.content.cogwheel_chain.graph.PlacingCogwheelChain;
import com.kipti.bnb.network.BnbPackets;
import net.createmod.catnip.net.base.ServerboundPacketPayload;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;

public record PlaceCogwheelChainPacket(
        PlacingCogwheelChain worldSpacePartialChain,
        int priorityChainTakeHand
) implements ServerboundPacketPayload {

    public static final StreamCodec<RegistryFriendlyByteBuf, PlaceCogwheelChainPacket> STREAM_CODEC =
            StreamCodec.composite(
                    PlacingCogwheelChain.STREAM_CODEC,
                    PlaceCogwheelChainPacket::worldSpacePartialChain,
                    ByteBufCodecs.INT,
                    PlaceCogwheelChainPacket::priorityChainTakeHand,
                    PlaceCogwheelChainPacket::new
            );

    @Override
    public void handle(ServerPlayer player) {
        //Server side validation of the chain
        if (worldSpacePartialChain.maxBounds() > PlacingCogwheelChain.MAX_CHAIN_BOUNDS)
            return;

        if (!worldSpacePartialChain.checkMatchingNodesInLevel(player.level()))
            return;

        //TODO: check item cost and take the chains as necessary

        final List<PathedCogwheelNode> chainGeometry;
        try {
            chainGeometry = CogwheelChainPathfinder.buildChainPath(worldSpacePartialChain);
        } catch (final CogwheelChain.InvalidGeometryException ignored) {
            return;
        }
        if (chainGeometry == null)
            return;

        final CogwheelChain chain = new CogwheelChain(chainGeometry);

        chain.placeInLevel(player.level(), worldSpacePartialChain);
    }

    @Override
    public PacketTypeProvider getTypeProvider() {
        return BnbPackets.PLACE_COGWHEEL_CHAIN;
    }

}
