package dev.codinglabs.modpack.data

import net.minecraft.data.PackOutput
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder
import net.neoforged.neoforge.client.model.generators.BlockStateProvider
import net.neoforged.neoforge.client.model.generators.ConfiguredModel
import net.neoforged.neoforge.common.data.ExistingFileHelper
import dev.codinglabs.modpack.Blocks
import dev.codinglabs.modpack.Blocks.ENDER_FIRE
import dev.codinglabs.modpack.EnderFire
import dev.codinglabs.modpack.ModPackTweaks
import dev.codinglabs.modpack.VoidAnchorBlock
import it.unimi.dsi.fastutil.objects.ObjectArrayList

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
        fluid("ender_fuel")
        voidAnchor()
    }

    private fun voidAnchor() {
        val disabled = voidAnchorModel("void_anchor_disabled", "disabled")
        val idle = voidAnchorModel("void_anchor", "0")
        val charged1 = voidAnchorModel("void_anchor_charge_1", "1")
        val charged2 = voidAnchorModel("void_anchor_charge_2", "2")
        val charged3 = voidAnchorModel("void_anchor_charge_3", "3")
        val charged4 = voidAnchorModel("void_anchor_charge_4", "4")
        models().withExistingParent("void_anchor_item", mcLoc("block/cube"))
            .texture("particle", modLoc("block/void_anchor_side"))
            .texture("down", modLoc("block/void_anchor_side"))
            .texture("up", modLoc("block/void_anchor_top_0"))
            .texture("north", modLoc("block/void_anchor_front_0"))
            .texture("south", modLoc("block/void_anchor_side"))
            .texture("east", modLoc("block/void_anchor_side"))
            .texture("west", modLoc("block/void_anchor_side"))

        getVariantBuilder(Blocks.VOID_ANCHOR).forAllStates { state ->
            val active = state.getValue(VoidAnchorBlock.ACTIVE)
            val charges = state.getValue(VoidAnchorBlock.CHARGES)
            val model = when {
                !active -> disabled
                charges == 0 -> idle
                charges == 1 -> charged1
                charges == 2 -> charged2
                charges == 3 -> charged3
                else -> charged4
            }

            val yRot = when (state.getValue(VoidAnchorBlock.FACING)) {
                Direction.NORTH -> 0
                Direction.EAST -> 90
                Direction.SOUTH -> 180
                Direction.WEST -> 270
                else -> 0
            }

            ConfiguredModel.builder().modelFile(model).rotationY(yRot).build()
        }
    }

    private fun voidAnchorModel(name: String, suffix: String): BlockModelBuilder {
        return models().withExistingParent(name, mcLoc("block/cube"))
            .texture("particle", modLoc("block/void_anchor_side"))
            .texture("down", modLoc("block/void_anchor_side"))
            .texture("up", modLoc("block/void_anchor_top_clear"))
            .texture("north", modLoc("block/void_anchor_front_$suffix"))
            .texture("south", modLoc("block/void_anchor_side"))
            .texture("east", modLoc("block/void_anchor_side"))
            .texture("west", modLoc("block/void_anchor_side"))
    }

    @Suppress("UnusedVariable", "unused")
    fun fluid(fluidName: String) {
        val stateBuilder = getVariantBuilder(Blocks.ENDER_FUEL)
        val model = models()
            .getBuilder(fluidName)
            .texture("particle", "minecraft:block/water_still")
            .renderType("minecraft:translucent").let { ConfiguredModel(it) }
        val state = stateBuilder.partialState()
            .addModels(model)
    }

    @Suppress("UnusedVariable", "unused")
    fun fire(
        prefix: String,
        fireBlock: Block,
        northProperty: BooleanProperty,
        eastProperty: BooleanProperty,
        southProperty: BooleanProperty,
        westProperty: BooleanProperty,
        upProperty: BooleanProperty,
    ) {
        val floor = ObjectArrayList<BlockModelBuilder>()
        val up = ObjectArrayList<BlockModelBuilder>()
        val side = ObjectArrayList<BlockModelBuilder>()

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
