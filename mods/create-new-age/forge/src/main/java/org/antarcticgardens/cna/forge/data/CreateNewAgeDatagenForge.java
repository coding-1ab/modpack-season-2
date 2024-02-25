package org.antarcticgardens.cna.forge.data;

import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.antarcticgardens.cna.data.CNAGeneratedEntriesProvider;
import org.antarcticgardens.cna.data.CreateNewAgeDatagen;
import org.antarcticgardens.cna.forge.ForgeDataProviderAdder;

public class CreateNewAgeDatagenForge extends CreateNewAgeDatagen {
    public void gatherData(GatherDataEvent event) {
        setupDatagen(new ForgeDataProviderAdder(event));
        CNAGeneratedEntriesProvider.BUILDER.add(ForgeRegistries.Keys.BIOME_MODIFIERS, CNAForgeBiomeModifiers::bootstrap);
    }
}
