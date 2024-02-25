package org.antarcticgardens.cna;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.antarcticgardens.cna.content.electricity.network.NetworkTicker;
import org.antarcticgardens.cna.forge.data.CreateNewAgeDatagenForge;

@Mod(CreateNewAge.MOD_ID)
public class CreateNewAgeForge extends CreateNewAge {
    public CreateNewAgeForge() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.LOWEST, new CreateNewAgeDatagenForge()::gatherData);
        
        this.initialize(new ForgePlatform());
        MinecraftForge.EVENT_BUS.addListener((TickEvent.LevelTickEvent e) -> NetworkTicker.tickWorld(e.level));
    }
}
