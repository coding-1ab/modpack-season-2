package dev.eriksonn.aeronautics.content.blocks.hot_air.balloon;

import dev.eriksonn.aeronautics.content.blocks.hot_air.BlockEntityLiftingGasProvider;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph.BalloonLayerData;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.graph.BalloonLayerGraph;
import dev.eriksonn.aeronautics.content.blocks.hot_air.balloon.map.SavedBalloon;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasData;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasHolder;
import dev.eriksonn.aeronautics.content.blocks.hot_air.lifting_gas.LiftingGasType;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.SubLevelHelper;
import dev.ryanhcode.sable.companion.math.Pose3d;
import dev.ryanhcode.sable.api.physics.force.ForceGroups;
import dev.ryanhcode.sable.api.physics.force.QueuedForceGroup;
import dev.ryanhcode.sable.physics.config.dimension_physics.DimensionPhysicsData;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.util.LevelAccelerator;
import dev.ryanhcode.sable.util.SableMathUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Matrix3d;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServerBalloon extends Balloon {
    private final Map<LiftingGasType, LiftingGasData> gasAmounts = new Object2ObjectOpenHashMap<>();

    // Physics
    private final Matrix3d outerProduct = new Matrix3d();
    private final Matrix3d inertiaTensor = new Matrix3d();

    private final Matrix3d translatedInertiaTensor = new Matrix3d();
    private final Matrix3d translatedOuterProduct = new Matrix3d();

    private final Vector3d averagePosition = new Vector3d();
    private final Vector3d translatedAveragePosition = new Vector3d();

    private final Vector3d offset = new Vector3d();
    /**
     * The origin vector used for the average position & matrices
     */
    private final Vector3d physicsOrigin;

    // Fill
    private double totalLift;
    private double totalFilledVolume;
    private double totalTargetVolume;
    private double totalVolumeChange;
    private boolean leaking = false;

    @ApiStatus.Internal
    public ServerBalloon(final Level level, final LevelAccelerator accelerator, final BlockPos controllerPos, final BalloonLayerGraph graph, final ObjectArrayList<BlockEntityLiftingGasProvider> heaters) {
        super(level, accelerator, controllerPos, graph, heaters);
        this.physicsOrigin = new Vector3d(controllerPos.getX(), controllerPos.getY(), controllerPos.getZ());
        this.onRebuilt();
    }

    /**
     * equivalent to identity.scale(u.dot(v))-fmaOuterProduct(u,v)
     */
    public static Matrix3d fmaInertiaTensor(final Vector3dc u, final Vector3dc v, final double scale, final Matrix3d target) {
        target.m00 += (u.y() * v.y() + u.z() * v.z()) * scale;
        target.m01 -= u.y() * v.x() * scale;
        target.m02 -= u.z() * v.x() * scale;
        target.m10 -= u.x() * v.y() * scale;
        target.m11 += (u.z() * v.z() + u.x() * v.x()) * scale;
        target.m12 -= u.z() * v.y() * scale;
        target.m20 -= u.x() * v.z() * scale;
        target.m21 -= u.y() * v.z() * scale;
        target.m22 += (u.x() * v.x() + u.y() * v.y()) * scale;
        return target;
    }

    public void translateMatrices(final ServerSubLevel serverSubLevel) {
        final Vector3dc centerOfMass = serverSubLevel.getMassTracker().getCenterOfMass();
        this.offset.set(centerOfMass).sub(this.physicsOrigin);

        this.translatedOuterProduct.set(this.outerProduct);
        SableMathUtils.fmaOuterProduct(this.offset, this.averagePosition, -this.getCapacity(), this.translatedOuterProduct);
        SableMathUtils.fmaOuterProduct(this.averagePosition, this.offset, -this.getCapacity(), this.translatedOuterProduct);
        SableMathUtils.fmaOuterProduct(this.offset, this.offset, this.getCapacity(), this.translatedOuterProduct);

        this.translatedInertiaTensor.set(this.inertiaTensor);
        fmaInertiaTensor(this.offset, this.averagePosition, -this.getCapacity(), this.translatedInertiaTensor);
        fmaInertiaTensor(this.averagePosition, this.offset, -this.getCapacity(), this.translatedInertiaTensor);
        fmaInertiaTensor(this.offset, this.offset, this.getCapacity(), this.translatedInertiaTensor);
    }

    protected void checkHeaters() {
        super.checkHeaters();

        for (final LiftingGasData data : this.gasAmounts.values()) {
            data.target = 0;
        }

        if (this.leaking) {
            return;
        }

        for (final BlockEntityLiftingGasProvider heater : this.heaters) {
            this.gasAmounts.compute(heater.getLiftingGasType(), (k, v) -> {
                if (v == null) {
                    v = new LiftingGasData();
                }

                v.target += heater.getGasOutput();
                return v;
            });
        }
    }

    public void applyForces(final double timeStep) {
        final int capacity = this.getCapacity();
        if (capacity <= 0) return;

        final ServerSubLevel subLevel = (ServerSubLevel) Sable.HELPER.getContaining(this.level, this.controllerPos);

        if (subLevel == null || this.totalFilledVolume == 0) {
            return;
        }

        this.translateMatrices(subLevel);

        final Level level = subLevel.getLevel();
        final Pose3d pose = subLevel.logicalPose();

        this.translatedAveragePosition.set(this.averagePosition)
                .add(this.physicsOrigin);

        // calculate and impart appropriate forces onto associated sub-level

        final Vector3d localAveragePosition = new Vector3d(this.translatedAveragePosition).sub(pose.rotationPoint());
        final Vector3d worldCenter = new Vector3d(localAveragePosition);
        pose.orientation().transform(worldCenter).add(pose.position());

        final Vector3d gravity = pose.orientation().transformInverse(new Vector3d(DimensionPhysicsData.getGravity(level, worldCenter)));
        final double pressure = DimensionPhysicsData.getAirPressure(level, worldCenter);

        final double diff = 1;
        final Vector3d gradient = new Vector3d();
        final double pressureX = DimensionPhysicsData.getAirPressure(level, gradient.set(diff, 0, 0).add(worldCenter)) - pressure;
        final double pressureY = DimensionPhysicsData.getAirPressure(level, gradient.set(0, diff, 0).add(worldCenter)) - pressure;
        final double pressureZ = DimensionPhysicsData.getAirPressure(level, gradient.set(0, 0, diff).add(worldCenter)) - pressure;
        gradient.set(pressureX, pressureY, pressureZ).div(diff);
        pose.orientation().transformInverse(gradient);

        final Vector3d baseForcePerBlock = new Vector3d(gravity).mul(-this.totalLift / capacity);
        final Vector3d baseTorquePerBlock = new Vector3d();

        final Vector3d force = new Vector3d();
        final Vector3d torque = new Vector3d();
        final Vector3d temp = new Vector3d();

        final Vector3d centerForce = new Vector3d();
        this.translatedOuterProduct.transform(gradient);
        baseTorquePerBlock.cross(localAveragePosition, centerForce).add(baseForcePerBlock);
        baseTorquePerBlock.cross(gradient, force).fma(capacity * pressure, centerForce);

        localAveragePosition.cross(force, torque);
        torque.add(gradient.cross(centerForce, temp));
        torque.fma(pressure, this.translatedInertiaTensor.transform(baseTorquePerBlock, temp));

        // torque and force are both in momentum units, let's get them into acceleration
        force.mul(timeStep);
        torque.mul(timeStep);

        final QueuedForceGroup forceGroup = subLevel.getOrCreateQueuedForceGroup(ForceGroups.BALLOON_LIFT.get());
        forceGroup.getForceTotal().applyLinearAndAngularImpulse(force, torque);

        if (subLevel.isTrackingIndividualQueuedForces()) {
            forceGroup.recordPointForce(new Vector3d(this.translatedAveragePosition), force);
        }
    }

    @Override
    protected void onRebuilt() {
        this.outerProduct.zero();
        this.inertiaTensor.zero();
        this.averagePosition.zero();

        final Vector3d p = new Vector3d();
        final Matrix3d m = new Matrix3d();

        for (final List<BalloonLayerData> layers : this.graph.getAllLayers()) {
            for (final BalloonLayerData layer : layers) {
                final Iterator<BlockPos> iter = layer.nonSolidBlockIterator();

                while (iter.hasNext()) {
                    final BlockPos pos = iter.next();

                    p.set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).sub(this.physicsOrigin);
                    this.averagePosition.add(p);
                }
            }
        }

        this.averagePosition.div(this.getCapacity());

        for (final List<BalloonLayerData> layers : this.graph.getAllLayers()) {
            for (final BalloonLayerData layer : layers) {
                final Iterator<BlockPos> iter = layer.nonSolidBlockIterator();

                while (iter.hasNext()) {
                    final BlockPos pos = iter.next();
                    final int x = pos.getX();
                    final int y = pos.getY();
                    final int z = pos.getZ();

                    p.set(x + 0.5, y + 0.5, z + 0.5).sub(this.physicsOrigin);
                    this.outerProduct.add(m.set(
                            p.x * p.x, p.x * p.y, p.x * p.z,
                            p.y * p.x, p.y * p.y, p.y * p.z,
                            p.z * p.x, p.z * p.y, p.z * p.z
                    ));
                    this.inertiaTensor.add(m.set(
                            p.y * p.y + p.z * p.z, -p.x * p.y, -p.x * p.z,
                            -p.y * p.x, p.z * p.z + p.x * p.x, -p.y * p.z,
                            -p.z * p.x, -p.z * p.y, p.x * p.x + p.y * p.y
                    ));
                }
            }
        }

        this.leaking = false;
    }

    @Override
    protected void onHotAirAdded(final BlockPos pos) {
        final Matrix3d m = new Matrix3d();
        final Vector3d p = new Vector3d();

        p.set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                .sub(this.physicsOrigin);

        this.averagePosition.mul(this.getCapacity() - 1)
                .add(p)
                .div(this.getCapacity());

        this.outerProduct.add(m.set(
                p.x * p.x, p.x * p.y, p.x * p.z,
                p.y * p.x, p.y * p.y, p.y * p.z,
                p.z * p.x, p.z * p.y, p.z * p.z
        ));
        this.inertiaTensor.add(m.set(
                p.y * p.y + p.z * p.z, -p.x * p.y, -p.x * p.z,
                -p.y * p.x, p.z * p.z + p.x * p.x, -p.y * p.z,
                -p.z * p.x, -p.z * p.y, p.x * p.x + p.y * p.y
        ));
    }

    @Override
    protected void onHotAirRemoved(final BlockPos pos) {
        final Matrix3d m = new Matrix3d();
        final Vector3d p = new Vector3d();

        p.set(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                .sub(this.physicsOrigin);

        this.averagePosition.mul(this.getCapacity() + 1)
                .sub(p)
                .div(this.getCapacity());

        this.outerProduct.add(m.set(
                p.x * p.x, p.x * p.y, p.x * p.z,
                p.y * p.x, p.y * p.y, p.y * p.z,
                p.z * p.x, p.z * p.y, p.z * p.z
        ));
        this.inertiaTensor.add(m.set(
                p.y * p.y + p.z * p.z, -p.x * p.y, -p.x * p.z,
                -p.y * p.x, p.z * p.z + p.x * p.x, -p.y * p.z,
                -p.z * p.x, -p.z * p.y, p.x * p.x + p.y * p.y
        ));
    }

    @Override
    protected void onHotAirRemoved(final Iterable<BlockPos> iterable) {
        super.onHotAirRemoved(iterable);
        final Matrix3d m = new Matrix3d();
        final Vector3d p = new Vector3d();

        for (final BlockPos blockPos : iterable) {
            final int y = blockPos.getY();
            final int x = blockPos.getX();
            final int z = blockPos.getZ();

            p.set(x + 0.5, y + 0.5, z + 0.5).sub(this.physicsOrigin);

            this.averagePosition.mul(this.capacity)
                    .sub(p)
                    .div(this.capacity - 1);

            this.outerProduct.sub(m.set(
                    p.x * p.x, p.x * p.y, p.x * p.z,
                    p.y * p.x, p.y * p.y, p.y * p.z,
                    p.z * p.x, p.z * p.y, p.z * p.z
            ));
            this.inertiaTensor.sub(m.set(
                    p.y * p.y + p.z * p.z, -p.x * p.y, -p.x * p.z,
                    -p.y * p.x, p.z * p.z + p.x * p.x, -p.y * p.z,
                    -p.z * p.x, -p.z * p.y, p.x * p.x + p.y * p.y
            ));
            this.capacity--;
        }
    }

    @Override
    public boolean isValid() {
        return this.totalTargetVolume > 0.05 || this.totalFilledVolume > 0.05;
    }

    public void tick() {
        super.tick();
        this.updateGasAmounts();
    }

    public void updateGasAmounts() {
        final int capacity = this.getCapacity();

        // we'll allow the balloon to temporarily be over the capacity
        // so that situations such as nuking half of the balloon won't cause instant changes in lift

        // sum total target
        this.totalTargetVolume = 0;
        for (final LiftingGasData data : this.gasAmounts.values()) {
            this.totalTargetVolume += data.target;
        }

        // get nudges
        final double scale = Math.min(capacity / this.totalTargetVolume, 1);
        double totalDesiredVolume = 0;
        for (final Map.Entry<LiftingGasType, LiftingGasData> entry : this.gasAmounts.entrySet()) {
            final LiftingGasData data = entry.getValue();
            final LiftingGasType type = entry.getKey();
            final double diff = data.target * scale - data.amount;
            data.nudge = diff > 0 ? diff / type.getFillingTime() : (diff < 0 ? diff / type.getEmptyingTime() : 0);

            if (type.getResponsivenessAdjustmentFactor() > 0 && type.getResponsivenessAdjustmentRange() > 0) {
                final double x = diff / (capacity * type.getResponsivenessAdjustmentRange());
                data.nudge *= 1 + type.getResponsivenessAdjustmentFactor() / (1 + 3 * x * x);
            }

            totalDesiredVolume += data.amount + data.nudge;
        }

        // apply nudges and calculate total lift
        this.totalLift = 0;
        this.totalFilledVolume = 0;
        this.totalVolumeChange = 0;

        for (final Map.Entry<LiftingGasType, LiftingGasData> entry : this.gasAmounts.entrySet()) {
            final LiftingGasData data = entry.getValue();
            data.amount += data.nudge;
            this.totalLift += data.amount * entry.getKey().getLiftStrength();
            this.totalFilledVolume += data.amount;
            this.totalVolumeChange += data.nudge;
        }

        this.totalTargetVolume = Math.min(this.totalTargetVolume, capacity);
    }

    @Override
    public void merge(final Balloon other) {
        super.merge(other);

        if (other instanceof final ServerBalloon otherServerBalloon) {
            for (final Map.Entry<LiftingGasType, LiftingGasData> entry : otherServerBalloon.gasAmounts.entrySet()) {
                final LiftingGasType type = entry.getKey();
                final LiftingGasData data = entry.getValue();

                this.gasAmounts.computeIfAbsent(type, x -> new LiftingGasData()).amount += data.amount;
            }
        }
    }

    @Override
    public void setLeaking() {
        this.leaking = true;
    }

    public Vec3 getCenter() {
        return JOMLConversion.toMojang(this.averagePosition).add(this.physicsOrigin.x(), this.physicsOrigin.y(), this.physicsOrigin.z());
    }

    public double getTotalLift() {
        return this.totalLift;
    }

    public double getTotalFilledVolume() {
        return this.totalFilledVolume;
    }

    public double getTotalTargetVolume() {
        return this.totalTargetVolume;
    }

    public double getTotalVolumeChange() {
        return this.totalVolumeChange;
    }

    @Override
    public boolean shouldSpawnGust(final BlockPos pos) {
        final float percentHeight = (pos.getY() + 0.5f - this.bounds.minY) / this.getHeight();
        return percentHeight > 1.0 - Math.clamp(this.totalFilledVolume / this.getCapacity(), 0, 1);
    }

    @Override
    public void setAssembling(final SubLevelAssemblyHelper.AssemblyTransform transform) {
        super.setAssembling(transform);
        this.physicsOrigin.set(this.controllerPos.getX(), this.controllerPos.getY(), this.controllerPos.getZ());
    }

    public void loadFrom(final SavedBalloon unloaded) {
        for (final LiftingGasHolder entry : unloaded.gasData()) {
            final LiftingGasType type = entry.type();
            final LiftingGasData data = entry.data();

            this.gasAmounts.put(type, data);
        }
    }

    public List<LiftingGasHolder> getLiftingGasHolders() {
        final List<LiftingGasHolder> holders = new ObjectArrayList<>();

        for (final Map.Entry<LiftingGasType, LiftingGasData> entry : this.gasAmounts.entrySet()) {
            holders.add(new LiftingGasHolder(entry.getKey(), entry.getValue()));
        }

        return holders;
    }
}
