package com.simibubi.create.foundation.mixin.accessor;

import net.minecraftforge.client.model.generators.ModelBuilder;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ModelBuilder.class)
public interface ModelBuilderAccessor {
	@Accessor(value = "textures", remap = false)
	Map<String, String> create$getTextures();
}
