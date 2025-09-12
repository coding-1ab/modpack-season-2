package vectorwing.farmersdelight.common.mixin;

import com.google.common.collect.ImmutableMap;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import vectorwing.farmersdelight.common.registry.ModItems;

import java.util.Map;

@Mixin(Villager.class)
public class VillagerFoodPointsMixin {
    @Shadow
    @Final
    @Mutable
    private static Map<Item, Integer> FOOD_POINTS;

    @Inject(method = "<clinit>", at = @At("RETURN"))
    private static void addMoreFoods(CallbackInfo ci) {
        ImmutableMap.Builder<Item, Integer> fp = ImmutableMap.builder();
        fp.putAll(FOOD_POINTS);
        fp.put(ModItems.CABBAGE.get(), 1);
        fp.put(ModItems.TOMATO.get(), 1);
        fp.put(ModItems.ONION.get(), 1);
        fp.put(ModItems.RICE.get(), 2);

        FOOD_POINTS = fp.build();
    }
}
