package com.simibubi.create.api.contraption.storage.item.registrate;

import com.simibubi.create.api.contraption.storage.MountedStorageTypeRegistries;
import com.simibubi.create.api.contraption.storage.item.MountedItemStorageType;
import com.simibubi.create.api.registry.AttachedRegistry;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.nullness.NonnullType;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class MountedItemStorageTypeBuilder<T extends MountedItemStorageType<?>, P> extends AbstractBuilder<MountedItemStorageType<?>, T, P, MountedItemStorageTypeBuilder<T, P>> {
	private final T type;

	public MountedItemStorageTypeBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, T type) {
		super(owner, parent, name, callback, MountedStorageTypeRegistries.ITEMS);
		this.type = type;
	}

	public MountedItemStorageTypeBuilder<T, P> registerTo(Block block) {
		MountedStorageTypeRegistries.ITEM_STORAGES.register(block, this.type);
		return this;
	}

	public MountedItemStorageTypeBuilder<T, P> registerTo(TagKey<Block> tag) {
		MountedStorageTypeRegistries.ITEM_STORAGES.registerProvider(AttachedRegistry.Provider.forBlockTag(tag, this.type));
		return this;
	}

	@Override
	@NonnullType
	protected T createEntry() {
		return this.type;
	}
}
