package dev.ryanhcode.sable.neoforge.mixin.sublevel_render.vanilla;

import dev.ryanhcode.sable.neoforge.mixinterface.sublevel_render.vanilla.ModelDataManagerExtension;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelDataManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ModelDataManager.class)
public class ModelDataManagerMixin implements ModelDataManagerExtension {

    @Shadow
    @Final
    private Long2ObjectMap<Set<BlockPos>> needModelDataRefresh;

    @Shadow
    @Final
    private Long2ObjectMap<Long2ObjectMap<ModelData>> modelDataCache;

    @Override
    public void sable$removeModelData(final BlockPos pos) {
        final int sectionX = pos.getX() >> SectionPos.SECTION_BITS;
        final int sectionY = pos.getY() >> SectionPos.SECTION_BITS;
        final int sectionZ = pos.getZ() >> SectionPos.SECTION_BITS;
        final long section = SectionPos.asLong(sectionX, sectionY, sectionZ);
        this.needModelDataRefresh.remove(section);
        this.modelDataCache.remove(section);
    }
}
