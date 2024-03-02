package org.antarcticgardens.cna.content.electricity.battery;

import com.simibubi.create.Create;
import com.simibubi.create.foundation.data.SpecialBlockStateGen;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

#if CNA_FABRIC
import io.github.fabricators_of_create.porting_lib.models.generators.ModelFile;
#else
import net.minecraftforge.client.model.generators.ModelFile;
#endif

public class BatteryBlockStateGen extends SpecialBlockStateGen {
    @Override
    protected int getXRotation(BlockState state) {
        return 0;
    }

    @Override
    protected int getYRotation(BlockState state) {
        return 0;
    }

    @Override
    public <T extends Block> ModelFile getModel(DataGenContext<Block, T> ctx, RegistrateBlockstateProvider prov, BlockState state) {
        boolean top = state.getValue(BatteryBlock.TOP);
        boolean bottom = state.getValue(BatteryBlock.BOTTOM);

        String shape = "middle";

        if (top && bottom)
            shape = "single";
        else if (top)
            shape = "top";
        else if (bottom)
            shape = "bottom";

        return prov.models().getBuilder("battery_" + shape).parent(new ModelFile.UncheckedModelFile(Create.asResource("block/fluid_tank/block_" + shape)))
                .texture("0", prov.modLoc("block/battery_top"))
                .texture("1", prov.modLoc("block/battery_side"));
    }
}
