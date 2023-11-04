package org.antarcticgardens.newage.mixin;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@SuppressWarnings("UnstableApiUsage")
@Mixin(FluidTank.class)
public abstract class FluidTankMixin extends SingleVariantStorage<FluidVariant> {
    @Shadow(remap = false) protected FluidStack stack;

    /**
     * @author Kastuś Talstoj
     * @reason Tag does not save in original version
     */
    @Overwrite(remap = false)
    private void updateStack() {
        this.stack = new FluidStack(this.variant, this.amount, stack.getTag());
    }
}
