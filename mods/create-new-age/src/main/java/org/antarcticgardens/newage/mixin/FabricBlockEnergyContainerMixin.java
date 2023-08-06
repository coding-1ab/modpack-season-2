package org.antarcticgardens.newage.mixin;

import earth.terrarium.botarium.common.energy.base.EnergyContainer;
import earth.terrarium.botarium.common.energy.base.EnergySnapshot;
import earth.terrarium.botarium.fabric.energy.FabricBlockEnergyContainer;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("UnstableApiUsage")
@Mixin(FabricBlockEnergyContainer.class)
public abstract class FabricBlockEnergyContainerMixin extends SnapshotParticipant<EnergySnapshot> {
    @Final @Shadow private EnergyContainer container;

    /**
     * @author Kastuś Talstoj
     * @reason we need a real value of simulate
     */
    @Overwrite(remap = false)
    public long insert(long maxAmount, TransactionContext transaction) {
        if (maxAmount <= 0) return 0;
        this.updateSnapshots(transaction);

        Class<?> clazz = transaction.getClass();
        boolean simulate = false;

        if (clazz.getName().equals("net.fabricmc.fabric.impl.transfer.transaction.TransactionManagerImpl$TransactionImpl")) {
            try {
                Method getResult = clazz.getDeclaredMethod("getResult");
                getResult.setAccessible(true);
                Transaction.Result result = (Transaction.Result) getResult.invoke(transaction);
                getResult.setAccessible(false);

                if (result == TransactionContext.Result.ABORTED)
                    simulate = true;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }

        return container.insertEnergy(Math.min(maxAmount, container.maxInsert()), simulate);
    }
}
