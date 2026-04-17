package org.example.com.tmvkrpxl0.modpack.data

import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import org.example.com.tmvkrpxl0.modpack.ModPackTweaks
import org.example.com.tmvkrpxl0.modpack.toResource

class ItemModels(output: PackOutput, existingFileHelper: ExistingFileHelper) :
    ItemModelProvider(output, ModPackTweaks.ID, existingFileHelper) {
    override fun registerModels() {
        withExistingParent("ender_fuel_bucket", "minecraft:item/generated").texture(
            "layer0",
            "item/ender_fuel_bucket".toResource()
        )
    }
}
