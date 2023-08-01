package org.antarcticgardens.newage.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.compat.jei.category.sequencedAssembly.JeiSequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.antarcticgardens.newage.NewAgeBlocks;
import org.antarcticgardens.newage.content.energiser.EnergisingRecipe;
import org.antarcticgardens.newage.tools.StringFormattingTool;

public class JeiEnergisingSubcategory extends JeiSequencedAssemblySubCategory {

    AnimatedKinetics energiser;

    public JeiEnergisingSubcategory() {
        super(25);
        energiser = new AnimatedKinetics() {
            @Override
            public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
                PoseStack matrixStack = graphics.pose();
                matrixStack.pushPose();
                matrixStack.translate(xOffset, yOffset, 200);
                matrixStack.mulPose(Axis.XP.rotationDegrees(-15.5f));
                matrixStack.mulPose(Axis.YP.rotationDegrees(22.5f));

                blockElement(shaft(Direction.Axis.Z))
                        .rotateBlock(0, 0, getCurrentAngle())
                        .scale(24)
                        .render(graphics);

                blockElement(NewAgeBlocks.ENERGISER_T1.getDefaultState())
                        .scale(24)
                        .render(graphics);


                matrixStack.popPose();
            }
        };
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        PoseStack ms = graphics.pose();
        energiser.offset = index;
        ms.pushPose();
        ms.translate(-5, 50, 0);
        ms.scale(.6f, .6f, .6f);
        energiser.draw(graphics, getWidth() / 2, 0);
        ms.popPose();
        graphics.drawString(Minecraft.getInstance().font, Component.literal(StringFormattingTool.formatLong(((EnergisingRecipe)recipe.getAsAssemblyRecipe()).energyNeeded) + " ⚡"), 0, 20, 0x1166ff, false);
    }
}
