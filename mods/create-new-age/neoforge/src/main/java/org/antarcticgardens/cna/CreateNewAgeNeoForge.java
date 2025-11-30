package org.antarcticgardens.cna;


import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.antarcticgardens.cna.content.electricity.network.NetworkTicker;
import org.antarcticgardens.cna.content.energising.EnergiserBlockEntity;
import org.antarcticgardens.cna.content.motor.MotorBlockEntity;
import org.antarcticgardens.cna.content.nuclear.reactor.fuelacceptor.ReactorFuelAcceptorBlockEntity;
import org.antarcticgardens.cna.neoforge.content.nuclear.reactor.fuelacceptor.NeoForgeReactorFuelAcceptorBlockEntity;
import org.antarcticgardens.cna.neoforge.data.CreateNewAgeDatagenNeoForge;

@Mod(CreateNewAge.MOD_ID)
public class CreateNewAgeNeoForge extends CreateNewAge {
    public CreateNewAgeNeoForge(IEventBus eventBus, ModContainer modContainer) {
        this.initialize(new NeoForgePlatform(eventBus));
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Pre e) -> NetworkTicker.tickWorld(e.getLevel()));

//        eventBus.addListener(new CreateNewAgeClientNeoForge()::onClientSetup);
        eventBus.addListener(EventPriority.HIGHEST, CreateNewAgeDatagenNeoForge::gatherData);
        eventBus.addListener(NeoForgePlatform::registerDatapack);
        eventBus.addListener(NeoForgeReactorFuelAcceptorBlockEntity::registerCapabilities);
        eventBus.addListener(EnergiserBlockEntity::registerCapabilities);
        eventBus.addListener(MotorBlockEntity::registerCapabilities);
    }
}
