package dev.eriksonn.aeronautics.content.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;
import dev.eriksonn.aeronautics.index.AeroParticleTypes;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class LevititeSparklePartcleData implements ParticleOptions, ICustomParticleDataWithSprite<LevititeSparklePartcleData> {
    public static final int LEVITITE_GREEN = 9424022;
    public static final int LEVITITE_PINK = 15521489;

    public static final MapCodec<LevititeSparklePartcleData> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.INT.fieldOf("color").forGetter(p -> p.color)
            ).apply(instance, LevititeSparklePartcleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LevititeSparklePartcleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, p -> p.color,
            LevititeSparklePartcleData::new
    );

    public final int color;

    public LevititeSparklePartcleData(final int color) {
        this.color = color;
    }

    public LevititeSparklePartcleData() {
        this(LEVITITE_GREEN);
    }

    @Override
    public ParticleEngine.SpriteParticleRegistration<LevititeSparklePartcleData> getMetaFactory() {
        return LevititeSparklePartcle.Factory::new;
    }

    @Override
    public MapCodec<LevititeSparklePartcleData> getCodec(final ParticleType<LevititeSparklePartcleData> type) {
        return CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, LevititeSparklePartcleData> getStreamCodec() {
        return STREAM_CODEC;
    }

    @Override
    public ParticleType<?> getType() {
        return AeroParticleTypes.LEVITITE_SPARKLE.get();
    }
}
