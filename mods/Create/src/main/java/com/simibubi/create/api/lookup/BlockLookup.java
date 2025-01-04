package com.simibubi.create.api.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Lookup for objects provided by blocks. Values can either be registered directly
 * or found lazily through providers. Providers are only queried once per block.
 * If they return a value, that value is cached. If they don't, that block is recorded
 * as not having a corresponding value.
 * <br>
 * All providers are expected to be registered synchronously during game init.
 * Adding new ones late is not supported.
 */
public class BlockLookup<T> {
	private final Map<Block, T> map;
	private final Set<Block> blacklist;
	private final List<Provider<T>> providers;

	public BlockLookup(Provider<T> defaultProvider) {
		this.map = new HashMap<>();
		this.blacklist = new HashSet<>();
		this.providers = new ArrayList<>();
		this.registerProvider(defaultProvider);
	}

	@Nullable
	public T find(BlockState state) {
		return this.find(state.getBlock());
	}

	@Nullable
	public T find(Block block) {
		if (this.blacklist.contains(block))
			return null;

		return this.map.computeIfAbsent(block, $ -> {
			for (Provider<T> provider : this.providers) {
				T value = provider.get(block);
				if (value != null) {
					return value;
				}
			}

			this.blacklist.add(block);
			return null;
		});
	}

	public void register(Block block, T type) {
		this.map.put(block, type);
	}

	/**
	 * Register a new provider that will be queried.
	 * Providers are queried in reverse-registration order.
	 */
	public void registerProvider(Provider<T> provider) {
		this.providers.add(0, provider);
	}

	public interface Provider<T> {
		@Nullable
		T get(Block block);
	}
}
