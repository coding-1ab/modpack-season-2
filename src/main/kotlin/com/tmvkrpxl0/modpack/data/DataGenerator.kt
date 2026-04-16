package org.example.com.tmvkrpxl0.modpack.data

import net.neoforged.neoforge.data.event.GatherDataEvent

fun gatherData(event: GatherDataEvent) {
    val generator = event.generator
    val output = generator.packOutput
    val lookup = event.lookupProvider
    val helper = event.existingFileHelper

    event.generator.addProvider(event.includeClient(), BlockStates(output, helper))
    event.generator.addProvider(event.includeServer(), BlockTags(output, lookup, helper))
}
