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

import com.simibubi.create.content.fluids.tank.CreativeFluidTankBlockEntity.CreativeSmartFluidTank;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import dev.engine_room.flywheel.api.visualization.VisualizationManager;
import java.util.List;
import java.util.function.Consumer;
import net.createmod.catnip.animation.LerpedFloat;
import net.createmod.catnip.animation.LerpedFloat.Chaser;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import plus.dragons.createdragonsplus.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createdragonsplus.common.fluids.tank.FluidTankBehaviour;
import plus.dragons.createdragonsplus.util.FieldsAssertedNonnullByDefault;

@FieldsAssertedNonnullByDefault
public abstract class BlazeExperienceBlockEntity extends SmartBlockEntity {
    protected LerpedFloat headAnimation = LerpedFloat.linear();
    protected LerpedFloat headAngle = LerpedFloat.angular();
    private boolean isCreative;
    protected FluidTankBehaviour tanks;

    public BlazeExperienceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    protected abstract ConfigurableFluidTank createTank(Consumer<FluidStack> fluidUpdateCallback);

    protected abstract ConfigurableFluidTank createSpecialTank(Consumer<FluidStack> fluidUpdateCallback);

    protected abstract boolean isActive();

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        tanks = new FluidTankBehaviour(this, List.of(this::createTank, this::createSpecialTank), false);
        behaviours.add(tanks);
    }

    @Override
    public void write(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putBoolean("isCreative", isCreative);
    }

    @Override
    protected void read(CompoundTag compound, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        isCreative = compound.getBoolean("isCreative");
    }

    @Override
    public void tick() {
        super.tick();
        assert level != null;
        if (level.isClientSide) {
            if (shouldTickAnimation())
                tickAnimation();
            if (!isVirtual())
                spawnParticles(getHeatLevelFromBlock());
            return;
        }

        if (isCreative)
            return;
        updateBlockState();
    }

    @OnlyIn(Dist.CLIENT)
    protected boolean shouldTickAnimation() {
        // Offload the animation tick to the visual when flywheel in enabled
        return !VisualizationManager.supportsVisualization(level);
    }

    @OnlyIn(Dist.CLIENT)
    protected void tickAnimation() {
        boolean active = getHeatLevelFromBlock().isAtLeast(HeatLevel.FADING) && isActive();

        if (active) {
            headAngle.chase((AngleHelper.horizontalAngle(getBlockState().getOptionalValue(BlazeBurnerBlock.FACING)
                    .orElse(Direction.SOUTH)) + 180) % 360, .125f, Chaser.EXP);
            headAngle.tickChaser();
        } else {
            float target = 0;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x;
                double z;
                if (isVirtual()) {
                    x = -4;
                    z = -10;
                } else {
                    x = player.getX();
                    z = player.getZ();
                }
                double dx = x - (getBlockPos().getX() + 0.5);
                double dz = z - (getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), target);
            headAngle.chase(target, .25f, Chaser.exp(5));
            headAngle.tickChaser();
        }

        headAnimation.chase(active ? 1 : 0, .25f, Chaser.exp(.25f));
        headAnimation.tickChaser();
    }

    public SmartFluidTank getNormalTank() {
        return tanks.getHandlers()[0];
    }

    public SmartFluidTank getSpecialTank() {
        return tanks.getHandlers()[1];
    }

    public boolean applyFuel(ExperienceFuel fuel, boolean forceOverflow, boolean simulate) {
        assert level != null;
        if (isCreative)
            return false;
        boolean special = fuel.special();
        if (!((special ? getSpecialTank() : getNormalTank()) instanceof ConfigurableFluidTank tank)) {
            return false;
        }
        int experience = fuel.experience();
        var fluid = ExperienceHelper.getExperienceFluid(experience);
        int fill = tank.fill(fluid, FluidAction.SIMULATE, true);
        if (fill != experience && !forceOverflow)
            return false;
        if (simulate)
            return true;
        if (level.isClientSide)
            spawnParticleBurst(special);
        tank.fill(fluid, FluidAction.EXECUTE, true);

        HeatLevel heat = getHeatLevelFromBlock();
        playSound();
        updateBlockState();

        if (heat != getHeatLevelFromBlock())
            level.playSound(null, worldPosition, SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS,
                    .125f + level.random.nextFloat() * .125f, 1.15f - level.random.nextFloat() * .25f);

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
            case KINDLED -> tanks.setTank(0,
                    callback -> new CreativeSmartFluidTank(getNormalTank().getCapacity(), callback));
            case SEETHING -> tanks.setTank(1,
                    callback -> new CreativeSmartFluidTank(getSpecialTank().getCapacity(), callback));
            default -> {
                tanks.setTank(0, this::createTank);
                tanks.setTank(1, this::createSpecialTank);
            }
        }
        setBlockHeat(next);
    }

    public BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock() {
        return BlazeBurnerBlock.getHeatLevelOf(getBlockState());
    }

    public BlazeBurnerBlock.HeatLevel getHeatLevelForRender() {
        HeatLevel heatLevel = getHeatLevelFromBlock();
        if (!heatLevel.isAtLeast(HeatLevel.FADING))
            return HeatLevel.FADING;
        return heatLevel;
    }

    public void updateBlockState() {
        setBlockHeat(getHeatLevel());
    }

    protected void setBlockHeat(HeatLevel newHeat) {
        HeatLevel currentHeat = getHeatLevelFromBlock();
        if (currentHeat == newHeat)
            return;
        assert level != null;
        level.setBlockAndUpdate(worldPosition, getBlockState().setValue(BlazeBurnerBlock.HEAT_LEVEL, newHeat));
        notifyUpdate();
    }

    protected HeatLevel getHeatLevel() {
        var special = getSpecialTank().getFluid();
        if (!special.isEmpty())
            return HeatLevel.SEETHING;
        var normal = getNormalTank().getFluid();
        if (!normal.isEmpty()) {
            boolean lowPercent = normal.getAmount() / (double) getNormalTank().getCapacity() < 0.0125;
            return lowPercent ? HeatLevel.FADING : HeatLevel.KINDLED;
        }
        return HeatLevel.SMOULDERING;
    }

    protected void playSound() {
        assert level != null;
        level.playSound(null, worldPosition, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS,
                .125f + level.random.nextFloat() * .125f,
                .75f - level.random.nextFloat() * .25f);
    }

    protected void spawnParticles(HeatLevel heatLevel) {
        assert level != null;
        if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE)
            return;

        RandomSource random = level.getRandom();

        Vec3 center = VecHelper.getCenterOf(worldPosition);
        Vec3 smokePos = center.add(VecHelper.offsetRandomly(Vec3.ZERO, random, .125f)
                .multiply(1, 0, 1));

        if (random.nextInt(4) != 0)
            return;

        boolean empty = level.getBlockState(worldPosition.above())
                .getCollisionShape(level, worldPosition.above())
                .isEmpty();

        if (empty || random.nextInt(8) == 0)
            level.addParticle(ParticleTypes.LARGE_SMOKE, smokePos.x, smokePos.y, smokePos.z, 0, 0, 0);

        double yMotion = empty ? .0625f : random.nextDouble() * .0125f;
        Vec3 flamePos = center.add(VecHelper.offsetRandomly(Vec3.ZERO, random, .5f)
                        .multiply(1, .25f, 1)
                        .normalize()
                        .scale((empty ? .25f : .5) + random.nextDouble() * .125f))
                .add(0, .5, 0);

        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, flamePos.x, flamePos.y, flamePos.z, 0, yMotion, 0);
        } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            level.addParticle(ParticleTypes.FLAME, flamePos.x, flamePos.y, flamePos.z, 0, yMotion, 0);
        }
    }

    protected void spawnParticleBurst(boolean soul) {
        assert level != null;
        Vec3 c = VecHelper.getCenterOf(worldPosition);
        RandomSource random = level.random;
        for (int i = 0; i < 20; i++) {
            Vec3 offset = VecHelper.offsetRandomly(Vec3.ZERO, random, .5f)
                    .multiply(1, .25f, 1)
                    .normalize();
            Vec3 pos = c.add(offset.scale(.5 + random.nextDouble() * .125f)).add(0, .125, 0);
            Vec3 motion = offset.scale(1 / 32f);

            level.addParticle(soul ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    pos.x, pos.y, pos.z,
                    motion.x, motion.y, motion.z);
        }
    }
}
