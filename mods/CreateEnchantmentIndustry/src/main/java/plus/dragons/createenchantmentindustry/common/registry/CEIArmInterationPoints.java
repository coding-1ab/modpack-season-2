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

import com.simibubi.create.api.registry.CreateRegistries;
import com.simibubi.create.content.kinetics.mechanicalArm.ArmInteractionPointType;
import net.minecraft.core.Holder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import plus.dragons.createenchantmentindustry.common.CEICommon;
import plus.dragons.createenchantmentindustry.common.processing.enchanter.BlazeEnchanterArmInteractionPoint;

public class CEIArmInterationPoints {
    public static final DeferredRegister<ArmInteractionPointType> TYPES = DeferredRegister
            .create(CreateRegistries.ARM_INTERACTION_POINT_TYPE, CEICommon.ID);

    public static final Holder<ArmInteractionPointType> BLAZE_ENCHANTER = TYPES
            .register("blaze_enchanter", BlazeEnchanterArmInteractionPoint.Type::new);

    public static void register(IEventBus modBus) {
        TYPES.register(modBus);
    }
}
