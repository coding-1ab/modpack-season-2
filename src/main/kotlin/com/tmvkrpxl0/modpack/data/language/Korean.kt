package com.tmvkrpxl0.modpack.data.language

import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider
import net.neoforged.neoforge.fluids.FluidType
import com.tmvkrpxl0.modpack.Blocks
import com.tmvkrpxl0.modpack.Fluids
import com.tmvkrpxl0.modpack.Items
import com.tmvkrpxl0.modpack.ModPackTweaks

class Korean(output: PackOutput) : LanguageProvider(output, ModPackTweaks.ID, "ko_kr") {
    override fun addTranslations() {
        add(Fluids.ENDER_FUEL_TYPE, "엔더 연료")
        add(Items.ENDER_FUEL_BUCKET, "엔더 연료 양동이")
        add(Blocks.ENDER_FIRE, "엔더의 불")
        add(Blocks.ENDER_FUEL, "엔더 연료")
        add(Items.SHULKER_SHELL_FRAGMENT, "셜커 껍데기 조각")
        add("itemGroup.${ModPackTweaks.ID}.title", "코딩랩 모드팩 자체 컨텐츠")
    }

    fun add(fluidType: FluidType, name: String) {
        add(fluidType.descriptionId, name)
    }
}
