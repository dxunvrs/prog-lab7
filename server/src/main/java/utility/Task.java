package utility;

import network.Request;

import java.net.SocketAddress;

public record Task(Request request, SocketAddress address) {}
