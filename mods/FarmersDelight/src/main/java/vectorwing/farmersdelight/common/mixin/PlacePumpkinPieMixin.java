package vectorwing.farmersdelight.common.mixin;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import vectorwing.farmersdelight.common.registry.ModItems;

@Mixin(Item.class)
public class PlacePumpkinPieMixin
{
	@Inject(
			method = "useOn",
			at = @At("TAIL"),
			cancellable = true)
	private void usePumpkinPie(UseOnContext ctx, CallbackInfoReturnable<InteractionResult> cir)
	{
		if (ctx.getItemInHand().is(Items.PUMPKIN_PIE))
		{
			cir.setReturnValue(ModItems.DEBUG_PUMPKIN_PIE.get().useOn(ctx));
		}
	}
}
