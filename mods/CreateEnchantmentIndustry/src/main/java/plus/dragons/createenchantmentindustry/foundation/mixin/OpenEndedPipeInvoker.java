package plus.dragons.createenchantmentindustry.foundation.mixin;

import com.simibubi.create.content.fluids.OpenEndedPipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(OpenEndedPipe.class)
public interface OpenEndedPipeInvoker { // LEGACY TODO remove

    //@Invoker(remap = false)
    //void invokeApplyEffects(FluidStack fluid);
}
