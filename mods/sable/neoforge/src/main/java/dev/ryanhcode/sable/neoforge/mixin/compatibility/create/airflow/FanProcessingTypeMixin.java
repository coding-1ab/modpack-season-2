package dev.ryanhcode.sable.neoforge.mixin.compatibility.create.airflow;

import com.simibubi.create.content.kinetics.fan.processing.FanProcessingType;
import com.simibubi.create.content.kinetics.fan.processing.FanProcessingTypeRegistry;
import dev.ryanhcode.sable.ActiveSableCompanion;
import dev.ryanhcode.sable.Sable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(FanProcessingType.class)
public interface FanProcessingTypeMixin {

    /**
     * @author Cyvack
     * @reason Easier implementation for subLevel compatibility for fans. Might cause issues with addons if they decide to overwrite this method as well.
     */
    @Overwrite
    static FanProcessingType getAt(final Level level, final BlockPos pos) {
        ActiveSableCompanion helper = Sable.HELPER;
        return helper.runIncludingSubLevels(level, pos.getCenter(), true, helper.getContaining(level, pos), (subLevel, relativePos) -> {
            for (final FanProcessingType type : FanProcessingTypeRegistry.SORTED_TYPES_VIEW) {
                if (type != null && type.isValidAt(level, relativePos)) {
                    return type;
                }
            }

            return null;
        });
    }

}
