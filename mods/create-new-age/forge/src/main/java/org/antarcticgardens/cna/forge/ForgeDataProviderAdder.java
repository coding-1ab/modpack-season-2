package org.antarcticgardens.cna.forge;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraftforge.data.event.GatherDataEvent;
import org.antarcticgardens.cna.platform.DataProviderAdder;

import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ForgeDataProviderAdder implements DataProviderAdder {
    private final GatherDataEvent event;
    
    public ForgeDataProviderAdder(GatherDataEvent event) {
        this.event = event;
    }
    
    @Override
    public void addProvider(Function<PackOutput, DataProvider> factory) {
        event.getGenerator().addProvider(true, factory.apply(event.getGenerator().getPackOutput()));
    }

    @Override
    public void addProvider(BiFunction<PackOutput, CompletableFuture<HolderLookup.Provider>, DataProvider> factory) {
        event.getGenerator().addProvider(true, factory.apply(event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }
}
