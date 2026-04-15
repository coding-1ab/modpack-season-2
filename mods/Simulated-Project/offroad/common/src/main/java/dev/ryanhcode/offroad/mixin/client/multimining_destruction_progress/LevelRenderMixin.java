package dev.ryanhcode.offroad.mixin.client.multimining_destruction_progress;

import com.google.common.collect.Sets;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.ryanhcode.offroad.handlers.client.MultiMiningBlockDestructionProgress;
import dev.ryanhcode.offroad.handlers.client.MultiMiningClientHandler;
import dev.ryanhcode.offroad.mixin_interface.level_renderer.MultiMiningDestructionExtension;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.BlockDestructionProgress;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

@Mixin(LevelRenderer.class)
public abstract class LevelRenderMixin implements MultiMiningDestructionExtension {

    @Shadow
    @Final
    private Int2ObjectMap<BlockDestructionProgress> destroyingBlocks;

    @Shadow
    @Final
    private Long2ObjectMap<SortedSet<BlockDestructionProgress>> destructionProgress;

    @Shadow
    private int ticks;

    /**
     * we need to manually handle this as our multimining progress does not have a tree set associated with it. It's just a holder for other progresses...
     */
    @WrapMethod(method = "removeProgress")
    private void offroad$handleMultiMiningProgressRemoval(final BlockDestructionProgress progress, final Operation<Void> original) {
        if (progress instanceof final MultiMiningBlockDestructionProgress mmProgress) {
            if (!mmProgress.otherProgresses.isEmpty()) {
                for (final BlockDestructionProgress innerProgress : mmProgress.otherProgresses.values()) {
                    original.call(innerProgress);
                }

                mmProgress.otherProgresses.clear();
            }
        } else {
            original.call(progress);
        }
    }

    @Override
    public void offroad$manuallyAddMultiDestructionProgress(final int id, final Map<BlockPos, MultiMiningClientHandler.ClientBlockBreakingData> clientData) {
        final BlockDestructionProgress multiMineBlockHolder = this.destroyingBlocks.computeIfAbsent(id, $ -> new MultiMiningBlockDestructionProgress(id, BlockPos.ZERO));
        if (multiMineBlockHolder instanceof final MultiMiningBlockDestructionProgress mmProgress) {
            // update already present block breaking progresses, and add new ones
            for (final Map.Entry<BlockPos, MultiMiningClientHandler.ClientBlockBreakingData> clientSet : clientData.entrySet()) {
                mmProgress.otherProgresses.computeIfAbsent(clientSet.getKey(), pos -> new BlockDestructionProgress(id, clientSet.getKey()))
                        .setProgress((byte) clientSet.getValue().destroyProgress);
            }

            // check ALL progresses and remove invalid ones
            final Iterator<Map.Entry<BlockPos, BlockDestructionProgress>> iter = mmProgress.otherProgresses.entrySet().iterator();
            while (iter.hasNext()) {
                // remove invalid progresses
                final BlockDestructionProgress blockDestructionProgress = iter.next().getValue();
                if (blockDestructionProgress.getProgress() < 0 || blockDestructionProgress.getProgress() >= 10) {
                    this.offroad$removeProgress(blockDestructionProgress);
                    iter.remove();
                    continue;
                }

                this.destructionProgress.computeIfAbsent(blockDestructionProgress.getPos().asLong(), l -> Sets.newTreeSet())
                        .add(blockDestructionProgress);
            }

            // update our timer to make sure we keep voided appropriately
            mmProgress.updateTick(this.ticks);
        }
    }

    @Unique
    private void offroad$removeProgress(final BlockDestructionProgress innerProgress) {
        final long progressID = innerProgress.getPos().asLong();
        final SortedSet<BlockDestructionProgress> progressSet = this.destructionProgress.get(progressID);

        // our progress set can be null
        if (progressSet != null) {
            progressSet.remove(innerProgress);

            if (progressSet.isEmpty()) {
                this.destructionProgress.remove(progressID);
            }
        }
    }
}