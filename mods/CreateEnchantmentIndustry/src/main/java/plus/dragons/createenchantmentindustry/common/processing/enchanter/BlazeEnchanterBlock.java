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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.entity.BlockEntityType;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlock;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlock;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlockEntities;

public class BlazeEnchanterBlock extends BlazeExperienceBlock<BlazeEnchanterBlockEntity> {
    public BlazeEnchanterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BlazeBlock<BlazeEnchanterBlockEntity>> codec() {
        return simpleCodec(BlazeEnchanterBlock::new);
    }

    @Override
    public Class<BlazeEnchanterBlockEntity> getBlockEntityClass() {
        return BlazeEnchanterBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends BlazeEnchanterBlockEntity> getBlockEntityType() {
        return CEIBlockEntities.BLAZE_ENCHANTER.get();
    }
}
