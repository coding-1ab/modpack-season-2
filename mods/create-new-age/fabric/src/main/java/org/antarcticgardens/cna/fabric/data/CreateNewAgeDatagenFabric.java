package org.antarcticgardens.cna.fabric.data;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.data.recipe.CNAStandardRecipeGen;
import org.antarcticgardens.cna.data.CreateNewAgeDatagen;
import org.antarcticgardens.cna.fabric.FabricDataProviderAdder;

import java.util.function.Consumer;
import java.util.function.Function;

public class CreateNewAgeDatagenFabric extends CreateNewAgeDatagen implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        
        CreateNewAge.REGISTRATE.setupDatagen(pack, ExistingFileHelper.withResourcesFromArg());
        setupDatagen(new FabricDataProviderAdder(pack));
    }
}
