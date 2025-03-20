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

import com.mojang.serialization.Codec;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataMapProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceRatio;

public class CEIDataMaps {
    public static final DataMapType<Fluid, ExperienceRatio> EXPERIENCE_RATIO = DataMapType
            .builder(CEICommon.asResource("experience_ratio"), Registries.FLUID, ExperienceRatio.CODEC)
            .synced(ExperienceRatio.CODEC, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_ADDRESS_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/address_ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_COPY_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/copy_ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_CUSTOM_NAME_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/custom_name_ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();
    public static final DataMapType<Fluid, Style> PRINTING_CUSTOM_NAME_STYLE = DataMapType
            .builder(CEICommon.asResource("printing/custom_name_style"), Registries.FLUID, Style.Serializer.CODEC)
            .synced(Style.Serializer.CODEC, true)
            .build();
    public static final DataMapType<Fluid, Integer> PRINTING_WRITTEN_BOOK_INGREDIENT = DataMapType
            .builder(CEICommon.asResource("printing/written_book_ingredient"), Registries.FLUID, ExtraCodecs.POSITIVE_INT)
            .synced(Codec.INT, true)
            .build();

    public static void register(IEventBus modBus) {
        modBus.register(CEIDataMaps.class);
        REGISTRATE.addDataGenerator(ProviderType.DATA_MAP, CEIDataMaps::generate);
    }

    @SubscribeEvent
    public static void register(final RegisterDataMapTypesEvent event) {
        event.register(EXPERIENCE_RATIO);
        event.register(PRINTING_ADDRESS_INGREDIENT);
        event.register(PRINTING_COPY_INGREDIENT);
        event.register(PRINTING_CUSTOM_NAME_INGREDIENT);
        event.register(PRINTING_CUSTOM_NAME_STYLE);
        event.register(PRINTING_WRITTEN_BOOK_INGREDIENT);
    }

    public static void generate(RegistrateDataMapProvider provider) {
        provider.builder(EXPERIENCE_RATIO)
                .add(CEIFluids.EXPERIENCE,
                        new ExperienceRatio(1, true), false)
                .add(ResourceLocation.fromNamespaceAndPath("cofh_core", "experience"),
                        new ExperienceRatio(25, true), false,
                        new ModLoadedCondition("cofh_core"))
                .add(ResourceLocation.fromNamespaceAndPath("cyclic", "xpjuice"),
                        new ExperienceRatio(20, true), false,
                        new ModLoadedCondition("cyclic"))
                .add(ResourceLocation.fromNamespaceAndPath("enderio", "xpjuice"),
                        new ExperienceRatio(20, true), false,
                        new ModLoadedCondition("enderio"))
                .add(ResourceLocation.fromNamespaceAndPath("industrialforegoing", "essence"),
                        new ExperienceRatio(20, true), false,
                        new ModLoadedCondition("industrialforegoing"))
                .add(ResourceLocation.fromNamespaceAndPath("mob_grinding_utils", "fluid_xp"),
                        new ExperienceRatio(20, true), false,
                        new ModLoadedCondition("mob_grinding_utils"))
                .add(ResourceLocation.fromNamespaceAndPath("industrialforegoing", "essence"),
                        new ExperienceRatio(20, true), false,
                        new ModLoadedCondition("industrialforegoing"))
                .add(ResourceLocation.fromNamespaceAndPath("pneumaticcraft", "memory_essence"),
                        new ExperienceRatio(20, true), false,
                        new ModLoadedCondition("pneumaticcraft"))
                .add(ResourceLocation.fromNamespaceAndPath("reliquary", "xp_juice_still"),
                        new ExperienceRatio(20, true), false,
                        new ModLoadedCondition("reliquary"))
                .add(ResourceLocation.fromNamespaceAndPath("sophisticatedcore", "xp_still"),
                        new ExperienceRatio(20, true), false,
                        new ModLoadedCondition("sophisticatedcore"));
        var blackDye = CDPFluids.COMMON_TAGS.dyesByColor.get(DyeColor.BLACK);
        provider.builder(PRINTING_COPY_INGREDIENT)
                .add(blackDye, 10, false);
        provider.builder(PRINTING_CUSTOM_NAME_INGREDIENT)
                .add(CEIFluids.EXPERIENCE, 10, false)
                .add(CDPFluids.COMMON_TAGS.dyes, 250, false);
        var customNameStyles = provider.builder(PRINTING_CUSTOM_NAME_STYLE);
        CDPFluids.COMMON_TAGS.dyesByColor.forEach((color, tag) -> customNameStyles
                .add(tag, Style.EMPTY.withColor(color.getTextColor()), false));
        provider.builder(PRINTING_ADDRESS_INGREDIENT)
                .add(blackDye, 10, false);
        provider.builder(PRINTING_WRITTEN_BOOK_INGREDIENT)
                .add(blackDye, 10, false);
    }
}
