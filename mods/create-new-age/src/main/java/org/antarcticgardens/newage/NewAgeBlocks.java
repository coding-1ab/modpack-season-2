package org.antarcticgardens.newage;

import com.simibubi.create.content.kinetics.BlockStressDefaults;
import com.tterrag.registrate.util.entry.BlockEntry;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.antarcticgardens.newage.content.carbonbrushes.CarbonBrushesBlock;
import org.antarcticgardens.newage.content.energiser.EnergiserBlock;

import static org.antarcticgardens.newage.CreateNewAge.REGISTRATE;

public class NewAgeBlocks {
    static {
        REGISTRATE.defaultCreativeTab(CreateNewAge.CREATIVE_TAB_KEY);
    }

    public static final BlockEntry<EnergiserBlock> ENERGISER_T1 =
            REGISTRATE.block("energiser_t1", EnergiserBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .transform(BlockStressDefaults.setImpact(2.0))
                    .simpleItem()
                    .register();

    public static final BlockEntry<CarbonBrushesBlock> CARBON_BRUSHES =
            REGISTRATE.block("carbon_brushes", CarbonBrushesBlock::new)
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .simpleItem()
                    .register();

    public static void load() {  }
}
