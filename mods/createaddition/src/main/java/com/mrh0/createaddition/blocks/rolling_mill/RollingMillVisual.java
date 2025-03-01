package com.mrh0.createaddition.blocks.rolling_mill;

import com.simibubi.create.content.kinetics.base.KineticBlockEntityVisual;
import com.simibubi.create.content.kinetics.base.RotatingInstance;
import dev.engine_room.flywheel.api.instance.Instance;
import dev.engine_room.flywheel.api.visualization.VisualizationContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RollingMillVisual extends KineticBlockEntityVisual<RollingMillBlockEntity> {
    protected RotatingInstance rotatingModel1;
    protected RotatingInstance rotatingModel2;

    public RollingMillVisual(VisualizationContext context, RollingMillBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        this.rotatingModel1 = this.setup((RotatingInstance)this.getModel().createInstance());
        this.rotatingModel2 = this.setup((RotatingInstance)this.getModel().createInstance());

        rotatingModel1.setRotationAxis(axis)
                .setRotationalSpeed(getBlockEntitySpeed())
                .setRotationOffset(-getRotationOffset(axis))
                .setColor(blockEntity)
                .setPosition(getInstancePosition());

        rotatingModel2.setRotationAxis(axis)
                .setRotationalSpeed(getBlockEntitySpeed())
                .setRotationOffset(-getRotationOffset(axis))
                .setColor(blockEntity)
                .setPosition(getInstancePosition())
                .nudge(0, 4f/16f, 0)
                .setRotationalSpeed(-getBlockEntitySpeed());
    }

    @Override
    public void update(float v) {
        this.updateRotation(this.rotatingModel1);
        this.updateRotation(this.rotatingModel2);
        rotatingModel2.setRotationalSpeed(-getBlockEntitySpeed());
    }

    public void updateLight(float v) {
        this.relight(this.pos, this.rotatingModel1, this.rotatingModel2);
    }

    @Override
    public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
        consumer.accept(rotatingModel1);
        consumer.accept(rotatingModel2);
    }

    @Override
    protected void _delete() {
        rotatingModel1.delete();
        rotatingModel2.delete();
    }
}
