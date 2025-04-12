package com.mrh0.createaddition.recipe.rolling;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.compat.jei.RollingMillAssemblySubCategory;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.recipe.liquid_burning.LiquidBurningRecipe;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.wrapper.RecipeWrapper;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class RollingRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {
    protected final ItemStack output;
    protected final Ingredient ingredient;

    protected RollingRecipe(String group, Ingredient ingredient, ItemStack output) {
        // This line needs to be checked
        super(new RollingRecipeInfo((SequencedAssemblyRollingRecipeSerializer) CARecipes.ROLLING.get(), CARecipes.ROLLING_TYPE.get()), new RollingMillRecipeParams(ResourceLocation.fromNamespaceAndPath(CreateAddition.MODID,group),ingredient, new ProcessingOutput(output, 1f)));
        this.output = output;
        this.ingredient = ingredient;
    }

    public static void register() {
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    @Override
    public boolean matches(RecipeWrapper inv, @NotNull Level level) {
        if (inv.isEmpty()) return false;
        return ingredient.test(inv.getItem(0));
    }

    @Override
    protected int getMaxInputCount() {
        return 1;
    }

    @Override
    protected int getMaxOutputCount() {
        return 1;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeWrapper recipeWrapper, HolderLookup.@NotNull Provider provider) {
        return this.output;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > 0;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider provider) {
        return this.output;
    }

    public ItemStack getResultItem() {
        return this.output;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return CARecipes.ROLLING.get();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return CARecipes.ROLLING_TYPE.get();
    }

    @Override
    public @NotNull ItemStack getToastSymbol() {
        return this.output;
    }

    @Override
    public Component getDescriptionForAssembly() {
        return Component.translatable("createaddition.recipe.rolling.sequence").withStyle(ChatFormatting.DARK_GREEN);
    }

    @Override
    public void addRequiredMachines(Set<ItemLike> set) {
        set.add(CABlocks.ROLLING_MILL.get());
    }

    @Override
    public void addAssemblyIngredients(List<Ingredient> list) {

    }

    @Override
    public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
        return () -> RollingMillAssemblySubCategory::new;
    }

    public static class Serializer implements RecipeSerializer<RollingRecipe> {
        private static final MapCodec<RollingRecipe> CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(RollingRecipe::getGroup),
                        //CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(ChargingRecipe::category),
                        Ingredient.CODEC.fieldOf("input").forGetter(r -> r.ingredient),
                        ItemStack.CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(r -> r.output)
                ).apply(builder, RollingRecipe::new)
        );

        public static final StreamCodec<RegistryFriendlyByteBuf, RollingRecipe> STREAM_CODEC = StreamCodec.of(
                RollingRecipe.Serializer::toNetwork, RollingRecipe.Serializer::fromNetwork
        );

        @Override
        public MapCodec<RollingRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, RollingRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static RollingRecipe fromNetwork(RegistryFriendlyByteBuf buffer) {
            String group = buffer.readUtf();
            ItemStack output = ItemStack.STREAM_CODEC.decode(buffer);
            Ingredient input = Ingredient.CONTENTS_STREAM_CODEC.decode(buffer);
            return new RollingRecipe(group, input, output);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buffer, RollingRecipe recipe) {
            buffer.writeUtf(recipe.getGroup());
            ItemStack.STREAM_CODEC.encode(buffer, recipe.output);
            Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, recipe.ingredient);
        }
    }
}
