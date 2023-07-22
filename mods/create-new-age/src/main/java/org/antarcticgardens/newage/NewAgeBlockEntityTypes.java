package org.antarcticgardens.newage;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import org.antarcticgardens.newage.content.carbonbrushes.CarbonBrushesBlockEntity;
import org.antarcticgardens.newage.content.carbonbrushes.CarbonBrushesInstance;
import org.antarcticgardens.newage.content.carbonbrushes.CarbonBrushesRenderer;
import org.antarcticgardens.newage.content.energiser.EnergiserInstance;
import org.antarcticgardens.newage.content.energiser.EnergiserRenderer;
import org.antarcticgardens.newage.content.energiser.EnergiserBlockEntity;
import org.antarcticgardens.newage.content.heat.heatpipe.HeatPipeBlockEntity;

import static org.antarcticgardens.newage.CreateNewAge.REGISTRATE;

public class NewAgeBlockEntityTypes {
    public static final BlockEntityEntry<EnergiserBlockEntity> ENERGISER_T1 = REGISTRATE
            .blockEntity("energiser_t1", EnergiserBlockEntity::newTier1)
            .instance(() -> EnergiserInstance::new)
            .validBlocks(NewAgeBlocks.ENERGISER_T1)
            .renderer(() -> EnergiserRenderer::new)
            .register();

    public static final BlockEntityEntry<CarbonBrushesBlockEntity> CARBON_BRUSHES = REGISTRATE
            .blockEntity("carbon_brushes", CarbonBrushesBlockEntity::new)
            .instance(() -> CarbonBrushesInstance::new)
            .validBlocks(NewAgeBlocks.CARBON_BRUSHES)
            .renderer(() -> CarbonBrushesRenderer::new)
            .register();

    public static final BlockEntityEntry<HeatPipeBlockEntity> HEAT_PIPE = REGISTRATE
            .blockEntity("heat_pipe", HeatPipeBlockEntity::new)
            .validBlocks(NewAgeBlocks.HEAT_PIPE)
            .register();

    public static void load() {  }
}
