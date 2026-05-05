package dev.codinglabs.modpack.data

import net.minecraft.advancements.critereon.ItemPredicate
import net.minecraft.core.HolderLookup
import net.minecraft.data.PackOutput
import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.data.loot.LootTableProvider
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.item.Items
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.predicates.MatchTool
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import dev.codinglabs.modpack.Blocks
import java.util.concurrent.CompletableFuture

fun createLootTablesProvider(
    output: PackOutput,
    lookup: CompletableFuture<HolderLookup.Provider>,
): LootTableProvider {
    return LootTableProvider(
        output,
        setOf(),
        listOf(LootTableProvider.SubProviderEntry(::VoidAnchorBlockLoot, LootContextParamSets.BLOCK)),
        lookup,
    )
}

private class VoidAnchorBlockLoot(holder: HolderLookup.Provider) :
    BlockLootSubProvider(setOf(), FeatureFlags.REGISTRY.allFlags(), holder) {

    override fun generate() {
        val validTool = MatchTool.toolMatches(
            ItemPredicate.Builder.item().of(Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE)
        )

        add(
            Blocks.VOID_ANCHOR,
            LootTable.lootTable().withPool(
                applyExplosionCondition(
                    Blocks.VOID_ANCHOR,
                    LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .add(LootItem.lootTableItem(Blocks.VOID_ANCHOR).`when`(validTool))
                )
            )
        )
    }

    override fun getKnownBlocks(): Iterable<Block> = listOf(Blocks.VOID_ANCHOR)
}

