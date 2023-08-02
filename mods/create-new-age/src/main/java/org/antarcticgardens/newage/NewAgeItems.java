package org.antarcticgardens.newage;

import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.world.item.Item;


import static org.antarcticgardens.newage.CreateNewAge.REGISTRATE;

public class NewAgeItems {
    public static final ItemEntry<Item> NUCLEAR_FUEL =
            REGISTRATE.item("nuclear_fuel", Item::new)
                    .register();

    public static final ItemEntry<Item> THORIUM =
            REGISTRATE.item("thorium", Item::new)
                    .register();

    public static void load() {  }
}