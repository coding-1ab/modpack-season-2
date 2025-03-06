package com.simibubi.create.content.processing.recipe;

import java.util.Random;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.createmod.catnip.data.Pair;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ProcessingOutput {

	public static final ProcessingOutput EMPTY = new ProcessingOutput(ItemStack.EMPTY, 1);

	public static final StreamCodec<RegistryFriendlyByteBuf, ProcessingOutput> STREAM_CODEC = StreamCodec.composite(
		ByteBufCodecs.registry(Registries.ITEM), i -> i.item,
		ByteBufCodecs.INT, i -> i.count,
		DataComponentPatch.STREAM_CODEC, i -> i.patch,
		ByteBufCodecs.FLOAT, i -> i.chance,
		ProcessingOutput::new
	);

	private static final Random r = new Random();
	private final Item item;
	private final int count;
	private final DataComponentPatch patch;
	private final float chance;

	private ResourceLocation compatDatagenOutput;

	public ProcessingOutput(ItemStack stack, float chance) {
		this(stack.getItem(), stack.getCount(), stack.getComponentsPatch(), chance);
	}

	public ProcessingOutput(Item item, int count, float chance) {
		this(item, count, DataComponentPatch.EMPTY, chance);
	}

	public ProcessingOutput(Item item, int count, DataComponentPatch patch, float chance) {
		this.item = item;
		this.count = count;
		this.patch = patch;
		this.chance = chance;
	}

	public ProcessingOutput(ResourceLocation item, int count, float chance) {
		this.item = Items.AIR;
		this.compatDatagenOutput = item;
		this.count = count;
		this.patch = DataComponentPatch.EMPTY;
		this.chance = chance;
	}

	public ItemStack getStack() {
		return new ItemStack(compatDatagenOutput != null ? BuiltInRegistries.ITEM.get(compatDatagenOutput) : item, count);
	}

	public float getChance() {
		return chance;
	}

	public ItemStack rollOutput() {
		int outputAmount = count;
		for (int roll = 0; roll < count; roll++)
			if (r.nextFloat() > chance)
				outputAmount--;
		if (outputAmount == 0)
			return ItemStack.EMPTY;
		ItemStack out = item.getDefaultInstance();
		out.setCount(outputAmount);
		return out;
	}

	private static final Codec<Either<ItemStack, Pair<ResourceLocation, Integer>>> ITEM_CODEC_OLD = Codec.either(
		ItemStack.SINGLE_ITEM_CODEC,
		ResourceLocation.CODEC.comapFlatMap(
			loc -> DataResult.error(() -> "Compat cannot be deserialized"),
			Pair::getFirst
		)
	);

	public static final Codec<ProcessingOutput> CODEC_OLD = RecordCodecBuilder.create(i -> i.group(
		ITEM_CODEC_OLD.fieldOf("item").forGetter(s -> s.compatDatagenOutput != null ? Either.right(Pair.of(s.compatDatagenOutput, s.count)) : Either.left(s.item.getDefaultInstance())),
		Codec.INT.optionalFieldOf("count", 1).forGetter(s -> s.count),
		Codec.FLOAT.optionalFieldOf("chance", 1F).forGetter(s -> s.chance)
	).apply(i, (item, count, chance) -> {
		return item.map(
			stack -> new ProcessingOutput(stack.getItem(), count, chance),
			compat -> new ProcessingOutput(compat.getFirst(), compat.getSecond(), chance)
		);
	}));

	private static final Codec<Either<Item, ResourceLocation>> ITEM_CODEC = Codec.either(
		BuiltInRegistries.ITEM.byNameCodec(),
		ResourceLocation.CODEC
	);

	public static final Codec<ProcessingOutput> CODEC_NEW = RecordCodecBuilder.create(i -> i.group(
		ITEM_CODEC.fieldOf("id").forGetter(s -> {
			if (s.compatDatagenOutput != null)
				return Either.right(s.compatDatagenOutput);
			return Either.left(s.item);
		}),
		DataComponentPatch.CODEC.optionalFieldOf("components", DataComponentPatch.EMPTY).forGetter(s -> s.patch),
		Codec.INT.optionalFieldOf("count", 1).forGetter(s -> s.count),
		Codec.FLOAT.optionalFieldOf("chance", 1F).forGetter(s -> s.chance)
	).apply(i, (item, components, count, chance) -> item.map(
		stack -> new ProcessingOutput(stack, count, components, chance),
		compat -> new ProcessingOutput(compat, count, chance)
	)));

	public static final Codec<ProcessingOutput> CODEC = Codec.withAlternative(CODEC_NEW, CODEC_OLD);

}
