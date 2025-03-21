/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.registry;

import static plus.dragons.createenchantmentindustry.common.CEICommon.REGISTRATE;

import com.simibubi.create.AllTags.AllItemTags;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class CEIItems {
    public static final ItemEntry<Item> EXPERIENCE_CAKE_BASE = REGISTRATE
            .item("experience_cake_base", Item::new)
            .lang("Cake Base o' Enchanting")
            .tag(AllItemTags.UPRIGHT_ON_BELT.tag)
            .register();
    public static final ItemEntry<Item> EXPERIENCE_CAKE = REGISTRATE
            .item("experience_cake", Item::new)
            .lang("Cake o' Enchanting")
            .properties(prop -> prop
                    .rarity(Rarity.UNCOMMON)
                    .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true))
            .tag(AllItemTags.UPRIGHT_ON_BELT.tag)
            .register();
    public static final DeferredItem<BucketItem> EXPERIENCE_BUCKET = DeferredItem
            .createItem(CEICommon.asResource("experience_bucket"));

    public static void register(IEventBus modBus) {}
}
