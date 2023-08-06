package org.antarcticgardens.newage.mixin;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@SuppressWarnings("UnstableApiUsage")
@Mixin(targets = "net.fabricmc.fabric.impl.transfer.transaction.TransactionManagerImpl$TransactionImpl")
public class TransactionImplMixin {
    @Unique private TransactionContext.Result result;

    @Inject(method = "close(Lnet/fabricmc/fabric/api/transfer/v1/transaction/TransactionContext$Result;)V",
            at = @At("TAIL"),
            locals = LocalCapture.CAPTURE_FAILHARD,
            remap = false)
    private void close(TransactionContext.Result result, CallbackInfo ci, RuntimeException closeException) {
        this.result = result;
    }

    @SuppressWarnings("MissingUnique")
    public TransactionContext.Result getResult() {
        return result;
    }
}
