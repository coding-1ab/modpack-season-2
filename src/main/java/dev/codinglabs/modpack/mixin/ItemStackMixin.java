package dev.codinglabs.modpack.mixin;

import com.mojang.serialization.DataResult;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.function.Consumer;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Redirect(
            method = "parse(Lnet/minecraft/core/HolderLookup$Provider;Lnet/minecraft/nbt/Tag;)Ljava/util/Optional;",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/serialization/DataResult;resultOrPartial(Ljava/util/function/Consumer;)Ljava/util/Optional;"
            )
    )
    private static Optional<ItemStack> logStackTrace(DataResult<ItemStack> result, Consumer<String> onError) {
        if (result.isError()) {
            Thread.dumpStack();
        }
        return result.resultOrPartial(onError);
    }
}
