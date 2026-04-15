package dev.simulated_team.simulated.network.packets.physics_staff;

import dev.ryanhcode.sable.util.SableBufferUtils;
import dev.simulated_team.simulated.Simulated;
import dev.simulated_team.simulated.SimulatedClient;
import foundry.veil.api.network.handler.PacketContext;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PhysicsStaffDragSessionsPacket implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, PhysicsStaffDragSessionsPacket> CODEC = StreamCodec.of((buf, value) -> value.write(buf), PhysicsStaffDragSessionsPacket::read);
    public static Type<PhysicsStaffDragSessionsPacket> TYPE = new Type<>(Simulated.path("physics_staff_drag_sessions"));

    protected final List<Pair<UUID, Vector3d>> sessions;
    private final ResourceKey<Level> dimension;

    public PhysicsStaffDragSessionsPacket(final ResourceKey<Level> dimension, final Collection<Pair<UUID, Vector3d>> sessions) {
        this.dimension = dimension;
        this.sessions = new ObjectArrayList<>(sessions);
    }

    private static PhysicsStaffDragSessionsPacket read(final RegistryFriendlyByteBuf buf) {
        final ResourceLocation level = buf.readResourceLocation();

        final int size = buf.readInt();

        final List<Pair<UUID, Vector3d>> sessions = new ObjectArrayList<>(size);
        for (int i = 0; i < size; i++) {
            sessions.add(Pair.of(buf.readUUID(), SableBufferUtils.read(buf, new Vector3d())));
        }

        return new PhysicsStaffDragSessionsPacket(ResourceKey.create(Registries.DIMENSION, level), sessions);
    }

    private void write(final RegistryFriendlyByteBuf buf) {
        buf.writeResourceKey(this.dimension);

        buf.writeInt(this.sessions.size());
        for (final Pair<UUID, Vector3d> point : this.sessions) {
            buf.writeUUID(point.first());
            SableBufferUtils.write(buf, point.value());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(final PacketContext context) {
        SimulatedClient.PHYSICS_STAFF_CLIENT_HANDLER.setServerDragSessions(this.dimension, this.sessions);
    }
}
