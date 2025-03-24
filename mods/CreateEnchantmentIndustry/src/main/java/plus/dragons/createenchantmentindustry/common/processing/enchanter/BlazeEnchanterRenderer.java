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

import com.mojang.blaze3d.vertex.PoseStack;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockRenderer;

public class BlazeEnchanterRenderer extends BlazeBlockRenderer<BlazeEnchanterBlockEntity> {
    public BlazeEnchanterRenderer(Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(BlazeEnchanterBlockEntity blockEntity, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(blockEntity.getLevel()))
            return;
        super.renderSafe(blockEntity, partialTicks, poseStack, bufferSource, light, overlay);
    }
}
