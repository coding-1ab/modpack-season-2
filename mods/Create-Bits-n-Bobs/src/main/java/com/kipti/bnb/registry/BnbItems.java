package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.kipti.bnb.content.cogwheel_chain.item.CogwheelChainItem;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

public class BnbItems {

    public static final ItemEntry<CogwheelChainItem> COGWHEEL_CHAIN_TO_BE_REPLACED = CreateBitsnBobs.REGISTRATE.item("cogwheel_chain_placing_chain", CogwheelChainItem::new)
        .register();

    public static final ItemEntry<Item> CRUSHED_DEEPSLATE = CreateBitsnBobs.REGISTRATE.item("crushed_deepslate", Item::new)
        .register();

    public static final ItemEntry<Item> CALCINATED_FLINT = CreateBitsnBobs.REGISTRATE.item("calcinated_flint", Item::new)
        .register();

    public static void register() {
    }

}
