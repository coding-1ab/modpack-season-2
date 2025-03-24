/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createenchantmentindustry.common.fluids.experience;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.foundation.utility.CreateLang;
import java.util.List;
import java.util.function.Consumer;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import plus.dragons.createdragonsplus.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createdragonsplus.common.fluids.tank.FluidTankBehaviour;
import plus.dragons.createdragonsplus.common.processing.blaze.BlazeBlockEntity;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

@FieldsNullabilityUnknownByDefault
public abstract class BlazeExperienceBlockEntity extends BlazeBlockEntity implements IHaveGoggleInformation {
    protected final LerpedFloat headAnimation = LerpedFloat.linear();
    protected final LerpedFloat headAngle = LerpedFloat.angular();
    private boolean isCreative;
    protected FluidTankBehaviour tanks;

    public BlazeExperienceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback);

    protected abstract ConfigurableFluidTank createSpecialTank(Consumer<FluidStack> fluidUpdateCallback);

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tanks = new FluidTankBehaviour(this, List.of(this::createNormalTank, this::createSpecialTank), false);
        behaviours.add(tanks);
    }

    @Override
    public boolean isCreative() {
        return isCreative;
    }

    @Override
    public HeatLevel getHeatLevel() {
        if (getSpecialExperience() > 0)
            return HeatLevel.SEETHING;
        double experience = getNormalExperience();
        if (experience > 0) {
            boolean lowPercent = experience / getNormalTank().getCapacity() < 0.0125;
            return lowPercent ? HeatLevel.FADING : HeatLevel.KINDLED;
        }
        return HeatLevel.SMOULDERING;
    }

    @Override
    protected void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean("isCreative", isCreative);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        isCreative = compound.getBoolean("isCreative");
    }

    public SmartFluidTank getNormalTank() {
        return tanks.getHandlers()[0];
    }

    public SmartFluidTank getSpecialTank() {
        return tanks.getHandlers()[1];
    }

    public int getNormalExperience() {
        return getNormalTank().getFluid().getAmount();
    }

    public int getSpecialExperience() {
        return getSpecialTank().getFluid().getAmount();
    }

    public int getTotalExperience() {
        return getNormalExperience() + getSpecialExperience();
    }

    public boolean consumeExperience(int amount, boolean special, boolean simulate) {
        var fluid = ExperienceHelper.getExperienceFluid(amount);
        var tank = special ? getSpecialTank() : tanks.getCapability();
        var drained = tank.drain(fluid, FluidAction.SIMULATE);
        if (drained.getAmount() != amount)
            return false;
        if (!simulate)
            tank.drain(fluid, FluidAction.EXECUTE);
        return true;
    }

    public boolean applyExperienceFuel(ExperienceFuel fuel, boolean forceOverflow, boolean simulate) {
        assert level != null;
        if (isCreative)
            return false;
        boolean special = fuel.special();
        var tank = special ? getSpecialTank() : getNormalTank();
        if (!(tank instanceof ConfigurableFluidTank configurableTank)) {
            return false;
        }
        var fluid = configurableTank.getFluid();
        if (!fluid.isEmpty() && !fluid.is(CEIFluids.EXPERIENCE))
            return false;
        int experience = fuel.experience();
        var experienceFluid = ExperienceHelper.getExperienceFluid(experience);
        int fill = configurableTank.fill(experienceFluid, FluidAction.SIMULATE, true);
        if (fill == 0)
            return false;
        else if (fill != experience && !forceOverflow)
            return false;
        if (simulate)
            return true;
        if (level.isClientSide)
            spawnParticleBurst(special);
        configurableTank.fill(experienceFluid, FluidAction.EXECUTE, true);

        HeatLevel heat = getHeatLevelFromBlock();
        playSound();
        updateBlockState();

        if (heat != getHeatLevelFromBlock())
            level.playSound(null, worldPosition, SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS,
                    .125f + level.random.nextFloat() * .125f,
                    1.15f - level.random.nextFloat() * .25f);
        notifyUpdate();
        return true;
    }

    public void applyCreativeFuel() {
        assert level != null;
        isCreative = true;
        HeatLevel next = getHeatLevelFromBlock().nextActiveLevel();
        if (level.isClientSide) {
            spawnParticleBurst(next.isAtLeast(HeatLevel.SEETHING));
            return;
        }
        playSound();
        if (next == HeatLevel.FADING)
            next = next.nextActiveLevel();
        switch (next) {
            case KINDLED -> {
                int capacity = getNormalTank().getCapacity();
                tanks.setTank(0, callback -> new CreativeSmartFluidTank(capacity, callback));
                getNormalTank().setFluid(ExperienceHelper.getExperienceFluid(capacity));
            }
            case SEETHING -> {
                int capacity = getSpecialTank().getCapacity();
                tanks.setTank(1, callback -> new CreativeSmartFluidTank(capacity, callback));
                getSpecialTank().setFluid(ExperienceHelper.getExperienceFluid(capacity));
            }
            default -> {
                tanks.setTank(0, this::createNormalTank);
                tanks.setTank(1, this::createSpecialTank);
            }
        }
        setBlockHeat(next);
        notifyUpdate();
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        LangBuilder mb = CreateLang.translate("generic.unit.millibuckets");
        CreateLang.translate("gui.goggles.fluid_container")
                .forGoggles(tooltip);
        CreateLang.builder().add(CEIFluids.EXPERIENCE.getType().getDescription())
                .style(ChatFormatting.GRAY)
                .forGoggles(tooltip, 1);
        boolean speical = false;
        for (var tank : tanks.getHandlers()) {
            CreateLang.builder()
                    .add(CreateLang.number(tank.getFluid().getAmount())
                            .add(mb)
                            .style(speical ? ChatFormatting.BLUE : ChatFormatting.GOLD))
                    .text(ChatFormatting.GRAY, " / ")
                    .add(CreateLang.number(tank.getCapacity())
                            .add(mb)
                            .style(ChatFormatting.DARK_GRAY))
                    .forGoggles(tooltip, 1);
            speical = true;
        }
        return true;
    }
}
