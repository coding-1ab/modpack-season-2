package dev.propulsionteam.propulsionsimulated.ponder;

import java.util.List;

import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.content.redstone.analogLever.AnalogLeverBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;

import dev.propulsionteam.propulsionsimulated.PropulsionConfig;
import dev.propulsionteam.propulsionsimulated.content.thruster.ion_thruster.IonThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.content.thruster.vector_thruster.VectorThrusterBlockEntity;
import dev.propulsionteam.propulsionsimulated.particles.ion.IonParticleData;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.createmod.ponder.api.scene.Selection;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Ponder for the Forge-Energy vector thruster (see {@code ponder_vector_thruster_normal.nbt}).
 * <p>
 * Sections: setup and reveal → intro + FE storage + generator → moving / links / copy → thrust on → thrust off.
 */
public final class VectorThrusterFeScenes {
    private VectorThrusterFeScenes() {
    }

    public static void vectorThrusterFe(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("ponder_vector_thruster_normal", "Vector Thruster (Forge Energy)");
        scene.configureBasePlate(0, 0, 7);
        scene.showBasePlate();
        scene.idle(12);

        BlockPos motorPos = util.grid().at(4, 1, 3);
        Selection floor = util.select().fromTo(0, 0, 0, 6, 0, 6);
        Selection machineLayer = util.select()
            .position(3, 1, 3)
            .add(util.select().position(4, 1, 3))
            .add(util.select().position(5, 1, 3))
            .add(util.select().fromTo(1, 1, 2, 1, 1, 5))
            .add(util.select().fromTo(2, 1, 2, 2, 1, 5));
        Selection thrusterSel = util.select().position(3, 2, 3);
        BlockPos thrusterPos = util.grid().at(3, 2, 3);
        // Analog levers beside the thruster (ponder_vector_thruster_normal.nbt, column x = 1).
        BlockPos leverWestPos = util.grid().at(1, 1, 2);
        BlockPos leverEastPos = util.grid().at(1, 1, 3);
        BlockPos leverDownPos = util.grid().at(1, 1, 4);
        BlockPos leverUpPos = util.grid().at(1, 1, 5);
        LeverControl leverWest = new LeverControl(util.select().position(leverWestPos), leverWestPos);
        LeverControl leverEast = new LeverControl(util.select().position(leverEastPos), leverEastPos);
        LeverControl leverDown = new LeverControl(util.select().position(leverDownPos), leverDownPos);
        LeverControl leverUp = new LeverControl(util.select().position(leverUpPos), leverUpPos);
        Selection kineticNet = util.select().position(4, 1, 3);

        // ── Section 1: reveal layout (floor, FE line, thruster) + spin pump ~32 RPM ─────────────
        scene.world().showSection(floor, Direction.DOWN);
        scene.idle(8);
        Block pumpBlock = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath("create", "mechanical_pump"));
        BlockState pumpState = pumpBlock.defaultBlockState()
            .setValue(DirectionalKineticBlock.FACING, Direction.WEST);
        scene.world().setBlock(motorPos, pumpState, false);
        scene.idle(6);
        scene.world().showSection(machineLayer, Direction.DOWN);
        scene.idle(10);
        scene.world().showSection(thrusterSel, Direction.DOWN);
        scene.idle(12);
        scene.world().setKineticSpeed(kineticNet, 32.0f);
        scene.effects().rotationSpeedIndicator(motorPos);
        scene.idle(20);

        // ── Section 2: main intro (on thruster) ─────────────────────────────────────────────────
        scene.overlay().showText(90)
            .attachKeyFrame()
            .sharedText("vector_thruster_fe.intro.main")
            .pointAt(util.vector().centerOf(thrusterPos))
            .placeNearTarget();
        scene.idle(100);
        scene.idle(25);

