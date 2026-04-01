package network;

import java.net.SocketAddress;

public record RawUDPRequest(SocketAddress address, byte[] data) {}
