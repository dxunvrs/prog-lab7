package network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

public class ConnectionManager implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    private final DatagramChannel channel;
    private final Selector selector;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(65535);


    public ConnectionManager(int port) throws IOException {
        selector = Selector.open();
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(port));
        channel.register(selector, SelectionKey.OP_READ);

        logger.debug("Селектор и канал датаграммы открыты");

    }

    public RawUDPRequest receive() throws IOException {
        if (selector.select() == 0) return null;

        Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
        while (keys.hasNext()) {
            SelectionKey key = keys.next();
            keys.remove();

            if (!key.isValid() || !key.isReadable()) continue;

            DatagramChannel chan = (DatagramChannel) key.channel();
            readBuffer.clear();
            SocketAddress clientAddress = chan.receive(readBuffer);

            if (clientAddress == null) return null;

            readBuffer.flip();
            byte[] bytes = new byte[readBuffer.remaining()];
            readBuffer.get(bytes);

            return new RawUDPRequest(clientAddress, bytes);
        }
        return null;
    }

    public void send(SocketAddress address, byte[] data) throws IOException {
        channel.send(ByteBuffer.wrap(data), address);
    }

    public void selectorWakeUp() {
        selector.wakeup();
    }

    @Override
    public void close() {
        try {
            if (selector != null && channel.isOpen()) selector.close();
            if (channel != null && channel.isOpen()) channel.close();
        } catch (IOException e) {
            System.out.println("Ошибка при закрытии ресурсов: " + e.getMessage());
        }
    }
}
