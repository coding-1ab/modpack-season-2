package com.kipti.bnb.content.cogwheel_chain.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PartialCogwheelChainNode(BlockPos pos, Direction.Axis rotationAxis, boolean isLarge) {

    public static final Codec<PartialCogwheelChainNode> CODEC = RecordCodecBuilder.create(
        p_337946_ -> p_337946_.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(PartialCogwheelChainNode::pos),
                Direction.Axis.CODEC.fieldOf("rotationAxis").forGetter(PartialCogwheelChainNode::rotationAxis),
                Codec.BOOL.fieldOf("isLarge").forGetter(PartialCogwheelChainNode::isLarge)
            )
            .apply(p_337946_, PartialCogwheelChainNode::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PartialCogwheelChainNode> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        PartialCogwheelChainNode::pos,
        CatnipStreamCodecs.AXIS,
        PartialCogwheelChainNode::rotationAxis,
        ByteBufCodecs.BOOL,
        PartialCogwheelChainNode::isLarge,
        PartialCogwheelChainNode::new
    );

}
