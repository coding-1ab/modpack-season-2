package org.antarcticgardens.cna.fabric.compat.emi;

import com.simibubi.create.content.processing.recipe.ProcessingOutput;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.network.chat.Component;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;
import org.antarcticgardens.cna.util.StringFormatUtil;

public class EnergisingEmiRecipe extends BasicEmiRecipe {

    private final int energyNeeded;

    public EnergisingEmiRecipe(EnergisingRecipe recipe) {
        super(CNAEmiPlugin.ENERGISING, recipe.getId(), 140, 30);
        this.inputs.add(EmiIngredient.of(recipe.getIngredients().get(0)));
        for (ProcessingOutput rollableResult : recipe.getRollableResults()) {
            this.outputs.add(EmiStack.of(rollableResult.getStack()));
        }
        energyNeeded = recipe.getEnergyNeeded();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(EmiTexture.EMPTY_ARROW, 26, 1);

        widgets.addSlot(inputs.get(0), 0, 0);

        Component text = Component.literal(StringFormatUtil.formatLong(energyNeeded) + " ⚡");
        widgets.addText(text, 30 - text.getString().length(), 20, 0x1166ff, false);

        int x = 58;
        for (EmiStack output : outputs) {
            widgets.addSlot(output, x, 0).recipeContext(this);
            x+=20;
        }
    }
}
