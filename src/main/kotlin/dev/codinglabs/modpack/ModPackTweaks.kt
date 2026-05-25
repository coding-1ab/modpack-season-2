package dev.codinglabs.modpack

import dev.codinglabs.modpack.config.Config
import net.minecraft.resources.ResourceLocation
import net.neoforged.fml.common.Mod
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import dev.codinglabs.modpack.data.gatherData
import dev.codinglabs.modpack.rapier_entity.network.registerTcpPackets
import net.neoforged.fml.ModContainer
import net.neoforged.fml.config.ModConfig
import thedarkcolour.kotlinforforge.neoforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.neoforge.forge.MOD_BUS

const val ID = "modpack_tweaks"

@Mod(ID)
class ModPackTweaks(
    val container: ModContainer
) {
    companion object {
        val LOGGER: Logger = LogManager.getLogger(ID)
    }

    init {
        LOGGER.info("Starting Coding Lab Modpack Tweaks")

        Blocks.register(MOD_BUS)
        BlockEntities.register(MOD_BUS)
        Items.register(MOD_BUS)
        Fluids.register(MOD_BUS)
        Attachments.register(MOD_BUS)
        Entities.register(MOD_BUS)

        MOD_BUS.addListener(::gatherData)
        MOD_BUS.addListener(::registerTcpPackets)

        FORGE_BUS.addListener(::onLivingDeath)
        FORGE_BUS.addListener(Commands::register)
        FORGE_BUS.addListener(Commands::onHurt)
        FORGE_BUS.addListener(Commands::onLeave)
        FORGE_BUS.addListener(Commands::onTick)

        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC)
    }
}

fun String.toResource(): ResourceLocation = ResourceLocation.fromNamespaceAndPath(ID, this)
