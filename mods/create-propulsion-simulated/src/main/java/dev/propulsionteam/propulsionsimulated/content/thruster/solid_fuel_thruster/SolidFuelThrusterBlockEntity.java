package dev.propulsionteam.propulsionsimulated.content.thruster.solid_fuel_thruster;

import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.utility.CreateLang;
import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlock;
import dev.propulsionteam.propulsionsimulated.content.thruster.AbstractThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.ItemThrusterProperties;
import dev.propulsionteam.propulsionsimulated.content.thruster.SolidThrusterFuelManager;
import dev.propulsionteam.propulsionsimulated.content.thruster.ThrusterParticleType;
import dev.propulsionteam.propulsionsimulated.registries.PropulsionBlockEntities;
import net.createmod.catnip.lang.LangBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Clearable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;
import java.util.Locale;

public class SolidFuelThrusterBlockEntity extends AbstractThrusterBlockEntity implements Clearable {
    private static final double TICKS_PER_MINUTE = 20.0d * 60.0d;

    private final SolidFuelThrusterItemHandler itemHandler = new SolidFuelThrusterItemHandler(this);

    private ItemStack burningFuel = ItemStack.EMPTY;
    private ItemStack queuedFuel = ItemStack.EMPTY;
    private int burnTime = 0;
    private double lastConsumedItemsPerTick = 0.0d;
    private int syncedFuelCount = 0;

    public SolidFuelThrusterBlockEntity(BlockPos pos, BlockState state) {
        super(PropulsionBlockEntities.SOLID_FUEL_THRUSTER_BLOCK_ENTITY.get(), pos, state);
    }

    public SolidFuelThrusterBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void tick() {
        if (level != null && !level.isClientSide) {
            tickFuel();
        }
        super.tick();
    }

    private void tickFuel() {
        if (burnTime > 0 && isPowered()) {
            burnTime--;
            updateConsumptionRate();
        } else if (burnTime > 0) {
            lastConsumedItemsPerTick = 0.0d;
        }

        if (burnTime <= 0) {
            burningFuel = ItemStack.EMPTY;
            lastConsumedItemsPerTick = 0.0d;
            if (!queuedFuel.isEmpty() && isPowered()) {
                startBurning(queuedFuel);
                queuedFuel = ItemStack.EMPTY;
                markFuelChanged();
            }
        }
    }

    private void updateConsumptionRate() {
        if (burningFuel.isEmpty()) {
            lastConsumedItemsPerTick = 0.0d;
            return;
        }
        int totalBurn = Math.max(1, SolidThrusterFuelManager.resolveBurnTicks(burningFuel));
        float throttle = Math.min(getPower(), calculateObstructionEffect());
        lastConsumedItemsPerTick = throttle > 0 ? throttle / (double) totalBurn : 0.0d;
    }

    private int getInternalFuelCount() {
        return (burningFuel.isEmpty() ? 0 : 1) + (queuedFuel.isEmpty() ? 0 : 1);
    }

    public void markFuelChanged() {
        syncedFuelCount = getInternalFuelCount();
        setChanged();
        notifyUpdate();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void startBurning(ItemStack stack) {
        if (stack.isEmpty() || !canAcceptFuel(stack)) {
            return;
        }
        burningFuel = stack.copyWithCount(1);
        burnTime = SolidThrusterFuelManager.resolveBurnTicks(burningFuel);
        updateConsumptionRate();
        markFuelChanged();
        dirtyThrust();
    }

    public boolean canAcceptFuel(ItemStack stack) {
        return !stack.isEmpty() && SolidThrusterFuelManager.getProperties(stack) != null;
    }

    public ItemStack getQueuedFuel() {
        return queuedFuel;
    }

    public void setQueuedFuel(ItemStack stack) {
        queuedFuel = stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1);
    }

    public int getBurnTime() {
        return burnTime;
    }

    public ItemStack getBurningFuel() {
        return burningFuel;
    }

    public IItemHandler getItemHandler(Direction side) {
        return itemHandler;
    }

    /** Same side as fluid thruster input: the block {@code facing} (back / funnel). */
    public Direction getFuelInputSide() {
        return getFacing();
    }

