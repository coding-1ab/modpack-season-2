package fuzs.iteminteractions.mixin.client;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractContainerScreen.class)
public interface AbstractContainerScreenAccess {
    @Invoker("slotClicked")
    void invokeSlotClicked(Slot slot, int slotId, int mouseButton, ClickType type);

    @Accessor("hoveredSlot")
    Slot getHoveredSlot();
}