        // ── Section 3: FE buffer + coral generator below ───────────────────────────────────────
        scene.overlay().showText(85)
            .attachKeyFrame()
            .sharedText("vector_thruster_fe.intro.fe")
            .pointAt(util.vector().centerOf(thrusterPos))
            .placeNearTarget();
        scene.idle(90);
        int feCap = PropulsionConfig.ION_THRUSTER_ENERGY_CAPACITY_FE.get();
        scene.overlay().showText(90)
            .attachKeyFrame()
            .sharedText("vector_thruster_fe.intro.fe_storage", feCap)
            .pointAt(util.vector().centerOf(thrusterPos))
            .placeNearTarget();
        scene.idle(95);
        scene.world().modifyBlockEntityNBT(thrusterSel, IonThrusterBlockEntity.class, nbt -> nbt.putInt("EnergyStored", feCap));
        scene.idle(10);
        scene.overlay().showText(95)
            .attachKeyFrame()
            .sharedText("vector_thruster_fe.intro.generators")
            .pointAt(util.vector().blockSurface(util.grid().at(3, 1, 3), Direction.DOWN))
            .placeNearTarget();
        scene.idle(100);

        // ── Section 4: analog levers + vector tilt (ponder does not tick BEs / links — thruster synced after) ─
        scene.overlay().showText(85)
            .attachKeyFrame()
            .sharedText("vector_thruster_fe.moving.intro")
            .pointAt(util.vector().centerOf(util.grid().at(3, 2, 3)))
            .placeNearTarget();
        scene.idle(90);
        runVectorCircle(scene, thrusterPos, leverWest, leverEast, leverDown, leverUp, 40);
        animateVectorTransition(scene, thrusterPos, leverWest, leverEast, leverDown, leverUp,
            new Step(15, 0, 0, 15), new Step(0, 0, 0, 0), 24);
        scene.idle(25);

        // ── Section 5: rotate view + highlight embedded redstone link sides ────────────────────
        scene.overlay().showText(95)
            .attachKeyFrame()
            .sharedText("vector_thruster_fe.moving.text_3")
            .pointAt(util.vector().centerOf(thrusterPos))
            .placeNearTarget();
        scene.idle(45);
        scene.effects().indicateSuccess(thrusterPos.west());
        scene.idle(8);
        scene.effects().indicateSuccess(thrusterPos.north());
        scene.idle(30);
        scene.rotateCameraY(180);
        scene.idle(15);
        scene.effects().indicateSuccess(thrusterPos.east());
        scene.idle(8);
        scene.effects().indicateSuccess(thrusterPos.south());
        scene.idle(25);
        scene.rotateCameraY(-180);
        scene.idle(100);

