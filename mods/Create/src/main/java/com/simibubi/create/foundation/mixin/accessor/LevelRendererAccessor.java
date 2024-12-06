package com.simibubi.create.foundation.mixin.accessor;

import net.minecraft.client.renderer.LevelRenderer;

import net.minecraft.client.renderer.culling.Frustum;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LevelRenderer.class)
public interface LevelRendererAccessor {
	@Accessor("cullingFrustum")
	Frustum create$getCullingFrustum();

	@Accessor("capturedFrustum")
	Frustum create$getCapturedFrustum();
}
