package org.antarcticgardens.newage.content.energiser;

import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EnergiserBehaviour extends BeltProcessingBehaviour {

    private final long maxAbsorptionSpeed;
    public EnergiserBlockEntity be;
    public EnergiserBehaviour(EnergiserBlockEntity be, long maxAbsorptionSpeed) {
        super(be);
        this.maxAbsorptionSpeed = maxAbsorptionSpeed;
        whenItemEnters(this::itemEnter);
        whileItemHeld(this::itemHeld);
        this.be = be;
    }

    public EnergisingRecipe getRecipe(ItemStack stack) {
        if (be.getLevel() == null) {
            return null;
        }
        
        List<EnergisingRecipe> recipes = be.getLevel().getRecipeManager().getAllRecipesFor(EnergisingRecipe.type.getType());

        for (EnergisingRecipe recipe : recipes) {
            if (recipe.test(stack)) {
                return recipe;
            }
        }
        return null;
    
    }

    public EnergisingRecipe currentRecipe;
    public long charged;

    @Override
    public void tick() {
        if (currentRecipe == null)
            return;

        long amount = be.energy.internalExtract(Math.min(maxAbsorptionSpeed, charged - currentRecipe.energyNeeded), false);

        super.tick();
    }

    @Override
    public void read(CompoundTag nbt, boolean clientPacket) {
        charged = nbt.getLong("charged");
        super.read(nbt, clientPacket);
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.putLong("charged", charged);
        super.write(nbt, clientPacket);
    }

    private ProcessingResult itemHeld(TransportedItemStack transportedItemStack, TransportedItemStackHandlerBehaviour transportedItemStackHandlerBehaviour) {
        if (be.getSpeed() == 0) {
            return ProcessingResult.PASS;
        }

        currentRecipe = getRecipe(transportedItemStack.stack);
        if (currentRecipe == null)
            return ProcessingResult.PASS;

        return ProcessingResult.HOLD;
    }

    private ProcessingResult itemEnter(TransportedItemStack transportedItemStack, TransportedItemStackHandlerBehaviour transportedItemStackHandlerBehaviour) {
        return ProcessingResult.HOLD;
    }

}
