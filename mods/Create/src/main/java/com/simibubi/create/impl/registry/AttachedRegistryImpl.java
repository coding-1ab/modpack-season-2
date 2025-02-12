package com.simibubi.create.impl.registry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.simibubi.create.api.registry.AttachedRegistry;

// methods are synchronized since registrations can happen during parallel mod loading
public class AttachedRegistryImpl<K, V> implements AttachedRegistry<K, V> {
	private static final Object nullMarker = new Object();

	// all of these have identity semantics
	private final Map<K, V> registrations = new IdentityHashMap<>();
	private final List<Provider<K, V>> providers = new ArrayList<>();
	private final Map<Provider<K, V>, Set<K>> providedKeys = new IdentityHashMap<>();
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
	public synchronized void invalidateProvider(Provider<K, V> provider) {
		Objects.requireNonNull(provider);
		if (!this.providers.contains(provider)) {
			throw new IllegalArgumentException("Cannot invalidate non-registered provider: " + provider);
		}

		// discard all the values the provider has provided
		Set<K> keys = providedKeys.remove(provider);
		if (keys != null) {
			keys.forEach(providedValues::remove);
		}

		// when a provider is invalidated, we need to clear all cached values that evaluated to null, so they can be re-queried
		this.providedValues.values().removeIf(value -> value == nullMarker);
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
		// descendingSet: go in reverse-registration order
		for (Provider<K, V> provider : this.providers) {
			V value = provider.get(object);
			if (value != null) {
				this.providedValues.put(object, value);
				// track which provider provided the value for invalidation
				this.providedKeys.computeIfAbsent(provider, $ -> identityHashSet()).add(object);
				return value;
			}
		}

		// no provider returned non-null
		this.providedValues.put(object, nullMarker());
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T> T nullMarker() {
		return (T) nullMarker;
	}

	private static <T> Set<T> identityHashSet() {
		return Collections.newSetFromMap(new IdentityHashMap<>());
	}
}
