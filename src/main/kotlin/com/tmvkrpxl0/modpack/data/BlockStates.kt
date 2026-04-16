package org.example.com.tmvkrpxl0.modpack.data

import net.minecraft.data.PackOutput
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.common.data.ExistingFileHelper
import org.example.com.tmvkrpxl0.modpack.Blocks.ENDER_FIRE
import org.example.com.tmvkrpxl0.modpack.EnderFire
import org.example.com.tmvkrpxl0.modpack.ModPackTweaks

class BlockStates(output: PackOutput, helper: ExistingFileHelper) :
    BlockStateProvider(output, ModPackTweaks.ID, helper) {
    override fun registerStatesAndModels() {

        fire(
            "ender_fire",
            ENDER_FIRE,
            EnderFire.NORTH,
            EnderFire.EAST,
            EnderFire.SOUTH,
            EnderFire.WEST,
            EnderFire.UP,
        )
    }

    fun fire(
        prefix: String,
        fireBlock: Block,
        northProperty: BooleanProperty,
        eastProperty: BooleanProperty,
        southProperty: BooleanProperty,
        westProperty: BooleanProperty,
        upProperty: BooleanProperty,
    ) {
        val floor = ArrayList<BlockModelBuilder>()
        val up = ArrayList<BlockModelBuilder>()
        val side = ArrayList<BlockModelBuilder>()

        val suffixes = arrayOf(
            "_up",
            "_up_alt",
            "_side",
            "_side_alt",
            "_floor",
        )

        for (suffix in suffixes) {
            val name = "${prefix}${suffix}"
            val templateName = "minecraft:block/template_fire${suffix}"
            for (i in 0..1) {
                val path = "${name}${i}"
                val modelBuilder = useTemplate(path, templateName, "fire", "block/${prefix}_${i}")
                    .renderType("minecraft:cutout_mipped")

                val noSuffix = name.removeSuffix("_alt")
                if (noSuffix.endsWith("up")) {
                    up.add(modelBuilder)
                } else if (noSuffix.endsWith("side")) {
                    side.add(modelBuilder)
                } else if (noSuffix.endsWith("floor")) {
                    floor.add(modelBuilder)
                } else {
                    throw IllegalStateException("Unknown model name")
                }
            }
        }

        assert(floor.size == 2)
        assert(side.size == 4)
        assert(up.size == 4)

        val blockStateBuilder = getMultipartBuilder(fireBlock)
        val downModel = blockStateBuilder.part()
            .modelFile(floor[0])
            .nextModel()
            .modelFile(floor[1])
            .addModel()
            .condition(northProperty, false)
            .condition(eastProperty, false)
            .condition(southProperty, false)
            .condition(westProperty, false)
            .condition(upProperty, false)

        val allDirections = arrayOf(
            northProperty,
            eastProperty,
            southProperty,
            westProperty,
            upProperty,
        )
        val sideDirections = arrayOf(
            northProperty to 0,
            eastProperty to 90,
            southProperty to 180,
            westProperty to 270,
        )

        val sideModels = sideDirections.map { (sideProperty, angle) ->
            val builder = blockStateBuilder.part()
                .modelFile(side[0])
                .rotationY(angle)
                .nextModel()
                .modelFile(side[1])
                .rotationY(angle)
                .nextModel()
                .modelFile(side[2])
                .rotationY(angle)
                .nextModel()
                .modelFile(side[3])
                .rotationY(angle)
                .addModel()
                .useOr()
                .nestedGroup()
                .condition(sideProperty, true)
                .end()

            builder.nestedGroup().let { group ->
                for (direction in allDirections) {
                    group.condition(direction, false)
                }
                group.end()
            }

            builder
        }

        assert(sideModels.size == 4)

        val upModel = blockStateBuilder.part()
            .modelFile(up[0])
            .nextModel()
            .modelFile(up[1])
            .nextModel()
            .modelFile(up[2])
            .nextModel()
            .modelFile(up[3])
            .addModel()
            .condition(upProperty, true)
    }

    fun useTemplate(path: String, templateName: String, key: String, texture: String): BlockModelBuilder {
        return models().withExistingParent(path, templateName).texture(key, texture)
    }
}
