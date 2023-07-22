package org.antarcticgardens.newage;

import com.simibubi.create.foundation.ponder.PonderRegistrationHelper;
import net.fabricmc.api.ClientModInitializer;
import org.antarcticgardens.newage.content.energiser.EnergiserPonder;

public class CreateNewAgeClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {

        // ponders
        PonderRegistrationHelper helper = new PonderRegistrationHelper(CreateNewAge.MOD_ID);
        helper.addStoryBoard(NewAgeBlocks.ENERGISER_T1, "energiser", EnergiserPonder::ponder);

    }
}
