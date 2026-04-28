package dev.propulsionteam.propulsionsimulated.particles.ion;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class IonParticle extends SimpleAnimatedParticle {
    
    private static final float BASE_QUAD_SIZE = 0.95f;
    private static final float END_QUAD_SIZE = 0.2f;
    private static final float SPEED_MULTIPLIER = 0.144f;
    private static final float FRICTION = 0.995f;
    private static final int BASE_LIFETIME = 20;
    private static final double FLUCTUATION = 0.0025d;

    private static final int SPRITE_COUNT = 9;
    private final SpriteSet spriteSet;
    private final float startSize;
    private final List<ResourceLocation> overrideTextures;

    protected IonParticle(ClientLevel level, double x, double y, double z, 
                            double dx, double dy, double dz, 
                            SpriteSet spriteSet, IonParticleData data) {
        super(level, x, y, z, spriteSet, 0);
        this.spriteSet = spriteSet;
        this.overrideTextures = data.overrideTextures();
        this.hasPhysics = false;
        this.friction = FRICTION;
        this.lifetime = BASE_LIFETIME;

        this.xd = dx * SPEED_MULTIPLIER;
        this.yd = dy * SPEED_MULTIPLIER;
        this.zd = dz * SPEED_MULTIPLIER;

        this.quadSize = BASE_QUAD_SIZE;
        this.startSize = this.quadSize;
        
        if (data.overrideColor() == null) {
            setColor(0xFFFFFF);
        } else {
            int rgb = data.overrideColor() & 0xFFFFFF;
            this.setColor(((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f);
        }
        this.setAlpha(1.0f);
        this.pickSpriteAndSize();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        this.move(this.xd, this.yd, this.zd);
        this.xd += (this.random.nextDouble() - 0.5d) * FLUCTUATION;
        this.yd += (this.random.nextDouble() - 0.5d) * FLUCTUATION;
        this.zd += (this.random.nextDouble() - 0.5d) * FLUCTUATION;
        this.xd *= this.friction;
        this.yd *= this.friction;
        this.zd *= this.friction;

        this.pickSpriteAndSize();
    }

    private void pickSpriteAndSize() {
        final float progress = Mth.clamp((float) this.age / (float) this.lifetime, 0.0f, 1.0f);
        final int frameIndex = Mth.clamp((int) (progress * SPRITE_COUNT), 0, SPRITE_COUNT - 1);
        
        if (this.overrideTextures != null && !this.overrideTextures.isEmpty()) {
            try {
                ResourceLocation texture = this.overrideTextures.get(frameIndex % this.overrideTextures.size());
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_PARTICLES).apply(texture);
                this.setSprite(sprite);
            } catch (Exception ignored) {
                this.setSprite(this.spriteSet.get(frameIndex, SPRITE_COUNT));
            }
        } else {
            this.setSprite(this.spriteSet.get(frameIndex, SPRITE_COUNT));
        }
        
        this.quadSize = Mth.lerp(progress, this.startSize, END_QUAD_SIZE);
    }

    @Nonnull
    @Override
    public ParticleRenderType getRenderType(){
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static class Factory implements ParticleProvider<IonParticleData>{
        private final SpriteSet spriteSet;
        public Factory(SpriteSet ionSpriteSet) {
            this.spriteSet = ionSpriteSet;
        }

        @Override
        public Particle createParticle(@Nonnull IonParticleData data, @Nonnull ClientLevel level, 
        double x, double y, double z, double dx, double dy, double dz){
            return new IonParticle(level, x, y, z, dx, dy, dz, this.spriteSet, data);
        }
    }
}
