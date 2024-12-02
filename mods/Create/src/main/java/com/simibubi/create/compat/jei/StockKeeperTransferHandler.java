package com.simibubi.create.compat.jei;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.ParametersAreNonnullByDefault;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.AllMenuTypes;
import com.simibubi.create.content.logistics.BigItemStack;
import com.simibubi.create.content.logistics.packager.InventorySummary;
import com.simibubi.create.content.logistics.stockTicker.CraftableBigItemStack;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.simibubi.create.content.logistics.stockTicker.StockKeeperRequestScreen;
import com.simibubi.create.foundation.utility.CreateLang;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.common.transfer.RecipeTransferOperationsResult;
import mezz.jei.common.transfer.RecipeTransferUtil;
import mezz.jei.library.transfer.RecipeTransferErrorMissingSlots;
import mezz.jei.library.transfer.RecipeTransferErrorTooltip;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.wrapper.RecipeWrapper;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StockKeeperTransferHandler implements IRecipeTransferHandler<StockKeeperRequestMenu, Object> {

	private IJeiHelpers helpers;

	public StockKeeperTransferHandler(IJeiHelpers helpers) {
		this.helpers = helpers;
	}

	@Override
	public Class<? extends StockKeeperRequestMenu> getContainerClass() {
		return StockKeeperRequestMenu.class;
	}

	@Override
	public Optional<MenuType<StockKeeperRequestMenu>> getMenuType() {
		return Optional.of(AllMenuTypes.STOCK_KEEPER_REQUEST.get());
	}

	@Override
	public RecipeType<Object> getRecipeType() {
		return null;
	}

	@Override
	public @Nullable IRecipeTransferError transferRecipe(StockKeeperRequestMenu container, Object object,
		IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		Level level = player.level();
		if (!(object instanceof Recipe<?> recipe))
			return null;
		MutableObject<IRecipeTransferError> result = new MutableObject<>();
		if (level.isClientSide())
			DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> result
				.setValue(transferRecipeOnClient(container, recipe, recipeSlots, player, maxTransfer, doTransfer)));
		return result.getValue();
	}

	private @Nullable IRecipeTransferError transferRecipeOnClient(StockKeeperRequestMenu container, Recipe<?> recipe,
		IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
		if (!(container.screenReference instanceof StockKeeperRequestScreen screen))
			return null;
		
		for (BigItemStack order : screen.recipesToOrder)
			if (order instanceof CraftableBigItemStack cbis && cbis.recipe == recipe)
				return new RecipeTransferErrorTooltip(CreateLang.temporaryText(
					"Already ordering this recipe")
					.component());

		if (screen.itemsToOrder.size() >= 9)
			return new RecipeTransferErrorTooltip(CreateLang.temporaryText("Order slots already full")
				.component());

		InventorySummary summary = screen.getMenu().contentHolder.getLastClientsideStockSnapshotAsSummary();
		if (summary == null)
			return null;

		Container outputDummy = new RecipeWrapper(new ItemStackHandler(9));
		List<Slot> craftingSlots = new ArrayList<>();
		for (int i = 0; i < outputDummy.getContainerSize(); i++)
			craftingSlots.add(new Slot(outputDummy, i, 0, 0));

		List<BigItemStack> stacksByCount = summary.getStacksByCount();
		Container inputDummy = new RecipeWrapper(new ItemStackHandler(stacksByCount.size()));
		Map<Slot, ItemStack> availableItemStacks = new HashMap<>();
		for (int j = 0; j < stacksByCount.size(); j++) {
			BigItemStack bigItemStack = stacksByCount.get(j);
			availableItemStacks.put(new Slot(inputDummy, j, 0, 0),
				bigItemStack.stack.copyWithCount(bigItemStack.count));
		}

		RecipeTransferOperationsResult transferOperations =
			RecipeTransferUtil.getRecipeTransferOperations(helpers.getStackHelper(), availableItemStacks,
				recipeSlots.getSlotViews(RecipeIngredientRole.INPUT), craftingSlots);

		if (!transferOperations.missingItems.isEmpty())
			return new RecipeTransferErrorMissingSlots(CreateLang.temporaryText("Required items are not in Stock")
				.component(), transferOperations.missingItems);

		if (!doTransfer)
			return null;
		
		screen.recipesToOrder.add(new CraftableBigItemStack(recipe.getResultItem(player.level()
			.registryAccess()), recipe));
		screen.searchBox.setValue("");
		screen.refreshSearchNextTick = true;
		return null;
	}

}
