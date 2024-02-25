package org.antarcticgardens.cna.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.CNABlocks;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;

@JeiPlugin
public class CNAJeiPlugin implements IModPlugin {
    private EnergisingJeiCategory energising;
    public static RecipeType<EnergisingRecipe> energisingType = new RecipeType<>(new ResourceLocation(CreateNewAge.MOD_ID, "energising"), EnergisingRecipe.class);
    
    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CreateNewAge.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        energising = new EnergisingJeiCategory();
        registration.addRecipeCategories(
                energising
        );
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(energisingType,
                Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(EnergisingRecipe.TYPE.getType()));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(CNABlocks.BASIC_ENERGISER.asStack(), energisingType);
        registration.addRecipeCatalyst(CNABlocks.ADVANCED_ENERGISER.asStack(), energisingType);
        registration.addRecipeCatalyst(CNABlocks.REINFORCED_ENERGISER.asStack(), energisingType);
    }
}