    public boolean tryInsertOrExtractFuel(Player player, InteractionHand hand) {
        if (player == null || player.isSpectator()) {
            return false;
        }
        ItemStack held = player.getItemInHand(hand);

        if (held.isEmpty()) {
            ItemStack queued = getQueuedFuel();
            if (queued.isEmpty()) {
                return false;
            }
            if (!player.getInventory().add(queued.copy())) {
                player.drop(queued.copy(), false);
            }
            setQueuedFuel(ItemStack.EMPTY);
            markFuelChanged();
            return true;
        }

        ItemStack remainder = itemHandler.insertItem(SolidFuelThrusterItemHandler.QUEUE_SLOT, held, false);
        if (remainder.getCount() == held.getCount()) {
            return false;
        }
        player.setItemInHand(hand, remainder);
        return true;
    }

    @Override
    protected boolean isWorking() {
        return burnTime > 0 && !burningFuel.isEmpty();
    }

    @Override
    public void updateThrust(BlockState currentBlockState) {
        float thrust = 0;
        float currentPower = getPower();
        lastConsumedItemsPerTick = 0.0d;

        if (isWorking() && currentPower > 0) {
            ItemThrusterProperties properties = SolidThrusterFuelManager.getProperties(burningFuel);
            float obstructionEffect = calculateObstructionEffect();
            float thrustPercentage = Math.min(currentPower, obstructionEffect);

            if (thrustPercentage > 0 && properties != null) {
                float fuelEfficiency = SolidThrusterFuelManager.getEfficiency(burningFuel.getItem());
                float baseThrustPn = (float) (getBaseThrust() * getThrustUnitsPerKn());
                baseThrustPn *= (float) calculateAtmosphericFactor();
                thrust = baseThrustPn * thrustPercentage * properties.thrustMultiplier() * fuelEfficiency;
                int totalBurn = Math.max(1, SolidThrusterFuelManager.resolveBurnTicks(burningFuel));
                lastConsumedItemsPerTick = thrustPercentage / (double) totalBurn;
            }
        }

        setThrustAndSync(thrust);
        isThrustDirty = false;
    }

    @Override
    protected double getBaseThrust() {
        return PropulsionConfig.SOLID_FUEL_THRUSTER_BASE_THRUST.get();
    }

    @Override
    protected double getRawThrustCap() {
        return PropulsionConfig.SOLID_FUEL_THRUSTER_BASE_THRUST.get();
    }

    @Override
    public double getNozzleOffsetFromCenter() {
        return PropulsionConfig.SOLID_FUEL_THRUSTER_NOZZLE_OFFSET.get();
    }

    @Override
    protected double getParticleCountMultiplier() {
        return PropulsionConfig.SOLID_FUEL_THRUSTER_PARTICLE_COUNT_MULTIPLIER.get();
    }

    @Override
    protected double getParticleVelocityMultiplier() {
        return PropulsionConfig.SOLID_FUEL_THRUSTER_PARTICLE_VELOCITY_MULTIPLIER.get();
    }

    @Override
    public boolean shouldEmitParticles() {
        if (!super.shouldEmitParticles()) {
            return false;
        }
        ItemThrusterProperties properties = SolidThrusterFuelManager.getProperties(burningFuel);
        return properties != null && properties.particleType() != ThrusterParticleType.NONE;
    }

    @Override
    protected ParticleOptions createParticleOptions() {
        ItemThrusterProperties properties = SolidThrusterFuelManager.getProperties(burningFuel);
        if (properties == null) {
            return super.createParticleOptions();
        }
        return properties.particleType().createParticleOptions(properties);
    }

    @Override
    protected LangBuilder getGoggleStatus() {
        if (burningFuel.isEmpty() && queuedFuel.isEmpty()) {
            return CreateLang.builder()
                .add(Component.translatable("createpropulsion.gui.goggles.thruster.status.no_fuel"))
                .style(ChatFormatting.RED);
        }
        if (!validFuel()) {
            return CreateLang.builder()
                .add(Component.translatable("createpropulsion.gui.goggles.thruster.status.wrong_fuel"))
                .style(ChatFormatting.RED);
        }
        if (!isPowered()) {
            return CreateLang.builder()
                .add(Component.translatable("createpropulsion.gui.goggles.thruster.status.not_powered"))
                .style(ChatFormatting.GOLD);
        }
        if (getEmptyBlocks() == 0) {
            return CreateLang.builder()
                .add(Component.translatable("createpropulsion.gui.goggles.thruster.obstructed"))
                .style(ChatFormatting.RED);
        }
        return CreateLang.builder()
            .add(Component.translatable("createpropulsion.gui.goggles.thruster.status.working"))
            .style(ChatFormatting.GREEN);
    }

