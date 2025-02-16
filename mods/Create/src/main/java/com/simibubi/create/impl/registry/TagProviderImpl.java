package com.simibubi.create.impl.registry;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TagsUpdatedEvent;

public class TagProviderImpl<K, V> implements SimpleRegistry.Provider<K, V> {
	private final TagKey<K> tag;
	private final Function<K, Holder<K>> holderGetter;
	private final V value;

	public TagProviderImpl(TagKey<K> tag, Function<K, Holder<K>> holderGetter, V value) {
		this.tag = tag;
		this.holderGetter = holderGetter;
		this.value = value;
	}

	@Override
	@Nullable
	public V get(K object) {
		Holder<K> holder = this.holderGetter.apply(object);
		return holder.is(this.tag) ? this.value : null;
	}

	@Override
	public void onRegister(SimpleRegistry<K, V> registry) {
		MinecraftForge.EVENT_BUS.addListener((TagsUpdatedEvent event) -> {
			if (event.shouldUpdateStaticData()) {
				registry.invalidate();
			}
		});
	}
}
