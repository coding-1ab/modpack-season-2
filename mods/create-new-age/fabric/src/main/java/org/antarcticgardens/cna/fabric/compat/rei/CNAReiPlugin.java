package org.antarcticgardens.cna.fabric.compat.rei;

import com.simibubi.create.compat.rei.ItemIcon;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayCategory;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import me.shedaniel.rei.api.common.util.EntryStacks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.crafting.Recipe;
import org.antarcticgardens.cna.CNABlocks;
import org.antarcticgardens.cna.CreateNewAge;
import org.antarcticgardens.cna.content.energising.recipe.EnergisingRecipe;
import org.antarcticgardens.cna.util.StringFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class CNAReiPlugin implements REIClientPlugin {
    public static final CategoryIdentifier<EnergiserDisplay> identifier = () -> new ResourceLocation(CreateNewAge.MOD_ID, "rei_plugin");

    @Override
    public void registerCategories(CategoryRegistry registry) {
        DisplayCategory<EnergiserDisplay> energisingCategory = new DisplayCategory<>() {
            @Override
            public List<Widget> setupDisplay(EnergiserDisplay display, Rectangle bounds) {
                Point origin = new Point(bounds.x, bounds.y + 5);

                List<Widget> widgets = new ArrayList<>();
                widgets.add(Widgets.createRecipeBase(bounds));
                widgets.add(Widgets.createSlot(new Point(origin.x + 26, origin.y)).markInput().entries(EntryIngredients.ofIngredient(display.recipe.getIngredients().get(0))));
                widgets.add(Widgets.createArrow(new Point(origin.x + 50, origin.y)));
                widgets.add(Widgets.createLabel(new Point(origin.x + 60, origin.y + 20), Component.literal(StringFormatUtil.formatLong(display.recipe.getEnergyNeeded()) + " ⚡")).color(0x1166ff).noShadow());

                var results = display.recipe.getRollableResults();
                for (int outputIndex = 0; outputIndex < results.size(); outputIndex++) {
                    int xOffset = outputIndex % 2 == 0 ? 0 : 19;
                    widgets.add(Widgets.createSlot(new Point((origin.x + xOffset) + 80, origin.y)).markOutput().entry(EntryStack.of(VanillaEntryTypes.ITEM, results.get(outputIndex).getStack())));
                }

                return widgets;
            }

            @Override
            public CategoryIdentifier<? extends EnergiserDisplay> getCategoryIdentifier() {
                return identifier;
            }

            @Override
            public Component getTitle() {
                return Component.translatable("emi.category.create_new_age.energising");
            }

            @Override
            public Renderer getIcon() {
                return new ItemIcon(CNABlocks.ADVANCED_ENERGISER::asStack);
            }

            @Override
            public int getDisplayHeight() {
                return 36;
            }
        };

        registry.add(energisingCategory);

        registry.addWorkstations(identifier, EntryStacks.of(CNABlocks.BASIC_ENERGISER), EntryStacks.of(CNABlocks.ADVANCED_ENERGISER), EntryStacks.of(CNABlocks.REINFORCED_ENERGISER));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        for (Recipe<Container> containerRecipe : registry.getRecipeManager().getAllRecipesFor(EnergisingRecipe.TYPE.getType())) {
            registry.add(new EnergiserDisplay((EnergisingRecipe) containerRecipe));
        }
    }
}
