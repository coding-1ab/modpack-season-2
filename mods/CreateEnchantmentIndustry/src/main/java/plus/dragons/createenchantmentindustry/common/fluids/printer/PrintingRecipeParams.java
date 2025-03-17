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

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import com.simibubi.create.foundation.fluid.FluidIngredient;
import io.netty.buffer.ByteBuf;
import java.util.function.Function;
import net.createmod.catnip.codecs.stream.CatnipStreamCodecBuilders;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.effects.PlaySoundEffect;
import plus.dragons.createdragonsplus.common.recipe.CustomProcessingRecipeParams;
import plus.dragons.createdragonsplus.util.CDPCodecs;
import plus.dragons.createdragonsplus.util.FieldsAssertedNonnullByDefault;

@FieldsAssertedNonnullByDefault
public class PrintingRecipeParams extends CustomProcessingRecipeParams {
    public static final MapCodec<PrintingRecipeParams> CODEC = RecordCodecBuilder.<PrintingRecipeParams>mapCodec(
            instance -> instance.group(
                    codec(PrintingRecipeParams::new).forGetter(Function.identity()),
                    CDPCodecs.PLAY_SOUND.fieldOf("sound").forGetter(PrintingRecipeParams::getSound)
            ).apply(instance, PrintingRecipeParams::setSound)
    ).validate(PrintingRecipeParams::validate);
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

    protected DataResult<PrintingRecipeParams> validate() {
        if (ingredients.size() != 2)
            return DataResult.error(() -> "Printing recipe must have 2 item inputs, [0]: input, [1]: template");
        if (fluidIngredients.size() != 1)
            return DataResult.error(() -> "Printing recipe must have 1 fluid input");
        return DataResult.success(this);
    }

    @Override
    protected void encode(RegistryFriendlyByteBuf buffer) {
        CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).encode(buffer, ingredients);
        CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).encode(buffer, results);
        CatnipStreamCodecBuilders.nonNullList(FluidIngredient.STREAM_CODEC).encode(buffer, fluidIngredients);
        PLAY_SOUND_STREAM_CODEC.encode(buffer, sound);
    }

    @Override
    protected void decode(RegistryFriendlyByteBuf buffer) {
        ingredients = CatnipStreamCodecBuilders.nonNullList(Ingredient.CONTENTS_STREAM_CODEC).decode(buffer);
        results = CatnipStreamCodecBuilders.nonNullList(ProcessingOutput.STREAM_CODEC).decode(buffer);
        fluidIngredients = CatnipStreamCodecBuilders.nonNullList(FluidIngredient.STREAM_CODEC).decode(buffer);
        sound = PLAY_SOUND_STREAM_CODEC.decode(buffer);
    }
}
