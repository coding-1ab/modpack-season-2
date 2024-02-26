package org.antarcticgardens.cna.fabric.compat.emi;

import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import org.antarcticgardens.cna.CNABlocks;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;

public class CNAEmiPlugin implements EmiPlugin {
    public static final ResourceLocation ENERGISING_SPRITE_SHEET = new ResourceLocation(CreateNewAge.MOD_ID, "textures/gui/emi_simplified_textures.png");
    public static final EmiStack ENERGISING_WORKSTATION = EmiStack.of(CNABlocks.BASIC_ENERGISER.asItem().getDefaultInstance());
    public static final EmiStack ENERGISING_WORKSTATION_2 = EmiStack.of(CNABlocks.ADVANCED_ENERGISER.asItem().getDefaultInstance());
    public static final EmiStack ENERGISING_WORKSTATION_3 = EmiStack.of(CNABlocks.REINFORCED_ENERGISER.asItem().getDefaultInstance());

    public static final EmiRecipeCategory ENERGISING = 
            new EmiRecipeCategory(new ResourceLocation(CreateNewAge.MOD_ID, "energising"), ENERGISING_WORKSTATION, new EmiTexture(ENERGISING_SPRITE_SHEET, 0, 0, 16, 16));
    
    @Override
    public void register(EmiRegistry registry) {
        registry.addCategory(ENERGISING);
        registry.addWorkstation(ENERGISING, ENERGISING_WORKSTATION);
        registry.addWorkstation(ENERGISING, ENERGISING_WORKSTATION_2);
        registry.addWorkstation(ENERGISING, ENERGISING_WORKSTATION_3);
        
        RecipeManager manager = registry.getRecipeManager();

        // Use vanilla's concept of your recipes and pass them to your EmiRecipe representation
        for (Recipe<Container> recipe : manager.getAllRecipesFor(EnergisingRecipe.TYPE.getType())) {
            registry.addRecipe(new EnergisingEmiRecipe((EnergisingRecipe) recipe));
        }
    }
}
