package org.antarcticgardens.cna.data.recipe;

import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipe;
import com.simibubi.create.content.processing.sequenced.SequencedAssemblyRecipeBuilder;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipeBuilder;
import org.antarcticgardens.cna.mixin.SequencedAssemblyRecipeBuilderAccessor;

import java.util.function.UnaryOperator;

public class CNASequencedAssemblyRecipeBuilder extends SequencedAssemblyRecipeBuilder {
    private final SequencedAssemblyRecipe recipe;
    
    public CNASequencedAssemblyRecipeBuilder(ResourceLocation id) {
        super(id);
        this.recipe = ((SequencedAssemblyRecipeBuilderAccessor) this).getRecipe();
    }

    @Override
    public <T extends ProcessingRecipe<?>> CNASequencedAssemblyRecipeBuilder addStep(ProcessingRecipeBuilder.ProcessingRecipeFactory<T> factory, 
                                                                                     UnaryOperator<ProcessingRecipeBuilder<T>> builder) {
        super.addStep(factory, builder);
        return this;
    }

    public CNASequencedAssemblyRecipeBuilder addEnergisingStep(UnaryOperator<EnergisingRecipeBuilder> builder) {
        EnergisingRecipeBuilder recipeBuilder =
                new EnergisingRecipeBuilder(EnergisingRecipe::new, new ResourceLocation("dummy"));
        Item placeHolder = recipe.getTransitionalItem()
                .getItem();
        recipe.getSequence()
                .add(new SequencedRecipe<>(builder.apply((EnergisingRecipeBuilder) recipeBuilder.require(placeHolder)
                                .output(placeHolder))
                        .build()));
        return this;
    }

    @Override
    public CNASequencedAssemblyRecipeBuilder require(ItemLike ingredient) {
        super.require(ingredient);
        return this;
    }

    @Override
    public CNASequencedAssemblyRecipeBuilder require(TagKey<Item> tag) {
        super.require(tag);
        return this;
    }

    @Override
    public CNASequencedAssemblyRecipeBuilder require(Ingredient ingredient) {
        super.require(ingredient);
        return this;
    }

    @Override
    public CNASequencedAssemblyRecipeBuilder transitionTo(ItemLike item) {
        super.transitionTo(item);
        return this;
    }

    @Override
    public CNASequencedAssemblyRecipeBuilder loops(int loops) {
        super.loops(loops);
        return this;
    }

    @Override
    public CNASequencedAssemblyRecipeBuilder addOutput(ItemLike item, float weight) {
        super.addOutput(item, weight);
        return this;
    }

    @Override
    public CNASequencedAssemblyRecipeBuilder addOutput(ItemStack item, float weight) {
        super.addOutput(item, weight);
        return this;
    }
}
