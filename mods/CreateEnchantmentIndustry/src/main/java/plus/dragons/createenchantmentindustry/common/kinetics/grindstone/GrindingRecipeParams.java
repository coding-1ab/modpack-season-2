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

package plus.dragons.createenchantmentindustry.common.kinetics.grindstone;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import plus.dragons.createdragonsplus.common.recipe.CustomProcessingRecipeParams;

public class GrindingRecipeParams extends CustomProcessingRecipeParams {
    public static final MapCodec<GrindingRecipeParams> CODEC = codec(GrindingRecipeParams::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, GrindingRecipeParams> STREAM_CODEC = streamCodec(GrindingRecipeParams::new);

    protected GrindingRecipeParams(ResourceLocation id) {
        super(id);
    }
}
