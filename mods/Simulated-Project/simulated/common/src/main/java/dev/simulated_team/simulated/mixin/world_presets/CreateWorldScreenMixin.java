package dev.simulated_team.simulated.mixin.world_presets;

import com.mojang.serialization.Lifecycle;
import dev.simulated_team.simulated.content.worldgen.SimulatedWorldPreset;
import dev.simulated_team.simulated.index.SimWorldPresets;
import dev.simulated_team.simulated.mixin_interface.PrimaryLevelDataExtension;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.client.gui.screens.worldselection.WorldCreationUiState;
import net.minecraft.core.Holder;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.dimension.end.EndDragonFight;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {

	@Shadow @Final private WorldCreationUiState uiState;

	@Shadow public abstract WorldCreationUiState getUiState();

	@Inject(method = "createNewWorld", at = @At("HEAD"), remap = false)
	private void simulated$createNewWorld(final PrimaryLevelData.SpecialWorldProperty specialWorldProperty, final LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, final Lifecycle lifecycle, final CallbackInfo ci) {
		final Holder<WorldPreset> preset = this.getUiState().getWorldType().preset();
		final ResourceLocation location = preset.unwrapKey().get().location();
		final SimulatedWorldPreset simPreset = SimWorldPresets.PRESETS.get(location);

		if(simPreset != null) {
			final GameRules gameRules = this.uiState.getGameRules();
			simPreset.modifyGameRules(gameRules);
		}
	}

	@Inject(method = "createNewWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;createWorldOpenFlows()Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, remap = false)
	private void simulated$createNewWorld2(final PrimaryLevelData.SpecialWorldProperty specialWorldProperty, final LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess, final Lifecycle lifecycle, final CallbackInfo ci, final Optional optional, final boolean bl, final WorldCreationContext worldCreationContext, final LevelSettings levelSettings, final WorldData worldData) {
		((PrimaryLevelDataExtension) worldData).setPreset(this.uiState.getWorldType().preset().unwrapKey().get().location());

		if (this.getUiState().getWorldType().preset().is(SimWorldPresets.END_SEA.id())) {
			((PrimaryLevelDataExtension) worldData).setEndDragonFight(new EndDragonFight.Data(false, true, true, false, Optional.empty(), Optional.empty(), Optional.empty()));
		}
	}
}
