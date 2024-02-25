package org.antarcticgardens.cna.fabric;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.world.level.levelgen.GenerationStep;
import org.antarcticgardens.cna.data.worldgen.CNAPlacedFeatures;

public class CNAFabricBiomeModifiers {
    public static void bootstrap() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, CNAPlacedFeatures.THORIUM_ORE);
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(), GenerationStep.Decoration.UNDERGROUND_ORES, CNAPlacedFeatures.MAGNETITE_BLOCK);
    }
}
