package dev.codinglabs.modpack.data.language

import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider
import net.neoforged.neoforge.fluids.FluidType
import dev.codinglabs.modpack.Blocks
import dev.codinglabs.modpack.Fluids
import dev.codinglabs.modpack.Items
import dev.codinglabs.modpack.ModPackTweaks
import dev.codinglabs.modpack.VoidAnchorBlock

class English(output: PackOutput) : LanguageProvider(output, ModPackTweaks.ID, "en_us") {
    override fun addTranslations() {
        add(Fluids.ENDER_FUEL_TYPE, "Ender Fuel")
        add(Items.ENDER_FUEL_BUCKET, "Ender Fuel Bucket")
        add(Blocks.ENDER_FIRE, "Ender Fire")
        add(Blocks.ENDER_FUEL, "Ender Fuel")
        add(Blocks.VOID_ANCHOR, "Void Anchor")
        add(Items.SHULKER_SHELL_FRAGMENT, "Shulker Shell Fragment")
        add(VoidAnchorBlock.MSG_NO_PORTAL,  "Must be near an active End Portal")
        add(VoidAnchorBlock.MSG_SET,        "Void Anchor set.")
        add(VoidAnchorBlock.MSG_CHARGES,    "Charges: %s / %s")
        add(VoidAnchorBlock.MSG_FULL,       "Void Anchor is already full")
        add(VoidAnchorBlock.MSG_DESTROYED,  "Your Void Anchor was destroyed")
        add(VoidAnchorBlock.MSG_NO_CHARGES, "Void Anchor has no charges")
        add("itemGroup.${ModPackTweaks.ID}.title", "Coding Lab Modpack Tweaks")
    }

    fun add(fluidType: FluidType, name: String) {
        add(fluidType.descriptionId, name)
    }
}
