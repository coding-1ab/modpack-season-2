package org.antarcticgardens.cna.fabric.compat.emi;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.emi.CreateEmiAnimations;
import com.simibubi.create.compat.emi.EmiSequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import dev.emi.emi.api.widget.DrawableWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.antarcticgardens.cna.compat.RecipeViewerEnergiserRenderer;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;
import org.antarcticgardens.cna.util.StringFormatUtil;

public class EmiEnergisingSubcategory extends EmiSequencedAssemblySubCategory {
    public EmiEnergisingSubcategory() {
        super(25);
    }

    @Override
    public void addWidgets(WidgetHolder widgets, int x, int y, SequencedRecipe<?> recipe, int index) {
        widgets.addDrawable(x, y, getWidth(), 96, new DrawableWidget.DrawableWidgetConsumer() {
            private final RecipeViewerEnergiserRenderer renderer = new RecipeViewerEnergiserRenderer(CreateEmiAnimations::blockElement);
            
            @Override
            public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
                PoseStack matrices = graphics.pose();
                matrices.pushPose();
                matrices.translate(3, 54, 0);
                matrices.scale(.75f, .75f, .75f);
                renderer.draw(graphics, 0, 0);
                matrices.popPose();
            }
        }).tooltip(getTooltip(recipe, index));

        widgets.addText(Component.literal(StringFormatUtil.formatLong(((EnergisingRecipe) recipe.getRecipe()).getEnergyNeeded()) + " ⚡"),
                x, 20, 0x1166ff, false);
    }
}
