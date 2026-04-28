package dev.propulsionteam.propulsionsimulated.particles.ion;

import java.util.ArrayList;
import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.foundation.particle.ICustomParticleDataWithSprite;

import net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import dev.propulsionteam.propulsionsimulated.particles.ParticleTypes;

public class IonParticleData implements ParticleOptions, ICustomParticleDataWithSprite<IonParticleData> {
    private final List<ResourceLocation> overrideTextures;
    private final Integer overrideColor;

    public IonParticleData() {
        this(List.of(), null);
    }

    public IonParticleData(List<ResourceLocation> overrideTextures, Integer overrideColor) {
        this.overrideTextures = overrideTextures == null ? List.of() : List.copyOf(overrideTextures);
        this.overrideColor = overrideColor;
    }

    public List<ResourceLocation> overrideTextures() {
        return overrideTextures;
    }

    public Integer overrideColor() {
        return overrideColor;
    }

    @Override
    public ParticleType<?> getType(){
        return ParticleTypes.getIonType();
    }

    public MapCodec<IonParticleData> getCodec(ParticleType<IonParticleData> type) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceLocation.CODEC.listOf().optionalFieldOf("override_textures", List.of()).forGetter(IonParticleData::overrideTextures),
            Codec.INT.optionalFieldOf("override_color").forGetter(data -> java.util.Optional.ofNullable(data.overrideColor))
        ).apply(instance, (textures, color) -> new IonParticleData(textures, color.orElse(null))));
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, IonParticleData> getStreamCodec() {
        return StreamCodec.of((buf, data) -> {
            buf.writeCollection(data.overrideTextures, (b, rl) -> b.writeResourceLocation(rl));
            buf.writeBoolean(data.overrideColor != null);
            if (data.overrideColor != null) {
                buf.writeInt(data.overrideColor);
            }
        }, buf -> {
            List<ResourceLocation> textures = buf.readCollection(ArrayList::new, b -> b.readResourceLocation());
            Integer color = buf.readBoolean() ? buf.readInt() : null;
            return new IonParticleData(textures, color);
        });
    }

    @Override
    public SpriteParticleRegistration<IonParticleData> getMetaFactory() {
        return IonParticle.Factory::new;
    }
}
