package dev.codinglabs.modpack.config

import net.neoforged.neoforge.common.ModConfigSpec

object Config {
    val ENABLE_UDP: ModConfigSpec.BooleanValue
    val SPEC: ModConfigSpec

    init {
        val builder = ModConfigSpec.Builder()
        ENABLE_UDP = builder
            .comment("Should we enable udp")
            .define("enable_udp", true)
        SPEC = builder.build()
    }
}
