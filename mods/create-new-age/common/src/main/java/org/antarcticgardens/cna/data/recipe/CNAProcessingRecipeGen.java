package org.antarcticgardens.cna.data.recipe;

import com.simibubi.create.AllItems;
import com.simibubi.create.AllRecipeTypes;
import com.simibubi.create.content.kinetics.crusher.CrushingRecipe;
import com.simibubi.create.content.kinetics.deployer.DeployerApplicationRecipe;
import com.simibubi.create.content.kinetics.mixer.CompactingRecipe;
import com.simibubi.create.content.kinetics.mixer.MixingRecipe;
import com.simibubi.create.content.kinetics.press.PressingRecipe;
import com.simibubi.create.content.kinetics.saw.CuttingRecipe;
import com.simibubi.create.content.processing.recipe.*;
import com.simibubi.create.foundation.data.recipe.CreateRecipeProvider;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import org.antarcticgardens.cna.CNAItems;
import org.antarcticgardens.cna.CNATags;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipeBuilder;
import org.antarcticgardens.cna.data.FluidConstants;

import java.util.function.UnaryOperator;

#if CNA_FABRIC
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import io.github.fabricators_of_create.porting_lib.tags.Tags;
#else
import net.minecraftforge.common.Tags;
#endif

@SuppressWarnings("unused")
public class CNAProcessingRecipeGen extends CreateRecipeProvider {
    // ========================================================================================================= Cutting
    
    GeneratedRecipe COPPER_WIRE = builder(CNAItems.COPPER_WIRE)
            .amount(4)
            .cutting(b -> b
                    .withItemIngredients(Ingredient.of(CNATags.Common.PLATES_COPPER))
                    .duration(100));
    
    GeneratedRecipe OVERCHARGED_GOLDEN_WIRE = builder(CNAItems.OVERCHARGED_GOLDEN_WIRE)
            .amount(4)
            .cutting(b -> b
                    .withItemIngredients(Ingredient.of(CNAItems.OVERCHARGED_GOLDEN_SHEET))
                    .duration(100));

    GeneratedRecipe OVERCHARGED_IRON_WIRE = builder(CNAItems.OVERCHARGED_IRON_WIRE)
            .amount(4)
            .cutting(b -> b
                    .withItemIngredients(Ingredient.of(CNAItems.OVERCHARGED_IRON_SHEET))
                    .duration(100));

    // ======================================================================================================= Deploying
    
    GeneratedRecipe COPPER_CIRCUIT = builder(CNAItems.COPPER_CIRCUIT)
            .deploying(b -> b
                    .withItemIngredients(Ingredient.of(CNAItems.BLANK_CIRCUIT), Ingredient.of(CNAItems.COPPER_WIRE)));

    // ====================================================================================================== Energising
    
    GeneratedRecipe OVERCHARGED_DIAMOND = builder(CNAItems.OVERCHARGED_DIAMOND)
            .energising(b -> (EnergisingRecipeBuilder) b
                    .energyNeeded(10000)
                    .withItemIngredients(Ingredient.of(Tags.Items.GEMS_DIAMOND)));

    GeneratedRecipe OVERCHARGED_GOLD = builder(CNAItems.OVERCHARGED_GOLD)
            .energising(b -> (EnergisingRecipeBuilder) b
                    .energyNeeded(2000)
                    .withItemIngredients(Ingredient.of(Tags.Items.INGOTS_GOLD)));

    GeneratedRecipe OVERCHARGED_GOLDEN_SHEET_ENERGISING = builder(CNAItems.OVERCHARGED_GOLDEN_SHEET)
            .energising(b -> (EnergisingRecipeBuilder) b
                    .energyNeeded(2000)
                    .withItemIngredients(Ingredient.of(CNATags.Common.PLATES_GOLD)));

    GeneratedRecipe OVERCHARGED_IRON = builder(CNAItems.OVERCHARGED_IRON)
            .energising(b -> (EnergisingRecipeBuilder) b
                    .energyNeeded(1000)
                    .withItemIngredients(Ingredient.of(Tags.Items.INGOTS_IRON)));

    GeneratedRecipe OVERCHARGED_IRON_SHEET_ENERGISING = builder(CNAItems.OVERCHARGED_IRON_SHEET)
            .energising(b -> (EnergisingRecipeBuilder) b
                    .energyNeeded(1000)
                    .withItemIngredients(Ingredient.of(CNATags.Common.PLATES_IRON)));
    
    GeneratedRecipe EXPERIENCE_BOTTLE = builder(Items.EXPERIENCE_BOTTLE)
            .energising(b -> (EnergisingRecipeBuilder) b
                    .energyNeeded(50000)
                    .withItemIngredients(Ingredient.of(Items.GLASS_BOTTLE)));

    // ====================================================================================================== Compacting
    
    GeneratedRecipe BLANK_CIRCUIT = builder(CNAItems.BLANK_CIRCUIT)
            .amount(4)
            .compacting(b -> b
                    .withItemIngredients(
                            Ingredient.of(Tags.Items.STONE), 
                            Ingredient.of(Tags.Items.DUSTS_REDSTONE), 
                            Ingredient.of(Tags.Items.INGOTS_IRON), 
                            Ingredient.of(Items.QUARTZ))
                    .requiresHeat(HeatCondition.SUPERHEATED));

