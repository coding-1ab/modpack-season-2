package org.antarcticgardens.newage.content.heat.solarheatingplate;

import com.simibubi.create.content.equipment.wrench.IWrenchable;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.NewAgeBlockEntityTypes;
import org.jetbrains.annotations.Nullable;

import static org.antarcticgardens.newage.content.heat.heatpipe.HeatPipeBlock.massPipe;

public class SolarHeatingPlateBlock extends Block implements EntityBlock, IWrenchable {
    private final BlockEntityEntry<?> entry;

    public SolarHeatingPlateBlock(Properties properties, BlockEntityEntry<?> entry) {
        super(properties.strength(4.0f));
        this.entry = entry;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return entry.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        final int on = massPipe;
        massPipe++;
        if (massPipe >= 20) {
            massPipe = 0;
        }
        return (level1, blockPos, blockState, blockEntity) -> {
            if ((level1.getGameTime() + on) % 20 != 0 || !(blockEntity instanceof SolarHeatingPlateBlockEntity ent)) return;
            ent.tick(blockPos, level1, blockState);
        };
    }

    public static Block createAdvanced(Properties properties) {
        return  new SolarHeatingPlateBlock(properties, NewAgeBlockEntityTypes.ADVANCED_SOLAR_HEATING_PLATE);
    }

    public static Block createBasic(Properties properties) {
        return  new SolarHeatingPlateBlock(properties, NewAgeBlockEntityTypes.BASIC_SOLAR_HEATING_PLATE);
    }

}
