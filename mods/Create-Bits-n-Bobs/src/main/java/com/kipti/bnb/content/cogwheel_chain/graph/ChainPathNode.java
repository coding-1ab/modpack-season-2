package com.kipti.bnb.content.cogwheel_chain.graph;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record ChainPathNode(BlockPos relativePos, Vec3 nodeOffset) {

    public static final Codec<ChainPathNode> CODEC = RecordCodecBuilder.create(
        p_337946_ -> p_337946_.group(
                BlockPos.CODEC.fieldOf("relativePos").forGetter(ChainPathNode::relativePos),
                Vec3.CODEC.fieldOf("nodeEnter").forGetter(ChainPathNode::nodeOffset)
            )
            .apply(p_337946_, ChainPathNode::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, ChainPathNode> STREAM_CODEC = StreamCodec.composite(
        BlockPos.STREAM_CODEC,
        ChainPathNode::relativePos,
        CatnipStreamCodecs.VEC3,
        ChainPathNode::nodeOffset,
        ChainPathNode::new
    );

    public Vec3 getPosition() {
        return relativePos.getCenter().add(nodeOffset);
    }

    public void write(CompoundTag tag) {
        tag.putInt("X", relativePos.getX());
        tag.putInt("Y", relativePos.getY());
        tag.putInt("Z", relativePos.getZ());
        tag.putDouble("OffsetX", nodeOffset.x);
        tag.putDouble("OffsetY", nodeOffset.y);
        tag.putDouble("OffsetZ", nodeOffset.z);
    }

    public static ChainPathNode read(CompoundTag tag) {
        BlockPos pos = new BlockPos(tag.getInt("X"), tag.getInt("Y"), tag.getInt("Z"));
        Vec3 offset = new Vec3(tag.getDouble("OffsetX"), tag.getDouble("OffsetY"), tag.getDouble("OffsetZ"));
        return new ChainPathNode(pos, offset);
    }

}
