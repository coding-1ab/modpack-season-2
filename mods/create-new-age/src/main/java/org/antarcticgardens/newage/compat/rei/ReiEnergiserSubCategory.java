package org.antarcticgardens.newage.compat.rei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.compat.rei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.rei.category.sequencedAssembly.ReiSequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.antarcticgardens.newage.content.energiser.EnergisingRecipe;
import org.antarcticgardens.newage.tools.StringFormattingTool;

public class ReiEnergiserSubCategory extends ReiSequencedAssemblySubCategory {

    private final AnimatedKinetics energiser;

    public ReiEnergiserSubCategory() {
        super(25);
        energiser = new NotAnimatedEnergiser();
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        PoseStack ms = graphics.pose();
        energiser.offset = index;
        ms.pushPose();
        ms.translate(-5, 50, 0);
        ms.scale(.6f, .6f, .6f);
        energiser.draw(graphics, getWidth() / 2, 0);
        graphics.drawCenteredString(Minecraft.getInstance().font, Component.literal(StringFormattingTool.formatLong(((EnergisingRecipe)recipe.getAsAssemblyRecipe()).energyNeeded) + " ⚡"), -5, 50, 0x1166ff);
        ms.popPose();
    }
}
