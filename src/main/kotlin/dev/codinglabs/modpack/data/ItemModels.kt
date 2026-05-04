package dev.codinglabs.modpack.data

import net.minecraft.data.PackOutput
import net.neoforged.neoforge.client.model.generators.ItemModelProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import dev.codinglabs.modpack.ModPackTweaks
import dev.codinglabs.modpack.toResource

class ItemModels(output: PackOutput, existingFileHelper: ExistingFileHelper) :
    ItemModelProvider(output, ModPackTweaks.ID, existingFileHelper) {
    override fun registerModels() {
        withExistingParent("void_anchor", modLoc("block/void_anchor"))
        withExistingParent("ender_fuel_bucket", "minecraft:item/generated").texture(
            "layer0",
            "item/ender_fuel_bucket".toResource()
        )
        withExistingParent("shulker_shell_fragment", "minecraft:item/generated").texture(
            "layer0",
            "item/shulker_shell_fragment".toResource()
        )
    }
}
