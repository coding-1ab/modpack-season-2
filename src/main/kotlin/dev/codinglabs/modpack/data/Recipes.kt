package dev.codinglabs.modpack.data

import appeng.core.definitions.AEItems
import com.mrh0.createaddition.index.CAFluids
import com.simibubi.create.AllItems
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe
import com.simibubi.create.content.kinetics.mixer.MixingRecipe
import com.simibubi.create.content.processing.recipe.HeatCondition
import com.simibubi.create.content.processing.recipe.StandardProcessingRecipe
import dev.codinglabs.modpack.Fluids
import dev.codinglabs.modpack.toResource
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.RecipeOutput
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.fluids.FluidStack
import plus.dragons.createdragonsplus.common.kinetics.fan.ending.EndingRecipe
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids
import java.util.concurrent.CompletableFuture
import dev.codinglabs.modpack.Items as MyItems

class Recipes(output: PackOutput, registries: CompletableFuture<HolderLookup.Provider>) :
    RecipeProvider(output, registries) {

    override fun buildRecipes(recipeOutput: RecipeOutput) {
        ending(Items.ARMADILLO_SCUTE, MyItems.SHULKER_SHELL_FRAGMENT, recipeOutput)
        enderFuelRecipe(recipeOutput)
        shulkerShellRecipe(recipeOutput)
        nuggetExperienceRecipe(recipeOutput)
    }

    @Suppress("SameParameterValue")
    private fun ending(from: ItemLike, to: ItemLike, output: RecipeOutput) {
        val recipeId = "ending_${from.asItem().id.namespace}_to_${to.asItem().id.namespace}".toResource()
        EndingRecipe.builder(recipeId).require(from).output(to).build(output)
    }

    private fun enderFuelRecipe(output: RecipeOutput) {
        val id = "mix_ender_fuel".toResource()
        val builder = StandardProcessingRecipe.Builder(::MixingRecipe, id)

        builder.requiresHeat(HeatCondition.HEATED)
            .require(CAFluids.SEED_OIL.get(), 250)
            .require(AEItems.ENDER_DUST)
            .output(FluidStack(Fluids.ENDER_FUEL, 250))
            .build(output)
    }

    private fun shulkerShellRecipe(output: RecipeOutput) {
        val id = "mix_shulker_shell".toResource()
        val builder = StandardProcessingRecipe.Builder(::CompactingRecipe, id)

        builder.requiresHeat(HeatCondition.HEATED)
            .require(CEIFluids.EXPERIENCE.get(), 100)
            .require(Ingredient.of(MyItems.SHULKER_SHELL_FRAGMENT, MyItems.SHULKER_SHELL_FRAGMENT, MyItems.SHULKER_SHELL_FRAGMENT))
            .output(Items.SHULKER_SHELL)
            .build(output)
    }

    private fun nuggetExperienceRecipe(output: RecipeOutput) {
        val id = "artificial_experience_nugget".toResource()
        val builder = StandardProcessingRecipe.Builder(::CompactingRecipe, id)

        builder.requiresHeat(HeatCondition.HEATED)
            .require(Ingredient.of(ItemStack(Items.AMETHYST_SHARD), ItemStack(Items.GLOWSTONE_DUST, 4)))
            .output(AllItems.EXP_NUGGET)
            .build(output)
    }

    val Item.id: ResourceLocation
        get() {
            return BuiltInRegistries.ITEM.getKey(this)
        }
}
