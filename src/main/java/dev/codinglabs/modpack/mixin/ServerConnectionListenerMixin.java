package dev.codinglabs.modpack.mixin;

import dev.codinglabs.modpack.ModPackTweaks;
import dev.codinglabs.modpack.config.Config;
import dev.codinglabs.modpack.mixin_interfaces.ServerConnectionListenerExtension;
import dev.codinglabs.modpack.rapier_entity.network.UdpPacket;
import dev.codinglabs.modpack.rapier_entity.network.UdpPacketKt;
import dev.codinglabs.modpack.rapier_entity.network.UdpServer;
import dev.ryanhcode.sable.network.udp.handler.SableUDPChannelHandlerServer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollDatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerConnectionListener;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.net.InetAddress;
import java.util.List;

/**
 * Copied from [dev.ryanhcode.sable.mixin.udp.ServerConnectionListenerMixin]
 */
@Mixin(ServerConnectionListener.class)
public class ServerConnectionListenerMixin implements ServerConnectionListenerExtension {
    @Shadow
    @Final
    private List<ChannelFuture> channels;

    @Shadow
    @Final
    MinecraftServer server;

    @Unique
    private UdpServer codinglab$udpServer;

    @Inject(method = "startTcpServerListener", at = @At("HEAD"))
    private void startTcpServerListener(final InetAddress inetAddress, final int port, final CallbackInfo ci) {
        if (!Config.INSTANCE.getENABLE_UDP().get()) {
            return;
        }

        //noinspection SynchronizeOnNonFinalField
        synchronized (this.channels) {
            final Class<? extends Channel> channelClass;
            final EventLoopGroup eventLoopGroup;

            if (Epoll.isAvailable() && this.server.isEpollEnabled()) {
                channelClass = EpollDatagramChannel.class;
                eventLoopGroup = ServerConnectionListener.SERVER_EPOLL_EVENT_GROUP.get();
            } else {
                channelClass = NioDatagramChannel.class;
                eventLoopGroup = ServerConnectionListener.SERVER_EVENT_GROUP.get();
            }

            ModPackTweaks.Companion.getLOGGER().info("Starting UDP channel");
            this.channels.add(new Bootstrap()
                    .channel(channelClass)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) throws Exception {
                            UdpPacketKt.setupSerialization(channel.pipeline(), false);
                            codinglab$setupUdpServer(channel);
                        }
                    })
                    .group(eventLoopGroup)
                    .localAddress(inetAddress, port)
                    .bind()
                    .syncUninterruptibly()
            );
        }
    }

    @Unique
    private void codinglab$setupChannel(final Channel channel) {
        final ChannelPipeline pipeline = channel.pipeline();

        // pipeline.addLast(????);
    }

    @Override
    public void codinglab$setupUdpServer(@NotNull Channel channel) {
        this.codinglab$udpServer = new UdpServer(this.server, channel);
    }


    @Override
    public @NotNull UdpServer getCodinglab$server() {
        return codinglab$udpServer;
    }
}
