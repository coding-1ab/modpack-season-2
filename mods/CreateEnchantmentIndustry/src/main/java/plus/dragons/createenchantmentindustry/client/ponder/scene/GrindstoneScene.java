package plus.dragons.createenchantmentindustry.client.ponder.scene;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.ponder.CreateSceneBuilder;
import net.createmod.catnip.math.Pointing;
import net.createmod.ponder.api.PonderPalette;
import net.createmod.ponder.api.scene.SceneBuilder;
import net.createmod.ponder.api.scene.SceneBuildingUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import plus.dragons.createenchantmentindustry.client.ponder.CEIPonderScenes;
import plus.dragons.createenchantmentindustry.common.kinetics.grindstone.GrindstoneDrainBlockEntity;
import plus.dragons.createenchantmentindustry.common.registry.CEIBlocks;

public class GrindstoneScene {
    public static void basic(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_grindstone.intro", "Introduction to Mechanical Grindstone");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().fromTo(4, 1, 2, 0, 1, 2), Direction.DOWN);
        scene.idle(5);
        scene.overlay().showText(50)
                .text("To use Mechanical Grindstone, you need an Item Drain.")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(20);
        scene.overlay().showControls(util.vector().centerOf(2, 1, 2), Pointing.UP, 50).rightClick().withItem(CEIBlocks.MECHANICAL_GRINDSTONE.asStack());
        scene.idle(20);
        scene.world().setBlock(util.grid().at(2,1,2),CEIBlocks.GRINDSTONE_DRAIN.getDefaultState().setValue(HorizontalKineticBlock.HORIZONTAL_FACING,Direction.SOUTH),true);
        scene.world().modifyBlockEntity(util.grid().at(2,1,2), GrindstoneDrainBlockEntity.class, SmartBlockEntity::markVirtual);
        scene.idle(30);
        scene.overlay().showText(50)
                .text("This is a Grindstone Drain. You will need another Mechanical Grindstone to use with it")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(20);
        scene.world().showSection(util.select().position(2, 2, 2), Direction.DOWN);
        scene.overlay().showOutline(PonderPalette.GREEN, util.select().fromTo(2,1,2,2,2,2), util.select().fromTo(2,1,2,2,2,2), 30);
        scene.idle(30);
        scene.world().showSection(util.select().fromTo(0,1,0,4,2,1), Direction.DOWN);
        scene.world().showSection(util.select().fromTo(0,1,3,4,2,4), Direction.DOWN);
        scene.world().setKineticSpeed(util.select().everywhere(),-64);
        scene.world().setKineticSpeed(util.select().fromTo(2,2,2,2,2,3),64);
        scene.overlay().showText(60)
                .text("Experience in item form can be crushed into liquid form by Mechanical Grindstone")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 2, 2));
        for(int i=0;i<4;i++){
            scene.world().createItemOnBelt(util.grid().at(0, 1, 2), Direction.UP, new ItemStack(i<2? AllItems.EXP_NUGGET: AllBlocks.EXPERIENCE_BLOCK));
            scene.idle(20);
        }
    }

    public static void extra(SceneBuilder builder, SceneBuildingUtil util) {
        CreateSceneBuilder scene = new CreateSceneBuilder(builder);
        scene.title("mechanical_grindstone.extra", "Mechanical Grindstone, not just Grindstone");
        scene.configureBasePlate(0, 0, 5);
        scene.showBasePlate();
        scene.idle(5);
        scene.world().showSection(util.select().everywhere(), Direction.DOWN);

        scene.idle(5);
        scene.overlay().showText(60)
                .text("Mechanical Grindstone not only has all the features of a grindstone...")
                .placeNearTarget()
                .pointAt(util.vector().centerOf(2, 2, 2));
        scene.world().setKineticSpeed(util.select().everywhere(),128);
        scene.world().setKineticSpeed(util.select().position(2,2,2),-128);
        scene.world().setKineticSpeed(util.select().fromTo(0,1,0,3,1,0),-128);
        scene.idle(10);
        var sword = new ItemStack(Items.DIAMOND_SWORD);
        CEIPonderScenes.enchant(scene,sword,Enchantments.SWEEPING_EDGE,3);
        scene.world().createItemOnBelt(util.grid().at(0, 1, 0), Direction.UP, sword);
        scene.idle(55);

        scene.overlay().showText(60)
                .text("...but it also has the features of sandpaper.")
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(10);
        scene.world().createItemOnBelt(util.grid().at(0, 1, 0), Direction.UP, new ItemStack(AllItems.ROSE_QUARTZ.get()));
        scene.idle(55);

        scene.overlay().showText(60)
                .text("You can use Mechanical Grindstone with item in your hand. 100% safe and won't hurt your hands :D")
                .colored(PonderPalette.GREEN)
                .placeNearTarget()
                .attachKeyFrame()
                .pointAt(util.vector().centerOf(2, 1, 2));
        scene.idle(60);
    }
}
