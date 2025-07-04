package com.kipti.bnb.content.light.headlamp;

import com.kipti.bnb.registry.BnbShapes;
import com.simibubi.create.AllShapes;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.createmod.catnip.outliner.Outliner;
import net.createmod.catnip.theme.Color;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class HeadlampBlockEntity extends SmartBlockEntity {

    int[] activePlacements = new int[9];

    public HeadlampBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {

    }

    public List<HeadlampPlacement> getExistingPlacements() {
        List<HeadlampPlacement> placements = new ArrayList<>();
        for (int i = 0; i < activePlacements.length; i++) {
            if (activePlacements[i] != 0) {
                placements.add(HeadlampPlacement.values()[i]);
            }
        }
        return placements;
    }

    @Override
    public void tick() {
        super.tick();
    }

    /**
     * Returns true if the headlamp was successfully placed into the block.
     */
    public boolean placeHeadlampIntoBlock(
        Vec3 position,
        Direction clickedDirection
    ) {
        Vec3 localPosition = getLocalSurfacePosition(position, clickedDirection);

        HeadlampPlacement placement = getClosestHeadlampPlacement(localPosition);
        List<HeadlampPlacement> existingPlacements = getExistingPlacements();

        if (placement.hasCollisionWith(existingPlacements)) {
            return false;
        }
        int index = placement.ordinal();
        this.activePlacements[index] = 1;
        sendData();
        return true;
    }

    public boolean canPlaceHeadlampIntoBlock(
        Vec3 position,
        Direction clickedDirection
    ) {
        Vec3 localPosition = getLocalSurfacePosition(position, clickedDirection);

        HeadlampPlacement placement = getClosestHeadlampPlacement(localPosition);
        List<HeadlampPlacement> existingPlacements = getExistingPlacements();

        return !placement.hasCollisionWith(existingPlacements);
    }

    public void placeDyeColorIntoBlock(DyeColor dyeColor, Vec3 position, Direction value) {
        Vec3 localPosition = getLocalSurfacePosition(position, value);

        HeadlampPlacement placement = getClosestExistingHeadlampPlacement(localPosition);
        if (placement == null) {
            return; // No existing placement found
        }
        int index = placement.ordinal();

        int i = dyeColor.ordinal() + 2;
        if (activePlacements[index] == i) {
            tryExtendPlaceDyeColorIntoFullBlock(
                dyeColor, getBlockState().getValue(HeadlampBlock.FACING), new ArrayList<>(List.of(getBlockPos())), new ArrayList<>()
            );
            return; // No change in color
        }
        this.activePlacements[index] = i; // 0 - no color, 1 - no dye, 2 - red, etc.
        sendData();
    }

    private void tryExtendPlaceDyeColorIntoFullBlock(DyeColor dyeColor, Direction facing, List<BlockPos> frontier, List<BlockPos> visited) {
        if (frontier.isEmpty()) {
            return; // No frontier to process
        }
        if (visited.size() > 32) {
            return;
        }
        BlockPos currentPos = frontier.remove(0);
        if (visited.contains(currentPos)) {
            tryExtendPlaceDyeColorIntoFullBlock(dyeColor, facing, frontier, visited);
            return; // Already visited this position
        }
        visited.add(currentPos);

        if (level.getBlockEntity(currentPos) instanceof HeadlampBlockEntity otherHeadlamp) {
            if (otherHeadlamp.placeDyeColorIntoFullBlock(dyeColor)) {
                return;
            }
        }

        // Check adjacent blocks
        List<Direction> directions = List.of(
            Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN
        ).stream().filter(d -> d.getAxis() != facing.getAxis()).toList();
        for (Direction direction : directions) {
            frontier.add(currentPos.relative(direction));
        }

        // Recursion :D
        tryExtendPlaceDyeColorIntoFullBlock(dyeColor, facing, frontier, visited);
    }

    public boolean removeNearestHeadlamp(Vec3 subtract, Direction value) {
        Vec3 localPosition = getLocalSurfacePosition(subtract, value);

        HeadlampPlacement closestPlacement = getClosestExistingHeadlampPlacement(localPosition);
        if (closestPlacement == null) {
            return false; // No existing placement found
        }
        int index = closestPlacement.ordinal();
        this.activePlacements[index] = 0; // Remove the headlamp
        sendData();
        if (getExistingPlacements().isEmpty()) {
            level.removeBlock(worldPosition, false);
        }
        return true;
    }

    private static @NotNull Vec3 getLocalSurfacePosition(Vec3 position, Direction value) {
        // Transform the position into a point on the xz plane, where x = leftright, z = updown.
        Vector3f jomlLocalPosition = value.getRotation().transformInverse(new Vector3f((float) position.x, (float) position.y, (float) position.z));
        Vec3 localPosition = new Vec3(
            jomlLocalPosition.x,
            jomlLocalPosition.y,
            jomlLocalPosition.z
        );
        return localPosition;
    }

    @Override
    protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.read(tag, registries, clientPacket);
        int[] placements = tag.getIntArray("activePlacements");
        if (placements.length != activePlacements.length) {
            throw new IllegalStateException("Active placements length mismatch: expected " + activePlacements.length + ", got " + placements.length);
        }
        System.arraycopy(placements, 0, activePlacements, 0, placements.length);
        if (clientPacket) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 16);
        }
    }

    @Override
    protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) {
        super.write(tag, registries, clientPacket);
        tag.putIntArray("activePlacements", activePlacements);
    }

    /**
     * Target on surface is a point on the xz plane, x = leftright, z = updown.
     */
    public static HeadlampPlacement getClosestHeadlampPlacement(
        Vec3 targetOnSurface
    ) {
        return new HeadlampPlacement(
            getAlignmentFromOffset(targetOnSurface.z),
            getAlignmentFromOffset(targetOnSurface.x)
        );
    }

    private @Nullable HeadlampPlacement getClosestExistingHeadlampPlacement(Vec3 localPosition) {
        HeadlampPlacement closestPlacement = null;
        double closestDistance = Double.MAX_VALUE;
        for (HeadlampPlacement placement : HeadlampPlacement.values()) {
            if (this.activePlacements[placement.ordinal()] == 0) {
                continue;
            }

            double distance = Math.abs(localPosition.x - placement.horizontalAlignment.getOffset()) +
                Math.abs(localPosition.z - placement.verticalAlignment.getOffset());

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPlacement = placement;
            }
        }
        return closestPlacement;
    }

    private static HeadlampAlignment getAlignmentFromOffset(
        double offset
    ) {
        if (offset < HeadlampAlignment.RIGHT_OR_BOTTOM.getOffset() / 2f) {
            return HeadlampAlignment.RIGHT_OR_BOTTOM;
        } else if (offset > HeadlampAlignment.LEFT_OR_TOP.getOffset() / 2f) {
            return HeadlampAlignment.LEFT_OR_TOP;
        } else {
            return HeadlampAlignment.MIDDLE;
        }
    }

    public int[] getActivePlacements() {
        return this.activePlacements;
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        AllShapes.Builder builder = new AllShapes.Builder(Shapes.empty());
        List<HeadlampPlacement> placements = getExistingPlacements();

        if (placements.isEmpty()) {
            return Shapes.block();
        }

        for (HeadlampPlacement placement : placements) {
            if (activePlacements[placement.ordinal()] != 0) {
                builder.add(BnbShapes.cuboid(
                    4 + placement.horizontalAlignment.pixelOffset, 0, 4 + placement.verticalAlignment.pixelOffset,
                    12 + placement.horizontalAlignment.pixelOffset, 7, 12 + placement.verticalAlignment.pixelOffset
                ));
            }
        }
        return builder.forDirectional().get(state.getValue(HeadlampBlock.FACING));
    }

    public boolean placeDyeColorIntoFullBlock(DyeColor dyeColor) {
        boolean placedAny = false;
        int toAdd = dyeColor.ordinal() + 2;
        for (int i = 0; i < HeadlampPlacement.values().length; i++) {
            if (this.activePlacements[i] == 0 || this.activePlacements[i] == toAdd) {
                continue; // Skip placements without headlamps
            }
            placedAny = true;
            this.activePlacements[i] = toAdd; // 0 - no color, 1 - no dye, 2 - red, etc.
        }
        if (!placedAny) {
            return false; // No headlamps to place dye color into
        }
        sendData();
        return true;
    }

    public enum HeadlampAlignment {
        RIGHT_OR_BOTTOM(-4),
        MIDDLE(0),
        LEFT_OR_TOP(4);
        final int pixelOffset;

        HeadlampAlignment(int pixelOffset) {
            this.pixelOffset = pixelOffset;
        }

        public int getPixelOffset() {
            return pixelOffset;
        }

        public double getOffset() {
            return pixelOffset / 16.0;
        }

        public boolean collidesWith(HeadlampAlignment horizontalAlignment) {
            return this == horizontalAlignment || this == MIDDLE || horizontalAlignment == MIDDLE;
        }
    }

    public record HeadlampPlacement(
        HeadlampAlignment verticalAlignment,
        HeadlampAlignment horizontalAlignment
    ) {
        private static final HeadlampPlacement[] VALUES = generateValues();

        private static HeadlampPlacement[] generateValues() {
            List<HeadlampPlacement> placements = new ArrayList<>();
            for (HeadlampAlignment vertical : HeadlampAlignment.values()) {
                for (HeadlampAlignment horizontal : HeadlampAlignment.values()) {
                    placements.add(new HeadlampPlacement(vertical, horizontal));
                }
            }
            return placements.toArray(new HeadlampPlacement[0]);
        }

        public static HeadlampPlacement[] values() {
            return VALUES;
        }

        public boolean hasCollisionWith(List<HeadlampPlacement> existingPlacements) {
            for (HeadlampPlacement placement : existingPlacements) {
                if (placement.horizontalAlignment.collidesWith(horizontalAlignment) &&
                    placement.verticalAlignment.collidesWith(verticalAlignment)) {
                    return true;
                }
            }
            return false;
        }

        public int ordinal() {
            for (int i = 0; i < VALUES.length; i++) {
                if (VALUES[i].equals(this)) {
                    return i;
                }
            }
            throw new IllegalStateException("HeadlampPlacement not found in values array: " + this);
        }
    }

}
