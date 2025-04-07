package plus.dragons.createenchantmentindustry.client.ponder.scene;

import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.content.processing.basin.BasinBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import plus.dragons.createdragonsplus.common.registry.CDPFluids;
import plus.dragons.createenchantmentindustry.common.registry.CEIFluids;

public class MiscScene {
    public static void experienceHatch(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("intro", "Introduction to Experience Hatch");
        scene.configureBasePlate(0, 0, 4);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(3, 1, 1, 1, 1, 3), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(3, 2, 2, 2, 3, 3), Direction.DOWN);
        scene.world().showSection(util.select().position(1, 3, 2), Direction.DOWN);
        scene.idle(5);

        scene.overlay().showText(50)
                .text("It is very simple to use. Right click Hatch to store Experience...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        var frontVec = util.vector().blockSurface(util.grid().at(2,3,2), Direction.WEST)
                .add(-.125, 0, 0);
        scene.overlay().showControls(frontVec, Pointing.UP, 50).rightClick();
        scene.idle(10);
        scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), FluidTankBlockEntity.class,
                be ->  be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 10000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(40);

        scene.world().modifyBlockEntity(util.grid().at(3, 2, 1), BasinBlockEntity.class,
                be ->  be.inputTank.getPrimaryHandler().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(10);
        scene.world().showSection(util.select().fromTo(3, 2, 1, 2, 2, 1), Direction.UP);
        scene.idle(5);
        scene.overlay().showText(60)
                .text("...Shift-Right-Click Hatch to extract stored Experience")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 1));
        frontVec = util.vector().blockSurface(util.grid().at(3,2,1), Direction.WEST)
                .add(-.125, 0, 0);
        scene.overlay().showControls(frontVec, Pointing.UP, 50).rightClick();
        scene.idle(30);
        scene.world().modifyBlockEntity(util.grid().at(3, 2, 1), BasinBlockEntity.class,
                be ->  be.inputTank.getPrimaryHandler().drain(new FluidStack(CEIFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(30);

        scene.overlay().showText(55)
                .text("There are a filter slot and a scroll panel on Hatch. You can configure how much Experience per interaction on the panel")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        for(int i=0;i<12;i++){
            scene.world().modifyBlockEntity(util.grid().at(2, 3, 2), FluidTankBlockEntity.class,
                    be ->  be.getControllerBE().getTankInventory().fill(new FluidStack(CEIFluids.EXPERIENCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(5);
        }

        scene.overlay().showText(40)
                .text("The filter slot is used to deal with experience fluids of other mods")
                .attachKeyFrame()
                .placeNearTarget()
                .pointAt(util.vector().centerOf(1, 3, 2));
        scene.idle(30);
        scene.world().showSection(util.select().position(0, 1, 1), Direction.DOWN);
        scene.idle(10);

        scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                be ->  be.getControllerBE().getTankInventory().fill(new FluidStack(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).get(), 36000), IFluidHandler.FluidAction.EXECUTE));
        scene.idle(10);
        scene.overlay().showText(40)
                .text("Let's assume that Liquid Cyan Dye is experience fluid from another mod...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        scene.idle(40);
        scene.overlay().showText(40)
                .text("...And place Bucket Cyan Dye in filter slot")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        scene.overlay().showControls(util.vector().centerOf(0, 1, 1), Pointing.DOWN, 40).withItem(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).getBucket().get().getDefaultInstance());
        scene.idle(40);

        scene.overlay().showText(60)
                .text("Now, you can directly save and extract your experience as 'The Cyan Experience'!")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(0, 1, 1));
        for(int i=0;i<12;i++){
            scene.world().modifyBlockEntity(util.grid().at(1, 1, 1), FluidTankBlockEntity.class,
                    be ->  be.getControllerBE().getTankInventory().drain(new FluidStack(CDPFluids.DYES_BY_COLOR.get(DyeColor.CYAN).get(), 3000), IFluidHandler.FluidAction.EXECUTE));
            scene.idle(5);
        }
    }

}
