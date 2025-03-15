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

package plus.dragons.createenchantmentindustry.common.fluids.printer;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import plus.dragons.createdragonsplus.common.recipe.CustomProcessingRecipeParams;
import plus.dragons.createdragonsplus.util.CDPCodecs;
import plus.dragons.createdragonsplus.util.FieldsAssertedNonnullByDefault;

@FieldsAssertedNonnullByDefault
public class PrintingRecipeParams extends CustomProcessingRecipeParams {
    public static final MapCodec<PrintingRecipeParams> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            codec(PrintingRecipeParams::new).forGetter(Function.identity()),
            CDPCodecs.PLAY_SOUND.fieldOf("sound").forGetter(PrintingRecipeParams::getSound)
    ).apply(instance, PrintingRecipeParams::setSound));
    public static final StreamCodec<RegistryFriendlyByteBuf, PrintingRecipeParams> STREAM_CODEC =
            streamCodec(PrintingRecipeParams::new);
    protected static final StreamCodec<ByteBuf, PlaySoundEffect> PLAY_SOUND_STREAM_CODEC =
            ByteBufCodecs.fromCodec(CDPCodecs.PLAY_SOUND);
    protected PlaySoundEffect sound;

    protected PrintingRecipeParams(ResourceLocation id) {
        super(id);
    }

    public PrintingRecipeParams(ResourceLocation id, PlaySoundEffect sound) {
        super(id);
        this.sound = sound;
    }

    protected PlaySoundEffect getSound() {
        return sound;
    }

    protected PrintingRecipeParams setSound(PlaySoundEffect sound) {
        this.sound = sound;
        return this;
    }

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        super.encode(buffer);
        PLAY_SOUND_STREAM_CODEC.encode(buffer, sound);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        super.decode(buffer);
        sound = PLAY_SOUND_STREAM_CODEC.decode(buffer);
    }
}
