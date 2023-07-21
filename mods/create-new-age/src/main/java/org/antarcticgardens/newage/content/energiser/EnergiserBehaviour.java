package org.antarcticgardens.newage.content.energiser;

import com.simibubi.create.content.kinetics.belt.BeltHelper;
import com.simibubi.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.simibubi.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.simibubi.create.content.kinetics.belt.transport.TransportedItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

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
    public void read(CompoundTag nbt, boolean clientPacket) {
        charged = nbt.getLong("charged");
        super.read(nbt, clientPacket);
    }

    @Override
    public void write(CompoundTag nbt, boolean clientPacket) {
        nbt.putLong("charged", charged);
        super.write(nbt, clientPacket);
    }

    private ProcessingResult itemHeld(TransportedItemStack transportedItemStack, TransportedItemStackHandlerBehaviour handler) {
        if (be.getSpeed() == 0 || currentRecipe == null || be.getLevel() == null) {
            return ProcessingResult.PASS;
        }
        int count = transportedItemStack.stack.getCount();
        long energyNeeded = (long) count * currentRecipe.energyNeeded;

        charged += be.energy.internalExtract(
                Math.min(maxAbsorptionSpeed * (long) Math.abs(be.getSpeed() * 0.1),
                        energyNeeded - charged), false);

        if (charged >= energyNeeded) {
            List<TransportedItemStack> out = currentRecipe.rollResults().stream()
                    .map(stack -> {
                        TransportedItemStack copy = transportedItemStack.copy();
                        boolean centered = BeltHelper.isItemUpright(stack);
                        stack.setCount(stack.getCount() * count);
                        copy.stack = stack;
                        copy.angle = centered ? 180 : be.getLevel().getRandom().nextInt(360);
                        return copy;
                    })
                    .collect(Collectors.toList());

            charged = 0;

            if (out.isEmpty()) {
                handler.handleProcessingOnItem(transportedItemStack,
                        TransportedItemStackHandlerBehaviour.TransportedResult.removeItem());
            } else {
                handler.handleProcessingOnItem(transportedItemStack,
                        TransportedItemStackHandlerBehaviour.TransportedResult.convertTo(out));
            }
            blockEntity.sendData();
            return ProcessingResult.HOLD;
        }


        return ProcessingResult.HOLD;
    }

    private ProcessingResult itemEnter(TransportedItemStack transportedItemStack, TransportedItemStackHandlerBehaviour transportedItemStackHandlerBehaviour) {
        if (be.getSpeed() == 0) {
            return ProcessingResult.PASS;
        }

        currentRecipe = getRecipe(transportedItemStack.stack);
        charged = 0;
        if (currentRecipe == null || currentRecipe.getIngredients().size() > 1)
            return ProcessingResult.PASS;

        return ProcessingResult.HOLD;
    }

}
