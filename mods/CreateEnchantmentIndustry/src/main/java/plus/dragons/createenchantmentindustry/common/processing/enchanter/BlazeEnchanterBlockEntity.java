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

package plus.dragons.createenchantmentindustry.common.processing.enchanter;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import dev.engine_room.flywheel.lib.transform.TransformStack;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import net.createmod.catnip.math.AngleHelper;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;
import plus.dragons.createdragonsplus.common.fluids.tank.ConfigurableFluidTank;
import plus.dragons.createdragonsplus.util.FieldsNullabilityUnknownByDefault;
import plus.dragons.createenchantmentindustry.client.model.CEIPartialModels;
import plus.dragons.createenchantmentindustry.common.fluids.experience.BlazeExperienceBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIEnchantments;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;
import plus.dragons.createenchantmentindustry.config.CEIConfig;

@FieldsNullabilityUnknownByDefault
public class BlazeEnchanterBlockEntity extends BlazeExperienceBlockEntity {
    protected EnchanterBehaviour enchanter;
    protected int seed;
    protected int processingTime = -1;
    protected ItemStack heldItem = ItemStack.EMPTY;
    protected List<Holder<Enchantment>> enchantments = ImmutableList.of();

    public BlazeEnchanterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.seed = -1;
    }

    public @Nullable IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (side == Direction.DOWN)
            return tanks.getCapability();
        return null;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        this.enchanter = new EnchanterBehaviour(this, new EnchanterTransform());
        behaviours.add(this.enchanter);
    }

    @Override
    protected ConfigurableFluidTank createNormalTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIConfig.fluids().blazeEnchanterFluidCapacity.get(), fluidUpdateCallback)
                .allowInsertion(fluidStack -> fluidStack.is(CEIFluids.EXPERIENCE));
    }

    @Override
    protected ConfigurableFluidTank createSpecialTank(Consumer<FluidStack> fluidUpdateCallback) {
        return new ConfigurableFluidTank(CEIConfig.fluids().blazeEnchanterFluidCapacity.get(), fluidUpdateCallback)
                .forbidInsertion();
    }

    @Override
    public boolean isActive() {
        return !heldItem.isEmpty();
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected @Nullable PartialModel getHatModel(HeatLevel heatLevel) {
        return heatLevel.isAtLeast(HeatLevel.FADING)
                ? CEIPartialModels.BLAZE_ENCHANTER_HAT
                : CEIPartialModels.BLAZE_ENCHANTER_HAT_SMALL;
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        compound.putInt("Seed", seed);
        compound.put("HeldItem", heldItem.saveOptional(registries));
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        seed = Math.absExact(compound.getInt("Seed"));
        heldItem = ItemStack.parseOptional(registries, compound.getCompound("HeldItem"));
        if (level != null)
            enchantments = findPossibleEnchantments(heldItem).toList();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (heldItem != null)
            enchantments = findPossibleEnchantments(heldItem).toList();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    protected void onHeatChange(HeatLevel currentHeat, HeatLevel newHeat) {
        if (currentHeat == HeatLevel.SEETHING || newHeat == HeatLevel.SEETHING)
            enchantments = findPossibleEnchantments(heldItem).toList();
    }

    public RandomSource getRandom(Level level) {
        if (seed == -1)
            updateSeed(level);
        return RandomSource.create(seed);
    }

    public void updateSeed(Level level) {
        seed = level.random.nextInt(Integer.MAX_VALUE);
        notifyUpdate();
    }

    public int getMaxEnchantLevel() {
        return getMaxEnchantLevel(getHeatLevel() == HeatLevel.SEETHING);
    }

    public int getMaxEnchantLevel(boolean isSuper) {
        int max = CEIConfig.enchantments().blazeEnchanterMaxEnchantLevel.get();
        int maxSuper = CEIConfig.enchantments().blazeEnchanterMaxSuperEnchantLevel.get();
        return isSuper ? Math.max(max, maxSuper) : Math.clamp(max, 0, maxSuper);
    }

    public boolean isEnchantmentMaterial(ItemStack stack) {
        return false;
    }

    private boolean isSuperEnchantingCursed() {
        if (level == null)
            return false;
        var dimension = level.dimensionType();
        if (!dimension.hasSkyLight())
            return false;
        if (dimension.hasCeiling())
            return false;
        return level.canSeeSky(worldPosition);
    }

    public TagKey<Enchantment> getEnchantmentTag() {
        assert level != null;
        return getHeatLevel() == HeatLevel.SEETHING
                ? CEIEnchantments.MOD_TAGS.superEnchanting
                : CEIEnchantments.MOD_TAGS.enchanting;
    }

    public Stream<Holder<Enchantment>> findPossibleEnchantments(ItemStack stack) {
        assert this.level != null;
        return this.level
                .registryAccess()
                .registryOrThrow(Registries.ENCHANTMENT)
                .getTag(getEnchantmentTag())
                .stream()
                .flatMap(HolderSet::stream)
                .filter(stack::isPrimaryItemFor);
    }

    public List<Holder<Enchantment>> getPossibleEnchantments() {
        return isEnchantmentMaterial(heldItem) ? enchanter.enchantments : enchantments;
    }

    private void setHeldItem(ItemStack stack, List<Holder<Enchantment>> enchantments) {
        this.heldItem = stack;
        this.enchantments = enchantments;
        notifyUpdate();
    }

    public boolean insertItem(ItemStack stack, boolean simulate) {
        assert level != null;
        if (!heldItem.isEmpty())
            return false;
        if (isEnchantmentMaterial(stack)) {
            setHeldItem(stack, ImmutableList.of());
            return true;
        }
        var enchantments = findPossibleEnchantments(stack).toList();
        if (enchantments.isEmpty())
            return false;
        setHeldItem(stack, enchantments);
        return true;
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        added |= enchanter.addToGoggleTooltip(tooltip, isPlayerSneaking);
        return added;
    }

    private static class EnchanterTransform extends ValueBoxTransform.Sided {
        @Override
        protected Vec3 getSouthLocation() {
            return VecHelper.voxelSpace(8, 8, 13.5);
        }

        @Override
        public void rotate(LevelAccessor level, BlockPos pos, BlockState state, PoseStack poseStack) {
            float yRot = AngleHelper.horizontalAngle(getSide()) + 180;
            TransformStack.of(poseStack).rotateYDegrees(yRot);
        }

        @Override
        protected boolean isSideActive(BlockState state, Direction direction) {
            return direction.getAxis().isHorizontal();
        }
    }
}