    // ======================================================================================================== Pressing
    
    GeneratedRecipe OVERCHARGED_GOLDEN_SHEET_PRESSING = builder(CNAItems.OVERCHARGED_GOLDEN_SHEET)
            .pressing(b -> b.withItemIngredients(Ingredient.of(CNAItems.OVERCHARGED_GOLD)));

    GeneratedRecipe OVERCHARGED_IRON_SHEET_PRESSING = builder(CNAItems.OVERCHARGED_IRON_SHEET)
            .pressing(b -> b.withItemIngredients(Ingredient.of(CNAItems.OVERCHARGED_IRON)));

    // ======================================================================================================== Crushing
    
    GeneratedRecipe RADIOACTIVE_THORIUM = builder(null)
            .name("radioactive_thorium")
            .crushing(b -> b
                    .withItemIngredients(Ingredient.of(CNAItems.THORIUM))
                    .output(0.1f, CNAItems.RADIOACTIVE_THORIUM)
                    .output(0.7f, Items.IRON_NUGGET)
                    .output(0.75f, AllItems.EXP_NUGGET)
                    .duration(400));

    // ========================================================================================================== Mixing
    
    GeneratedRecipe THORIUM = builder(CNAItems.THORIUM)
            .amount(2)
            .mixing(b -> b
                    .withItemIngredients(
                            Ingredient.of(CNAItems.THORIUM),
                            Ingredient.of(Tags.Items.STONE),
                            Ingredient.of(Items.CLAY))
                    .withFluidIngredients(FluidIngredient.fromFluid(Fluids.WATER, FluidConstants.BUCKET)));
    
    // =================================================================================================================
    
    protected GeneratedRecipeBuilder builder(ItemLike result) {
        return new GeneratedRecipeBuilder(result);
    }
    
    protected class GeneratedRecipeBuilder extends GeneratedRecipeBuilderBase<GeneratedRecipeBuilder> {
        public GeneratedRecipeBuilder(ItemLike result) {
            super(result);
        }
        
        protected GeneratedRecipe cutting(UnaryOperator<ProcessingRecipeBuilder<CuttingRecipe>> operator) {
            ProcessingRecipeSerializer<CuttingRecipe> serializer = AllRecipeTypes.CUTTING.getSerializer();
            return create(operator, serializer.getFactory());
        }
        
        protected GeneratedRecipe deploying(UnaryOperator<ProcessingRecipeBuilder<DeployerApplicationRecipe>> operator) {
            ProcessingRecipeSerializer<DeployerApplicationRecipe> serializer = AllRecipeTypes.DEPLOYING.getSerializer();
            return create(operator, serializer.getFactory());
        }
        
        protected GeneratedRecipe compacting(UnaryOperator<ProcessingRecipeBuilder<CompactingRecipe>> operator) {
            ProcessingRecipeSerializer<CompactingRecipe> serializer = AllRecipeTypes.COMPACTING.getSerializer();
            return create(operator, serializer.getFactory());
        }

        protected GeneratedRecipe pressing(UnaryOperator<ProcessingRecipeBuilder<PressingRecipe>> operator) {
            ProcessingRecipeSerializer<PressingRecipe> serializer = AllRecipeTypes.PRESSING.getSerializer();
            return create(operator, serializer.getFactory());
        }

        protected GeneratedRecipe crushing(UnaryOperator<ProcessingRecipeBuilder<CrushingRecipe>> operator) {
            ProcessingRecipeSerializer<CrushingRecipe> serializer = AllRecipeTypes.CRUSHING.getSerializer();
            return create(operator, serializer.getFactory());
        }

        protected GeneratedRecipe mixing(UnaryOperator<ProcessingRecipeBuilder<MixingRecipe>> operator) {
            ProcessingRecipeSerializer<MixingRecipe> serializer = AllRecipeTypes.MIXING.getSerializer();
            return create(operator, serializer.getFactory());
        }

        protected GeneratedRecipe energising(UnaryOperator<EnergisingRecipeBuilder> operator) {
            ProcessingRecipeSerializer<EnergisingRecipe> serializer = EnergisingRecipe.TYPE.getSerializer();
            return register(consumer -> {
                EnergisingRecipeBuilder builder = new EnergisingRecipeBuilder(serializer.getFactory(), createLocation());
                if (result != null) {
                    builder.output(result, amount);
                }
                operator.apply(builder).build(consumer);
            });
        }
        
        protected <T extends ProcessingRecipe<?>> GeneratedRecipe create(UnaryOperator<ProcessingRecipeBuilder<T>> operator, 
                                                                         ProcessingRecipeBuilder.ProcessingRecipeFactory<T> factory) {
            return register(consumer -> {
                ProcessingRecipeBuilder<T> builder = new ProcessingRecipeBuilder<>(factory, createLocation());
                if (result != null) {
                    builder.output(result, amount);
                }
                operator.apply(builder).build(consumer);
            });
        }
    }

    @Override
    public String getName() {
        return "Create New Age Processing Recipes";
    }

    public CNAProcessingRecipeGen(PackOutput output) {
        #if CNA_FABRIC
        super((FabricDataOutput) output);
        #else
        super(output);
        #endif
    }
}
