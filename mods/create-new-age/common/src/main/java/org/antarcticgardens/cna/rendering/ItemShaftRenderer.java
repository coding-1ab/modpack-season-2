package org.antarcticgardens.cna.rendering;

import com.jozufozu.flywheel.core.PartialModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.Create;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModel;
import com.simibubi.create.foundation.item.render.CustomRenderedItemModelRenderer;
import com.simibubi.create.foundation.item.render.PartialItemModelRenderer;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.antarcticgardens.cna.CreateNewAge;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ItemShaftRenderer extends CustomRenderedItemModelRenderer {
    private static final PartialModel SHAFT = new PartialModel(Create.asResource("block/shaft"));

    private final Vector3f offset;
    private final Quaternionf rotation;

    ItemShaftRenderer(Vector3f offset, Quaternionf rotation) {
        this.offset = offset.div(16.0f);
        this.rotation = rotation;
    }

    @Override
    protected void render(ItemStack stack, CustomRenderedItemModel model, PartialItemModelRenderer renderer,
                          ItemDisplayContext transformType, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
        renderer.render(model.getOriginalModel(), light);

        ms.mulPose(rotation);
        ms.mulPose(Axis.YP.rotation(1.0f));
        ms.translate(offset.x, offset.y, offset.z);
        renderer.render(SHAFT.get(), light);
    }

    public static <T extends Item, P> NonNullUnaryOperator<ItemBuilder<T, P>> itemTransformer(Vector3f offset, Quaternionf rotation) {
        return b -> {
            b.onRegister(item -> CreateNewAge.getInstance().getPlatform().getRegistrar()
                    .registerCustomItemRenderer(item, new ItemShaftRenderer(offset, rotation)));
            return b;
        };
    }
}