        scene.overlay().showText(90)
            .sharedText("vector_thruster_fe.moving.text_1")
            .pointAt(util.vector().centerOf(util.grid().at(3, 2, 3)))
            .placeNearTarget();
        scene.idle(100);
        // ── Section 6: thrust power (redstone) + ion plume, then power down ────────────────────
        scene.overlay().showText(90)
            .attachKeyFrame()
            .sharedText("vector_thruster_fe.powering.intro")
            .pointAt(util.vector().centerOf(util.grid().at(3, 2, 3)))
            .placeNearTarget();
        scene.idle(95);
        scene.world().modifyBlockEntityNBT(thrusterSel, IonThrusterBlockEntity.class, nbt -> {
            nbt.putInt("EnergyStored", feCap);
            nbt.putInt("RedstoneInput", 15);
        });
        scene.effects().indicateRedstone(util.grid().at(3, 2, 3));
        emitIonBurst(scene, util, 3, 2, 3, Direction.UP, 10);
        scene.idle(100);
        scene.world().modifyBlockEntityNBT(thrusterSel, IonThrusterBlockEntity.class, nbt -> nbt.putInt("RedstoneInput", 0));
        scene.idle(35);
        scene.markAsFinished();
    }

    private record LeverControl(Selection selection, BlockPos pos) {
    }

    private record Step(int w, int e, int d, int u) {
    }

    private static void setLevelPower(CreateSceneBuilder scene,
        LeverControl west, LeverControl east, LeverControl down, LeverControl up,
        int w, int e, int d, int u) {
        setLeverState(scene, west, w);
        setLeverState(scene, east, e);
        setLeverState(scene, down, d);
        setLeverState(scene, up, u);
    }

    private static void setLeverState(CreateSceneBuilder scene, LeverControl lever, int power) {
        int clampedPower = Math.clamp(power, 0, 15);
        scene.world().modifyBlockEntityNBT(lever.selection(), AnalogLeverBlockEntity.class,
            nbt -> nbt.putInt("State", clampedPower));
    }

    private static void runVectorCircle(CreateSceneBuilder scene, BlockPos thrusterPos,
        LeverControl leverWest, LeverControl leverEast, LeverControl leverDown, LeverControl leverUp, int stepTicks) {
        Step[] loop = {
            new Step(15, 0, 0, 0),
            new Step(15, 0, 15, 0),
            new Step(0, 0, 15, 0),
            new Step(0, 15, 15, 0),
            new Step(0, 15, 0, 0),
            new Step(0, 15, 0, 15),
            new Step(0, 0, 0, 15),
            new Step(15, 0, 0, 15),
        };
        Step previous = new Step(0, 0, 0, 0);
        for (Step s : loop) {
            animateVectorTransition(scene, thrusterPos, leverWest, leverEast, leverDown, leverUp, previous, s, stepTicks);
            previous = s;
        }
    }

    private static void animateVectorTransition(CreateSceneBuilder scene, BlockPos thrusterPos,
        LeverControl leverWest, LeverControl leverEast, LeverControl leverDown, LeverControl leverUp,
        Step from, Step to, int ticks) {
        int frames = Math.max(1, ticks / 2);
        int ticksPerFrame = Math.max(1, ticks / frames);
        for (int frame = 1; frame <= frames; frame++) {
            float progress = frame / (float) frames;
            int w = lerpPower(from.w, to.w, progress);
            int e = lerpPower(from.e, to.e, progress);
            int d = lerpPower(from.d, to.d, progress);
            int u = lerpPower(from.u, to.u, progress);
            setLevelPower(scene, leverWest, leverEast, leverDown, leverUp, w, e, d, u);
            if (frame == frames || frame % 8 == 0) {
                indicateLeverPower(scene, leverWest, w);
                indicateLeverPower(scene, leverEast, e);
                indicateLeverPower(scene, leverDown, d);
                indicateLeverPower(scene, leverUp, u);
            }
            float vx = (w - e) / 15.0f;
            float vy = (d - u) / 15.0f;
            scene.world().modifyBlockEntity(thrusterPos, VectorThrusterBlockEntity.class, be -> {
                be.applyVectorSignalsForScene(vx, vy);
                be.animateVectorForScene();
            });
            scene.idle(ticksPerFrame);
        }
    }

    private static void indicateLeverPower(CreateSceneBuilder scene, LeverControl lever, int power) {
        if (power > 0) {
            scene.effects().indicateRedstone(lever.pos().below());
        }
    }

    private static int lerpPower(int from, int to, float progress) {
        return Math.clamp(Math.round(from + (to - from) * progress), 0, 15);
    }

    private static void emitIonBurst(CreateSceneBuilder scene, SceneBuildingUtil util, int x, int y, int z,
        Direction exhaustDirection, int particlesPerTick) {
        scene.effects().emitParticles(util.vector().centerOf(util.grid().at(x, y, z)).add(0, 0.4, 0), (world, px, py, pz) -> {
            for (int i = 0; i < particlesPerTick; i++) {
                double lateral = 0.08;
                double ox = (world.random.nextDouble() - 0.5) * lateral;
                double oy = (world.random.nextDouble() - 0.5) * lateral;
                double oz = (world.random.nextDouble() - 0.5) * lateral;
                switch (exhaustDirection.getAxis()) {
                    case X -> ox = 0.0;
                    case Y -> oy = 0.0;
                    case Z -> oz = 0.0;
                    default -> {
                    }
                }
                double vx = exhaustDirection.getStepX() * 0.55 + (world.random.nextDouble() - 0.5) * 0.02;
                double vy = exhaustDirection.getStepY() * 0.55 + (world.random.nextDouble() - 0.5) * 0.02;
                double vz = exhaustDirection.getStepZ() * 0.55 + (world.random.nextDouble() - 0.5) * 0.02;
                world.addParticle(new IonParticleData(List.of(), null, 0.55f), px + ox, py + oy, pz + oz, vx, vy, vz);
            }
        }, 1, 70);
    }
}
