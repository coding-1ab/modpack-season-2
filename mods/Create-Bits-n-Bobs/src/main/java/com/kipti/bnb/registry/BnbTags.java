package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.simibubi.create.AllTags;
import net.createmod.catnip.lang.Lang;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import static com.simibubi.create.AllTags.NameSpace.MOD;

public class BnbTags {


    public enum BnbItemTags {

        CHAIRS;

        public final TagKey<Item> tag;

        BnbItemTags() {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateBitsnBobs.MOD_ID, Lang.asId(name()));
            tag = ItemTags.create(id);
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Item item) {
            return item.builtInRegistryHolder()
                .is(tag);
        }

        public boolean matches(ItemStack stack) {
            return stack.is(tag);
        }

        private static void register() {
        }

    }

    public enum BnbBlockTags {

        CHAIRS;

        public final TagKey<Block> tag;

        BnbBlockTags() {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(CreateBitsnBobs.MOD_ID, Lang.asId(name()));
            tag = BlockTags.create(id);
        }

        @SuppressWarnings("deprecation")
        public boolean matches(Block item) {
            return item.builtInRegistryHolder()
                .is(tag);
        }

        public boolean matches(BlockState stack) {
            return stack.is(tag);
        }

        private static void register() {
        }

    }

    public static void register() {
        BnbItemTags.register();
    }
}