    @Override
    protected void addThrusterDetails(List<Component> tooltip, boolean isPlayerSneaking) {
        super.addThrusterDetails(tooltip, isPlayerSneaking);
        CreateLang.builder()
            .add(Component.translatable("createpropulsion.gui.goggles.solid_fuel_thruster.fuel_label"))
            .style(ChatFormatting.WHITE)
            .forGoggles(tooltip);

        int stored = syncedFuelCount;
        CreateLang.builder()
            .add(Component.literal("  "))
            .add(Component.literal(Integer.toString(stored)).withStyle(ChatFormatting.AQUA))
            .add(Component.literal(" / 2").withStyle(ChatFormatting.GRAY))
            .forGoggles(tooltip);

        CreateLang.builder()
            .add(Component.literal("  "))
            .add(Component.literal(String.format(Locale.ROOT, "%.2f", getDisplayedItemsPerMinute())).withStyle(ChatFormatting.AQUA))
            .add(Component.translatable("createpropulsion.gui.goggles.solid_fuel_thruster.items_per_minute")
                .withStyle(ChatFormatting.GRAY))
            .forGoggles(tooltip);

        if (!queuedFuel.isEmpty()) {
            CreateLang.builder()
                .add(Component.literal("  "))
                .add(queuedFuel.getHoverName().copy().withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);
        } else if (!burningFuel.isEmpty()) {
            CreateLang.builder()
                .add(Component.literal("  "))
                .add(burningFuel.getHoverName().copy().withStyle(ChatFormatting.GRAY))
                .forGoggles(tooltip);
        }
    }

    private double getDisplayedItemsPerMinute() {
        return lastConsumedItemsPerTick * TICKS_PER_MINUTE;
    }

    public boolean validFuel() {
        if (!burningFuel.isEmpty()) {
            return SolidThrusterFuelManager.getProperties(burningFuel) != null;
        }
        if (!queuedFuel.isEmpty()) {
            return SolidThrusterFuelManager.getProperties(queuedFuel) != null;
        }
        return false;
    }

    @Override
    public FluidStack fluidStack() {
        return FluidStack.EMPTY;
    }

    @Override
    public boolean validFluid() {
        return false;
    }

    @Override
    public IFluidHandler getFluidHandler(Direction side) {
        return null;
    }

    @Override
    public void clearContent() {
        burningFuel = ItemStack.EMPTY;
        queuedFuel = ItemStack.EMPTY;
        burnTime = 0;
    }

    @Override
    protected void write(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.write(compound, registries, clientPacket);
        syncedFuelCount = getInternalFuelCount();
        compound.putInt("BurnTime", burnTime);
        compound.putDouble("LastConsumedItemsPerTick", lastConsumedItemsPerTick);
        compound.putInt("SyncedFuelCount", syncedFuelCount);
        if (!burningFuel.isEmpty()) {
            compound.put("BurningFuel", burningFuel.save(registries));
        } else {
            compound.remove("BurningFuel");
        }
        if (!queuedFuel.isEmpty()) {
            compound.put("QueuedFuel", queuedFuel.save(registries));
        } else {
            compound.remove("QueuedFuel");
        }
    }

    @Override
    protected void read(CompoundTag compound, net.minecraft.core.HolderLookup.Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        burnTime = compound.getInt("BurnTime");
        lastConsumedItemsPerTick = compound.getDouble("LastConsumedItemsPerTick");
        burningFuel = compound.contains("BurningFuel")
            ? ItemStack.parse(registries, compound.getCompound("BurningFuel")).orElse(ItemStack.EMPTY)
            : ItemStack.EMPTY;
        queuedFuel = compound.contains("QueuedFuel")
            ? ItemStack.parse(registries, compound.getCompound("QueuedFuel")).orElse(ItemStack.EMPTY)
            : ItemStack.EMPTY;
        syncedFuelCount = compound.contains("SyncedFuelCount")
            ? compound.getInt("SyncedFuelCount")
            : getInternalFuelCount();
    }

    @Override
    public void initialize() {
        super.initialize();
        syncedFuelCount = getInternalFuelCount();
    }
}
