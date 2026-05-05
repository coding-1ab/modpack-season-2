package dev.codinglabs.modpack.data

import net.neoforged.neoforge.data.event.GatherDataEvent
import dev.codinglabs.modpack.data.language.English
import dev.codinglabs.modpack.data.language.Korean

fun gatherData(event: GatherDataEvent) {
    val generator = event.generator
    val output = generator.packOutput
    val lookup = event.lookupProvider
    val helper = event.existingFileHelper

    event.generator.addProvider(event.includeClient(), BlockStates(output, helper))
    event.generator.addProvider(event.includeServer(), BlockTags(output, lookup, helper))
    event.generator.addProvider(event.includeServer(), FluidTags(output, lookup, helper))
    event.generator.addProvider(event.includeClient(), English(output))
    event.generator.addProvider(event.includeClient(), Korean(output))
    event.generator.addProvider(event.includeClient(), ItemModels(output, helper))
    event.generator.addProvider(event.includeServer(), Recipes(output, lookup))
    event.generator.addProvider(event.includeServer(), createLootTablesProvider(output, lookup))
}
