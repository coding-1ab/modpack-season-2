package org.antarcticgardens.newage.tools;

import com.simibubi.create.content.processing.AssemblyOperatorBlockItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.LinkedList;
import java.util.List;

import static org.antarcticgardens.newage.CreateNewAge.MOD_ID;

public class RegistryTool {

    public static List<ItemStack> items = new LinkedList<>();

    public static void finish() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, new ResourceLocation(MOD_ID, "tab"),
                FabricItemGroup.builder()
                        .icon(() -> items.get(0))
                        .displayItems(
                                (itemDisplayParameters, output) -> {
                                    output.acceptAll(items);
                                }
                        )
                        .title(Component.translatable("tab." + MOD_ID + ".tab")).build());
    }

    public static void start() {

    }

    public static <B extends Block> void registerBlock(String id, B block) {
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(MOD_ID, id), block);
        items.add(Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, id), new BlockItem(block, new Item.Properties())).getDefaultInstance());
    }

    public static <B extends Block> void registerAssemblyBlock(String id, B block) {
        Registry.register(BuiltInRegistries.BLOCK, new ResourceLocation(MOD_ID, id), block);
        items.add(Registry.register(BuiltInRegistries.ITEM, new ResourceLocation(MOD_ID, id), new AssemblyOperatorBlockItem(block, new Item.Properties())).getDefaultInstance());
    }

    public static <B extends Block, E extends BlockEntity> BlockEntityType<E> registerBlockEntityBlock(String id, B block, FabricBlockEntityTypeBuilder.Factory<E> blockEntity) {
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, new ResourceLocation(MOD_ID, id), FabricBlockEntityTypeBuilder.create(blockEntity, block).build());
    }

    public static RecipeType<Recipe<?>> registerRecipe(String id) {
        return RecipeType.register(id);
    }

    public static <T extends RecipeSerializer<?>> void registerSerializer(String id, T serializer) {
        Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, new ResourceLocation(MOD_ID, id), serializer);
    }

}
