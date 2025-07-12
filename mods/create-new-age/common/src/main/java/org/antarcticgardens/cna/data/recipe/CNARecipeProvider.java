package org.antarcticgardens.cna.data.recipe;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import org.antarcticgardens.cna.CreateNewAge;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


// Based on Steam and Rails' RailwaysRecipeProvider.java
// https://github.com/Layers-of-Railways/Railway/blob/1.20/dev/common/src/main/java/com/railwayteam/railways/base/data/recipe/RailwaysRecipeProvider.java

public abstract class CNARecipeProvider extends RecipeProvider {
    protected final List<GeneratedRecipe> all = new ArrayList<>();

    public CNARecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> writer) {
        all.forEach(c -> c.register(writer));
        CreateNewAge.LOGGER.info(getName() + " registered " + all.size() + " recipe" + (all.size() == 1 ? "" : "s"));
    }

    protected GeneratedRecipe register(GeneratedRecipe recipe) {
        all.add(recipe);
        return recipe;
    }

    @FunctionalInterface
    public interface GeneratedRecipe {
        void register(Consumer<FinishedRecipe> consumer);
    }
}
