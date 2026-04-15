package dev.eriksonn.aeronautics.index;

import dev.simulated_team.simulated.index.SimClickInteractions;
import dev.eriksonn.aeronautics.api.levitite_blend_crystallization.LevititeCatalyzerHandler;

public class AeroClickInteractions extends SimClickInteractions {

	public static LevititeCatalyzerHandler LEVITITE_CATALYZER_HANDLER = register(new LevititeCatalyzerHandler());

	public static void init() {

	}
}
