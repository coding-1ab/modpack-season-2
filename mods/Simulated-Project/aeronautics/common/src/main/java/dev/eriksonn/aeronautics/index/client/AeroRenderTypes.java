package dev.eriksonn.aeronautics.index.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.eriksonn.aeronautics.Aeronautics;
import foundry.veil.api.client.render.VeilRenderBridge;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class AeroRenderTypes extends RenderType {

    public static final ResourceLocation shaderPath = Aeronautics.path("levitite/levitite");
    private static final ShaderStateShard shaderShard = VeilRenderBridge.shaderState(shaderPath);

    private static final RenderType LEVITITE = RenderType.create(
            Aeronautics.MOD_ID + ":levitite",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            VeilRenderBridge.create(
                            RenderType.CompositeState.builder()
                                    .setShaderState(shaderShard)
                                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                    .setCullState(CULL)
                                    .setTextureState(RenderStateShard.BLOCK_SHEET)
                                    .setLightmapState(LightmapStateShard.LIGHTMAP)
                    ).addLayer(VeilRenderBridge.patchState(4))
                    .create(false)
    );

    private static final RenderType LEVITITE_GHOSTS = RenderType.create(
            Aeronautics.MOD_ID + ":levitite_ghosts",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            VeilRenderBridge.create(
                            RenderType.CompositeState.builder()
                                    .setShaderState(shaderShard)
                                    .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                                    .setCullState(NO_CULL)
                                    .setTextureState(RenderStateShard.BLOCK_SHEET)
                                    .setLightmapState(LightmapStateShard.LIGHTMAP)
                    ).addLayer(VeilRenderBridge.patchState(4))
                    .create(false)
    );

    public AeroRenderTypes(final String name,
                           final VertexFormat format,
                           final VertexFormat.Mode mode,
                           final int bufferSize,
                           final boolean affectsCrumbling,
                           final boolean sortOnUpload,
                           final Runnable setupState,
                           final Runnable clearState) {
        super(name, format, mode, bufferSize, affectsCrumbling, sortOnUpload, setupState, clearState);
    }

    public static RenderType levitite() {
        return LEVITITE;
    }

    public static RenderType levititeGhosts() {
        return LEVITITE_GHOSTS;
    }

}
