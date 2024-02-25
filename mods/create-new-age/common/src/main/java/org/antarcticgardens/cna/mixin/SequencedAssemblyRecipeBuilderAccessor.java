package org.antarcticgardens.cna.mixin;

import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SequencedAssemblyRecipeBuilder.class)
public interface SequencedAssemblyRecipeBuilderAccessor {
    @Accessor(remap = false)
    SequencedAssemblyRecipe getRecipe();
}
