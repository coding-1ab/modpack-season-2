package com.simibubi.create.foundation.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.simibubi.create.Create;

import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;

@Mixin(DimensionDataStorage.class)
public abstract class DimensionDataStorageMixin {
	@Shadow
	@Final
	private File dataFolder;

	@Shadow
	public abstract CompoundTag readTagFromDisk(String pName, int pLevelVersion) throws IOException;

	@ModifyReturnValue(method = "readSavedData", at = @At(value = "TAIL"))
	private <T extends SavedData> T create$tryLoadingFromDatOldIfFailedToLoad(T original, Function<CompoundTag, T> loadFunction, String name) {
		// Try loading old data if it's create's SavedData
		if (original == null && name.startsWith("create_")) {
			try {
				File currentFile = new File(dataFolder, name + ".dat");
				File oldFile = new File(dataFolder, name + ".dat_old");
				if (currentFile.exists() && oldFile.exists()) {
					Create.LOGGER.warn("Trying to restore {}.dat from {}.dat_old", name, name);
					CompoundTag compoundtag = readTagFromDisk(name, SharedConstants.getCurrentVersion().getDataVersion().getVersion());
					return loadFunction.apply(compoundtag.getCompound("data"));
				}
			} catch (Exception exception) {
				Create.LOGGER.error("Error restoring from old saved data: {}", name, exception);
			}
		} else {
			return original;
		}

		return null;
	}
}
