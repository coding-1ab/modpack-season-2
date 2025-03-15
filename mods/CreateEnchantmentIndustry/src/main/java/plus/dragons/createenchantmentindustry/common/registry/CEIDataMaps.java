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

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
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
    }

    @SubscribeEvent
    public static void register(final RegisterDataMapTypesEvent event) {
        event.register(FLUID_EXPERIENCE_RATIO);
    }
}
