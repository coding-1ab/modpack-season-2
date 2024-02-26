package org.antarcticgardens.cna.fabric.compat.jei;

import com.simibubi.create.compat.jei.category.sequencedAssembly.JeiSequencedAssemblySubCategory;
import com.simibubi.create.content.processing.sequenced.SequencedRecipe;
import net.minecraft.client.gui.GuiGraphics;
import org.antarcticgardens.cna.compat.jei.JeiEnergisingSubcategory;

public class FabricJeiEnergisingSubcategory extends JeiSequencedAssemblySubCategory {
    private final JeiEnergisingSubcategory subcategory = new JeiEnergisingSubcategory();
    
    public FabricJeiEnergisingSubcategory() {
        super(JeiEnergisingSubcategory.WIDTH);
    }

    @Override
    public void draw(SequencedRecipe<?> recipe, GuiGraphics graphics, double mouseX, double mouseY, int index) {
        subcategory.draw(recipe, graphics, mouseX, mouseY, index);
    }
}
