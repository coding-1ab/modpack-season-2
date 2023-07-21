package org.antarcticgardens.newage;

import com.simibubi.create.content.processing.recipe.ProcessingRecipeSerializer;
import net.fabricmc.api.ModInitializer;

import org.antarcticgardens.newage.content.energiser.EnergiserBlock;
import org.antarcticgardens.newage.content.energiser.EnergisingRecipe;
import org.antarcticgardens.newage.tools.RecipeTool;
import org.antarcticgardens.newage.tools.RegistryTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateNewAge implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("create_new_age");

	public static final String MOD_ID = "create_new_age";

	@Override
	public void onInitialize() {
		LOGGER.info("Hello 1.20.1 Create!");

		RegistryTool.start();

		new EnergiserBlock(1);
		EnergisingRecipe.type = RecipeTool.createIRecipeTypeInfo("energising", new ProcessingRecipeSerializer<>(EnergisingRecipe::new));

//		new CarbonBrushesBlock()

		RegistryTool.finish();
	}
}