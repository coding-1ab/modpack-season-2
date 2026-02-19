package vectorwing.farmersdelight.common.networking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.item.SkilletItem;

import java.util.function.Supplier;

public class ModNetworking {
    private static final String PROTOCOL_VERISON = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FarmersDelight.MODID, "main"),
            () -> PROTOCOL_VERISON,
            PROTOCOL_VERISON::equals,
            PROTOCOL_VERISON::equals
    );

    public static void register() {
        int i = 0;
        INSTANCE.registerMessage(i++, FlipSkilletMessage.class, FlipSkilletMessage::encode, FlipSkilletMessage::new, FlipSkilletMessage::handle);
    }

    public static class FlipSkilletMessage {

        public FlipSkilletMessage(FriendlyByteBuf buf) {
        }

        public FlipSkilletMessage(){
        }

        public void handle(Supplier<NetworkEvent.Context> ctx) {
            ctx.get().enqueueWork(() -> {
                ServerPlayer player = ctx.get().getSender();
                ItemStack stack = player.getUseItem();
                if (stack.getItem() instanceof SkilletItem) {
                    CompoundTag tag = stack.getOrCreateTag();
                    if (!tag.contains("FlipTimeStamp")) {
                        tag.putLong("FlipTimeStamp", player.level().getGameTime());
                    }
                }
            });
            ctx.get().setPacketHandled(true);
        }

        public void encode(FriendlyByteBuf buf) {
        }

    }
}
