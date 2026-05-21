package dev.propulsionteam.propulsionsimulated.network;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import dev.propulsionteam.propulsionsimulated.CreatePropulsion;
import dev.propulsionteam.propulsionsimulated.content.thruster.ItemThrusterProperties;
import dev.propulsionteam.propulsionsimulated.content.thruster.SolidThrusterFuelManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class SyncSolidThrusterFuelsPacket implements CustomPacketPayload {
    public static final Type<SyncSolidThrusterFuelsPacket> TYPE =
        new Type<>(ResourceLocation.fromNamespaceAndPath(CreatePropulsion.ID, "sync_solid_thruster_fuels"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SyncSolidThrusterFuelsPacket> STREAM_CODEC = StreamCodec.of(
        (buf, payload) -> payload.encode(buf),
        SyncSolidThrusterFuelsPacket::decode
    );

    private final Map<ResourceLocation, ItemThrusterProperties> fuelMap;
    private final Set<ResourceLocation> removedFuelIds;

    public static SyncSolidThrusterFuelsPacket create(Map<Item, ItemThrusterProperties> mapToSync, Set<ResourceLocation> removedFuelIds) {
        Map<ResourceLocation, ItemThrusterProperties> networkSafeMap = new HashMap<>();
        mapToSync.forEach((item, props) -> {
            ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
            if (key != null) {
                networkSafeMap.put(key, props);
            }
        });
        return new SyncSolidThrusterFuelsPacket(networkSafeMap, removedFuelIds);
    }

    private SyncSolidThrusterFuelsPacket(Map<ResourceLocation, ItemThrusterProperties> fuelMap, Set<ResourceLocation> removedFuelIds) {
        this.fuelMap = fuelMap;
        this.removedFuelIds = removedFuelIds;
    }

    public static SyncSolidThrusterFuelsPacket decode(FriendlyByteBuf buf) {
        Map<ResourceLocation, ItemThrusterProperties> map =
            buf.readMap(FriendlyByteBuf::readResourceLocation, ItemThrusterProperties::decode);
        Set<ResourceLocation> removed = buf.readCollection(HashSet::new, FriendlyByteBuf::readResourceLocation);
        return new SyncSolidThrusterFuelsPacket(map, removed);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeMap(fuelMap, FriendlyByteBuf::writeResourceLocation, (b, props) -> props.encode(b));
        buf.writeCollection(removedFuelIds, FriendlyByteBuf::writeResourceLocation);
    }

    public void handle() {
        SolidThrusterFuelManager.updateClient(fuelMap, removedFuelIds);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
