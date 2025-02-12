package com.simibubi.create.api.registry;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.impl.registry.AttachedRegistryImpl;
import com.simibubi.create.impl.registry.TagProviderImpl;

import net.minecraft.core.Holder;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * A mapping of registered objects to something else.
 * This class is thread-safe, and may be safely used during parallel mod init.
 */
public interface AttachedRegistry<K, V> {
	/**
	 * Register an association between a key and a value.
	 * @throws IllegalArgumentException if the object already has an associated value
	 */
	void register(K object, V value);

	/**
	 * Add a new provider to this registry. For information on providers, see {@link Provider}.
	 * @throws IllegalArgumentException if the provider has already been registered to this registry
	 */
	void registerProvider(Provider<K, V> provider);

	/**
	 * Invalidate the cached values provided by the given provider, so they get re-computed on the next query.
	 * @throws IllegalArgumentException if the provider is not registered to this registry
	 */
	void invalidateProvider(Provider<K, V> provider);

	/**
	 * Query the value associated with the given object. May be null if no association is present.
	 */
	@Nullable
	V get(K object);

	static <K, V> AttachedRegistry<K, V> create() {
		return new AttachedRegistryImpl<>();
	}

	/**
	 * A provider can provide values to the registry in a lazy fashion. When a key does not have an
	 * associated value, all providers will be queried in reverse-registration order.
	 * <p>
	 * The values returned by providers are cached so that repeated queries always return the same value.
	 * To invalidate the cache of a provider, call {@link AttachedRegistry#invalidateProvider(Provider)}.
	 */
	@FunctionalInterface
	interface Provider<K, V> {
		@Nullable
		V get(K object);

		/**
		 * Called by the AttachedRegistry this provider is registered to after it's registered.
		 * This is useful for behavior that should only happen if a provider is actually registered,
		 * such as registering event listeners.
		 */
		default void onRegister(AttachedRegistry<K, V> registry) {
		}

		/**
		 * Create a provider that will return the same value for all entries in a tag.
		 * The Provider will invalidate itself when tags are reloaded.
		 */
		static <K, V> Provider<K, V> forTag(TagKey<K> tag, Function<K, Holder<K>> holderGetter, V value) {
			return new TagProviderImpl<>(tag, holderGetter, value);
		}

		/**
		 * Shortcut for {@link #forTag} when the registry's type is Block.
		 */
		@SuppressWarnings("deprecation")
		static <V> Provider<Block, V> forBlockTag(TagKey<Block> tag, V value) {
			return new TagProviderImpl<>(tag, Block::builtInRegistryHolder, value);
		}

		/**
		 * Shortcut for {@link #forTag} when the registry's type is Item.
		 */
		@SuppressWarnings("deprecation")
		static <V> Provider<Item, V> forItemTag(TagKey<Item> tag, V value) {
			return new TagProviderImpl<>(tag, Item::builtInRegistryHolder, value);
		}
	}
}
