package org.antarcticgardens.cna.fabric;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import org.antarcticgardens.cna.platform.DataProviderAdder;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class FabricDataProviderAdder implements DataProviderAdder {
    private final FabricDataGenerator.Pack pack;
    
    public FabricDataProviderAdder(FabricDataGenerator.Pack pack) {
        this.pack = pack;
    }
    
    @Override
    public void addProvider(Function<PackOutput, DataProvider> factory) {
        pack.addProvider((FabricDataGenerator.Pack.Factory<DataProvider>) factory::apply);
    }

    @Override
    public void addProvider(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, DataProvider> factory) {
        pack.addProvider(factory::apply);
    }
}
