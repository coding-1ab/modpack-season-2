package dev.ryanhcode.sable.mixinterface.udp;

import dev.ryanhcode.sable.network.udp.SableUDPServer;
import io.netty.channel.Channel;

public interface ServerConnectionListenerExtension {
    void sable$setupUDPServer(Channel channel);

    SableUDPServer sable$getServer();
}
