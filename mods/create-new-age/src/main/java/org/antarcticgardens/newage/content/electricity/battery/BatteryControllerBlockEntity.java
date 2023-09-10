package org.antarcticgardens.newage.content.electricity.battery;

import com.simibubi.create.content.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.utility.Lang;
import earth.terrarium.botarium.common.energy.base.BotariumEnergyBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.antarcticgardens.newage.NewAgeTags;
import org.antarcticgardens.newage.tools.StringFormattingTool;

import java.util.List;

public class BatteryControllerBlockEntity extends BlockEntity implements BotariumEnergyBlock<BatteryEnergyContainer>, IHaveGoggleInformation {
    private final BatteryEnergyContainer container = new BatteryEnergyContainer();
    
    public BatteryControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    
    protected void tick() {
        if (getLevel() == null)
            return;
        
        BlockEntity be = getLevel().getBlockEntity(getBlockPos().offset(0, -1, 0));
        container.update(this);
        
        if (be instanceof FluidTankBlockEntity tank) {
            FluidTankBlockEntity controller = tank.getControllerBE();
            if (container.getTank() != controller)
                container.setTank(controller);
        } else if (container.getTank() != null) 
            container.setTank(null);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        Lang.translate("tooltip.create_new_age.energy_stats")
                .style(ChatFormatting.WHITE).forGoggles(tooltip);

        Lang.translate("tooltip.create_new_age.energy_stored")
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip);
        Lang.translate("tooltip.create_new_age.energy_storage", StringFormattingTool.formatLong(container.getClientEnergy()), StringFormattingTool.formatLong(container.getMaxCapacity()))
                .style(ChatFormatting.AQUA).forGoggles(tooltip, 1);
        
        if (container.getTank() == null || !container.getTank().getTankInventory().getFluid().getFluid().is(NewAgeTags.ELECTROLYTE_TAG)) {
            Lang.builder()
                    .space()
                    .forGoggles(tooltip);
            
            Lang.translate("tooltip.create_new_age.attention")
                    .style(ChatFormatting.GOLD)
                    .forGoggles(tooltip);
            
            Component hint = Lang.translateDirect("tooltip.create_new_age.battery_issue");
            List<Component> cutString = TooltipHelper.cutTextComponent(hint, TooltipHelper.Palette.GRAY_AND_WHITE);
            for (Component component : cutString)
                Lang.builder()
                        .add(component
                                .copy())
                        .forGoggles(tooltip);
        }
        
        return true;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public BatteryEnergyContainer getEnergyStorage() {
        return container;
    }
}
