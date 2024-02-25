package org.antarcticgardens.cna.compat.jei;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.compat.jei.category.animations.AnimatedKinetics;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import org.antarcticgardens.cna.CNABlocks;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;
import org.antarcticgardens.cna.util.StringFormatUtil;

#if CNA_FABRIC
import com.simibubi.create.compat.jei.category.sequencedAssembly.JeiSequencedAssemblySubCategory;
#else
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
#endif

#if CNA_FABRIC
public class JeiEnergisingSubcategory extends JeiSequencedAssemblySubCategory {
#else
public class JeiEnergisingSubcategory extends SequencedAssemblySubCategory {
#endif
    private final AnimatedKinetics energiser;

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
                        .scale(20)
                        .render(graphics);

                blockElement(CNABlocks.BASIC_ENERGISER.getDefaultState())
                        .scale(20)
                        .render(graphics);

                blockElement(AllBlocks.DEPOT.getDefaultState())
                        .scale(20)
                        .atLocal(0, 2, 0)
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
        ms.translate(-7, 50, 0);
        ms.scale(.75f, .75f, .75f);
        energiser.draw(graphics, getWidth() / 2, 0);
        ms.popPose();
        graphics.drawString(Minecraft.getInstance().font, Component.literal(StringFormatUtil.formatLong(((EnergisingRecipe)recipe.getAsAssemblyRecipe()).getEnergyNeeded()) + " ⚡"), 0, 20, 0x1166ff, false);
    }
}
