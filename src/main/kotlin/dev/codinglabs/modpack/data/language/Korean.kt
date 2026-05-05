package dev.codinglabs.modpack.data.language

import net.minecraft.data.PackOutput
import net.neoforged.neoforge.common.data.LanguageProvider
import net.neoforged.neoforge.fluids.FluidType
import dev.codinglabs.modpack.Blocks
import dev.codinglabs.modpack.Fluids
import dev.codinglabs.modpack.Items
import dev.codinglabs.modpack.ModPackTweaks
import dev.codinglabs.modpack.VoidAnchorBlock

class Korean(output: PackOutput) : LanguageProvider(output, ModPackTweaks.ID, "ko_kr") {
    override fun addTranslations() {
        add(Fluids.ENDER_FUEL_TYPE, "엔더 연료")
        add(Items.ENDER_FUEL_BUCKET, "엔더 연료 양동이")
        add(Blocks.ENDER_FIRE, "엔더의 불")
        add(Blocks.ENDER_FUEL, "엔더 연료")
        add(Blocks.VOID_ANCHOR, "공허 정박기")
        add(Items.SHULKER_SHELL_FRAGMENT, "셜커 껍데기 조각")
        add(VoidAnchorBlock.MSG_NO_PORTAL,  "엔드 포탈 근처에서만 사용할 수 있습니다")
        add(VoidAnchorBlock.MSG_SET,        "공허 정박기가 등록되었습니다.")
        add(VoidAnchorBlock.MSG_CHARGES,    "충전량: %s / %s")
        add(VoidAnchorBlock.MSG_FULL,       "이미 충전이 완료되었습니다.")
        add(VoidAnchorBlock.MSG_DESTROYED,  "공허 정박기가 파괴되어 앵커가 해제되었습니다")
        add(VoidAnchorBlock.MSG_NO_CHARGES, "충전량이 부족합니다")
        add("itemGroup.${ModPackTweaks.ID}.title", "코딩랩 모드팩 자체 컨텐츠")
    }

    fun add(fluidType: FluidType, name: String) {
        add(fluidType.descriptionId, name)
    }
}
