package org.antarcticgardens.cna;

import com.simibubi.create.compat.recipeViewerCommon.SequencedAssemblySubCategoryType;
import org.antarcticgardens.cna.fabric.FabricRegistrar;
import org.antarcticgardens.cna.fabric.compat.emi.EmiEnergisingSubcategory;
import org.antarcticgardens.cna.fabric.compat.jei.FabricJeiEnergisingSubcategory;
import org.antarcticgardens.cna.fabric.compat.rei.ReiEnergiserSubcategory;
import org.antarcticgardens.cna.platform.PlatformRegistrar;

public class FabricPlatform extends Platform {
    private final FabricRegistrar registrar = new FabricRegistrar();

    @Override
    public PlatformRegistrar getRegistrar() {
        return registrar;
    }

    @Override
    public void commonSetup(Runnable commonSetup) {
        commonSetup.run();
    }

    @Override
    public Object getEnergisingRecipeSubCategory() {
        return new SequencedAssemblySubCategoryType(
                () -> FabricJeiEnergisingSubcategory::new,
                () -> ReiEnergiserSubcategory::new,
                () -> EmiEnergisingSubcategory::new
        );
    }
}
