package org.antarcticgardens.newage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public class NewAgeTags {
    public static final TagKey<Fluid> ELECTROLYTE_TAG = new TagKey<>(Registries.FLUID, new ResourceLocation(CreateNewAge.MOD_ID, "electrolyte"));
}
