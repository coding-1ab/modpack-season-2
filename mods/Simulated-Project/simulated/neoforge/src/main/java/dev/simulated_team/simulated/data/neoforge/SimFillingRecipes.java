package dev.simulated_team.simulated.data.neoforge;

import com.simibubi.create.AllFluids;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.FillingRecipeGen;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.index.SimItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import java.util.concurrent.CompletableFuture;

public class SimFillingRecipes extends FillingRecipeGen {
    private final GeneratedRecipe HONEY_GLUE = this.create("honey_glue",
            b -> b.require(AllFluids.HONEY.get(), 500)
                  .require(AllItems.IRON_SHEET)
                  .output(SimItems.HONEY_GLUE));

    public SimFillingRecipes(final PackOutput output, final CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, Simulated.MOD_ID);
    }

    @Override
    public String getName() {
        return "Simulated's Fantastic Filling Recipes";
    }

}
