package com.kipti.bnb.registry;

import com.kipti.bnb.CreateBitsnBobs;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;

public class BnbItems {

    public static final ItemEntry<Item> CRUSHED_DEEPSLATE = CreateBitsnBobs.REGISTRATE.item("crushed_deepslate", Item::new)
            .register();

    public static final ItemEntry<Item> CLINKER_SHARD = CreateBitsnBobs.REGISTRATE.item("clinker_shard", Item::new)
            .register();
    
    public static void register() {
    }

}
