package org.antarcticgardens.cna;

import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.antarcticgardens.cna.forge.ForgeRegistrar;
import org.antarcticgardens.cna.forge.compat.jei.ForgeJeiEnergisingSubcategory;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

import java.util.function.Supplier;

public class ForgePlatform extends Platform {
    private final ForgeRegistrar registrationHelper = new ForgeRegistrar();
    
    @Override
    public PlatformRegistrar getRegistrar() {
        return registrationHelper;
    }

    @Override
    public void commonSetup(Runnable commonSetup) {
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent e) -> e.enqueueWork(commonSetup));
    }

    @Override
    public Object getEnergisingRecipeSubCategory() {
        return (Supplier<Supplier<SequencedAssemblySubCategory>>) () -> ForgeJeiEnergisingSubcategory::new;
    }
}
