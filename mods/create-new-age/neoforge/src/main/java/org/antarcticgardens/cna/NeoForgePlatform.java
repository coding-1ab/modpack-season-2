package org.antarcticgardens.cna;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.antarcticgardens.cna.content.nuclear.reactor.fuelacceptor.ReactorFuelAcceptorBlockEntity;
import org.antarcticgardens.cna.neoforge.NeoForgeRegistrar;
import org.antarcticgardens.cna.neoforge.compat.jei.ForgeJeiEnergisingSubcategory;
import org.antarcticgardens.cna.neoforge.content.nuclear.reactor.fuelacceptor.NeoForgeReactorFuelAcceptorBlockEntity;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

import java.util.function.Supplier;

public class NeoForgePlatform extends Platform {
    private final NeoForgeRegistrar registrationHelper = new NeoForgeRegistrar();
    private final IEventBus modEventBus;

    public NeoForgePlatform(IEventBus eventBus) {
        modEventBus = eventBus;
    }

    @Override
    public PlatformRegistrar getRegistrar() {
        return registrationHelper;
    }

    @Override
    public void commonSetup(Runnable commonSetup) {
        modEventBus.addListener((FMLCommonSetupEvent e) -> e.enqueueWork(commonSetup));
    }

    @Override
    public Object getEnergisingRecipeSubCategory() {
        return (Supplier<Supplier<SequencedAssemblySubCategory>>) () -> ForgeJeiEnergisingSubcategory::new;
    }

    @Override
    public ReactorFuelAcceptorBlockEntity platformReactorFuelAcceptorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new NeoForgeReactorFuelAcceptorBlockEntity(type, pos, blockState);
    }
}
