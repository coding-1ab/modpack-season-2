package plus.dragons.createdragonlib;

import net.minecraftforge.fml.ModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DragonLib {
    private static final Logger LOGGER = LogManager.getLogger();

    public DragonLib() {
        LOGGER.info("Create: Dragon Lib Legacy " +
            ModLoadingContext.get().getActiveContainer().getModInfo().getVersion() +
            " has initialized, ready to support CEI!"
        );

/*        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(EventPriority.LOWEST, DragonLib::gatherData);*/
    }

/*    public static void gatherData(GatherDataEvent event) {
        TagGen.genAll();
    }*/

}
