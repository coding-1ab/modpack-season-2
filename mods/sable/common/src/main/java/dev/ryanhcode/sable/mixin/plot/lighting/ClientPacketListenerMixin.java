package dev.ryanhcode.sable.mixin.plot.lighting;

import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.api.sublevel.ClientSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {

    @Redirect(method = "handleLevelChunkWithLight", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientLevel;queueLightUpdate(Ljava/lang/Runnable;)V"))
    private void sable$queueLightData(final ClientLevel level, final Runnable runnable, @Local(argsOnly = true) final ClientboundLevelChunkWithLightPacket packet) {
        final ClientSubLevelContainer container = SubLevelContainer.getContainer(level);

        if (container.inBounds(packet.getX(), packet.getZ())) {
            runnable.run();
            level.getLightEngine().runLightUpdates();
            return;
        }

        level.queueLightUpdate(runnable);
    }

}
