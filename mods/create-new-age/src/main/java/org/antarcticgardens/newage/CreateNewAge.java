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
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("create-overcharged");

	public static final String MOD_ID = "create_new_age";

	@Override
	public void onInitialize() {
		LOGGER.info("Hello 1.20.1 Create!");

		RegistryTool.start();
		new EnergiserBlock(1);
		EnergisingRecipe.type = RecipeTool.createIRecipeInfo("energising", new ProcessingRecipeSerializer<>(EnergisingRecipe::new));
		RegistryTool.finish();
	}
}