package com.simibubi.create.impl.registry;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.foundation.mixin.accessor.StateHolderAccessor;

import net.minecraft.world.level.block.state.StateHolder;

// methods are synchronized since registrations can happen during parallel mod loading
public class SimpleRegistryImpl<K, V> implements SimpleRegistry<K, V> {
	private static final Object nullMarker = new Object();

	private final Map<K, V> registrations = new IdentityHashMap<>();
	private final List<Provider<K, V>> providers = new ArrayList<>();
	private final Map<K, V> providedValues = new IdentityHashMap<>();

	@Override
	public synchronized void register(K object, V value) {
		Objects.requireNonNull(object, "object");
		Objects.requireNonNull(value, "value");

		V existing = this.registrations.get(object);
		if (existing != null) {
			String message = String.format("Tried to register duplicate values for object %s: old=%s, new=%s", object, existing, value);
			throw new IllegalArgumentException(message);
		}

		this.registrations.put(object, value);
	}

	@Override
	public synchronized void registerProvider(Provider<K, V> provider) {
		Objects.requireNonNull(provider);
		if (this.providers.contains(provider)) {
			throw new IllegalArgumentException("Tried to register provider twice: " + provider);
		}

		// add to start of list so it's queried first
		this.providers.add(0, provider);
		provider.onRegister(this);
	}

	@Override
	public synchronized void invalidate() {
		this.providedValues.clear();
	}

	@Override
	@Nullable
	public synchronized V get(K object) {
		if (this.registrations.containsKey(object)) {
			return this.registrations.get(object);
		} else if (this.providedValues.containsKey(object)) {
			V provided = this.providedValues.get(object);
			return provided == nullMarker ? null : provided;
		}

		// no value known, check providers
		// new providers are added to the start, so normal iteration is reverse-registration order
		for (Provider<K, V> provider : this.providers) {
			V value = provider.get(object);
			if (value != null) {
				this.providedValues.put(object, value);
				return value;
			}
		}

		// no provider returned non-null
		this.providedValues.put(object, nullMarker());
		return null;
	}

	@Override
	@Nullable
	@SuppressWarnings("unchecked")
	public synchronized V get(StateHolder<K, ?> state) {
		K owner = ((StateHolderAccessor<K, ?>) state).getOwner();
		return this.get(owner);
	}

	@SuppressWarnings("unchecked")
	private static <T> T nullMarker() {
		return (T) nullMarker;
	}
}
