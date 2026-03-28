package network;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.RequestHandler;
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
    private final ObjectMapper mapper;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(65535);

    private boolean isWorking = true;

    private final RequestHandler commandHandler;

    public ConnectionManager(int port, RequestHandler commandHandler) throws IOException {
        this.commandHandler = commandHandler;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        selector = Selector.open();
        channel = DatagramChannel.open();
        channel.configureBlocking(false);
        channel.bind(new InetSocketAddress(port));
        channel.register(selector, SelectionKey.OP_READ);

        logger.debug("Селектор и канал датаграммы открыты");
        logger.info("Сервер запущен на порту {}", port);
        System.out.println("Сервер запущен на порту " + port);
    }

    public void start() throws IOException {
        while (isWorking) {
            if (selector.select() == 0) continue;

            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) continue;

                if (key.isReadable()) {
                    read(key);
                }
            }
        }
    }

    private void read(SelectionKey key) {
        DatagramChannel chan = (DatagramChannel) key.channel();
        try {
            readBuffer.clear();
            SocketAddress clientAddress = chan.receive(readBuffer);

            if (clientAddress == null) return;

            readBuffer.flip();
            byte[] data = new byte[readBuffer.remaining()];
            readBuffer.get(data);

            Request request = mapper.readValue(data, Request.class);
            logger.info("Получен новый запрос от {}, вес: {} байт, сообщение: {}", clientAddress, data.length, new String(data));
            System.out.println("Новый запрос");
            System.out.println("От: " + clientAddress);
            System.out.println("JSON: " + new String(data));

            send(clientAddress, commandHandler.handle(request));

        } catch (IOException e) {
            logger.error("Ошибка при работе с данными", e);
            System.out.println("Ошибка при работе с данными: " + e.getMessage());
        }
    }

    private void send(SocketAddress clientAddress, Response response) throws IOException {
        byte[] data = mapper.writeValueAsBytes(response);
        ByteBuffer buffer = ByteBuffer.wrap(data);

        channel.send(buffer, clientAddress);
        logger.info("Отправлен ответ на {}, вес: {} байт, сообщение: {}", clientAddress, data.length, new String(data));
    }

    public void stop() {
        isWorking = false;
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
