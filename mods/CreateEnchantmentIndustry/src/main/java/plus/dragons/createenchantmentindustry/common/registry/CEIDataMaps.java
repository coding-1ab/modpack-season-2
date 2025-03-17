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

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataMapProvider;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.fluids.experience.ExperienceRatio;

public class CEIDataMaps {
    public static final DataMapType<Fluid, ExperienceRatio> FLUID_EXPERIENCE_RATIO = DataMapType
            .builder(CEICommon.asResource("experience_ratio"), Registries.FLUID, ExperienceRatio.CODEC)
            .synced(ExperienceRatio.CODEC, true)
            .build();

    public static void register(IEventBus modBus) {
        modBus.register(CEIDataMaps.class);
        REGISTRATE.addDataGenerator(ProviderType.DATA_MAP, CEIDataMaps::generate);
    }

    @SubscribeEvent
    public static void register(final RegisterDataMapTypesEvent event) {
        event.register(FLUID_EXPERIENCE_RATIO);
    }

    public static void generate(RegistrateDataMapProvider provider) {
        provider.builder(FLUID_EXPERIENCE_RATIO)
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
    }
}
