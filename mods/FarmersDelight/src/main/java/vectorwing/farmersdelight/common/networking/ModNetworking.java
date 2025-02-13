package vectorwing.farmersdelight.common.networking;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.item.SkilletItem;
import vectorwing.farmersdelight.common.registry.ModDataComponents;

@EventBusSubscriber(modid = FarmersDelight.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModNetworking {

    @SubscribeEvent
    public static void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(FlipSkilletMessage.TYPE, FlipSkilletMessage.STREAM_CODEC, (msg, ctx) -> msg.handle(ctx.player().getServer(), ((ServerPlayer)ctx.player())));
    }


    public static class FlipSkilletMessage implements CustomPacketPayload {
        public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(FarmersDelight.MODID, "flip_skillet");
        public static final FlipSkilletMessage INSTANCE = new FlipSkilletMessage();
        public static final Type<FlipSkilletMessage> TYPE = new Type<>(ID);
        public static final StreamCodec<RegistryFriendlyByteBuf, FlipSkilletMessage> STREAM_CODEC = StreamCodec.unit(INSTANCE);

        public FlipSkilletMessage() {
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public void handle(MinecraftServer server, ServerPlayer player) {
            ItemStack stack = player.getUseItem();
            if (stack.getItem() instanceof SkilletItem) {
                stack.set(ModDataComponents.SKILLET_FLIP_TIMESTAMP.get(), player.level().getGameTime());
            }
        }
    }
}
