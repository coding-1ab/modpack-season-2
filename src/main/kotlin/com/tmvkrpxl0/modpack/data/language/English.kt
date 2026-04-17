package org.example.com.tmvkrpxl0.modpack.data.language

import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider
import net.neoforged.neoforge.fluids.FluidType
import org.example.com.tmvkrpxl0.modpack.Blocks
import org.example.com.tmvkrpxl0.modpack.Fluids
import org.example.com.tmvkrpxl0.modpack.Items
import org.example.com.tmvkrpxl0.modpack.ModPackTweaks

class English(output: PackOutput) : LanguageProvider(output, ModPackTweaks.ID, "en_us") {
    override fun addTranslations() {
        add(Fluids.ENDER_FUEL_TYPE, "Ender Fuel")
        add(Items.ENDER_FUEL_BUCKET, "Ender Fuel Bucket")
        add(Blocks.ENDER_FIRE, "Ender Fire")
    }

    fun add(fluidType: FluidType, name: String) {
        add(fluidType.descriptionId, name)
    }
}
