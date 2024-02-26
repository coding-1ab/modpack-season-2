package org.antarcticgardens.cna.content.energising.recipe;

import com.google.gson.JsonObject;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.antarcticgardens.cna.CNABlocks;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.compat.jei.JeiEnergisingSubcategory;
import org.antarcticgardens.cna.util.RecipeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

#if CNA_FABRIC
import com.simibubi.create.compat.recipeViewerCommon.SequencedAssemblySubCategoryType;
#else
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
#endif

public class EnergisingRecipe extends ProcessingRecipe<Container> implements IAssemblyRecipe {
    public static IRecipeTypeInfo TYPE = RecipeUtil.createIRecipeTypeInfo("energising", new ProcessingRecipeSerializer<>(EnergisingRecipe::new));
    private int energyNeeded;
    
    public EnergisingRecipe(ProcessingRecipeBuilder.ProcessingRecipeParams params) {
        super(TYPE, params);
        
        if (params instanceof EnergisingRecipeBuilder.EnergisingRecipeParams p) {
            energyNeeded = p.energyNeeded;
        }
    }

    @Override
    public void readAdditional(JsonObject json) {
        energyNeeded = json.get("energyNeeded").getAsInt();
    }

    @Override
    public void readAdditional(FriendlyByteBuf buffer) {
        energyNeeded = buffer.readInt();
    }

    @Override
    public void writeAdditional(JsonObject json) {
        json.addProperty("energyNeeded", energyNeeded);
    }

    @Override
    public void writeAdditional(FriendlyByteBuf buffer) {
        buffer.writeInt(energyNeeded);
    }
    
    public int getEnergyNeeded() {
        return energyNeeded;
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 4;
    }

    @Override
    public Component getDescriptionForAssembly() {
        return Component.translatable("recipe.assembly.energising");
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> list) {
        list.add(CNABlocks.BASIC_ENERGISER.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {}
    
    @SuppressWarnings("unchecked")
    @Override
    #if CNA_FABRIC
    public SequencedAssemblySubCategoryType getJEISubCategory() {
        return (SequencedAssemblySubCategoryType) CreateNewAge.getInstance().getPlatform().getEnergisingRecipeSubCategory();
    }
    #else
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return (Supplier<Supplier<SequencedAssemblySubCategory>>) CreateNewAge.getInstance().getPlatform().getEnergisingRecipeSubCategory();
    }
    #endif

    @Override
    public boolean matches(Container container, @NotNull Level level) {
        if (container.isEmpty())
            return false;
        return ingredients.get(0)
                .test(container.getItem(0));
    }

    public boolean test(ItemStack stack) {
        return ingredients.get(0)
                .test(stack);
    }
    
    public static void load() {  }
}
