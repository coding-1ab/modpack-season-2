package org.antarcticgardens.cna.fabric.compat.rei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.rei.category.sequencedAssembly.ReiSequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import org.antarcticgardens.cna.compat.RecipeViewerEnergiserRenderer;
import org.antarcticgardens.cna.compat.RenderingUtil;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;
import org.antarcticgardens.cna.util.StringFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class ReiEnergiserSubcategory extends ReiSequencedAssemblySubCategory {
    private final AnimatedKinetics energiser;

    public ReiEnergiserSubcategory() {
        super(25);
        
        energiser = new AnimatedKinetics() {
            private final RecipeViewerEnergiserRenderer renderer = new RecipeViewerEnergiserRenderer(this::blockElement);

            @Override
            public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
                renderer.draw(graphics, xOffset, yOffset);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
                draw(guiGraphics, getPos().getX(), getPos().getY());
            }

            @Override
            public List<? extends GuiEventListener> children() {
                return new ArrayList<>();
            }
        };
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        PoseStack ms = graphics.pose();
        energiser.offset = index;
        ms.pushPose();
        ms.translate(-7, 50, 0);
        ms.scale(.75f, .75f, .75f);
        energiser.draw(graphics, getWidth() / 2, 0);
        ms.popPose();
        RenderingUtil.drawCenteredStringWithoutShadow(graphics, Minecraft.getInstance().font, 
                Component.literal(StringFormatUtil.formatLong(((EnergisingRecipe) recipe.getAsAssemblyRecipe()).getEnergyNeeded()) + " ⚡"), 13, 20, 0x1166ff);
    }
}
