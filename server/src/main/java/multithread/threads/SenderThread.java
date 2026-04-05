package multithread.threads;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import network.ConnectionManager;
import network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.BlockingQueue;

public class SenderThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SenderThread.class);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final BlockingQueue<Response> responseQueue;
    private final ConnectionManager connectionManager;

    public SenderThread(BlockingQueue<Response> responseQueue, ConnectionManager connectionManager) {
        this.responseQueue = responseQueue;
        this.connectionManager = connectionManager;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Response response = responseQueue.take();
                logger.debug("Поток {} берет задачу отправки из очереди", Thread.currentThread().getName());
                byte[] responseBytes = mapper.writeValueAsBytes(response);
                SocketAddress address = new InetSocketAddress(response.getHost(), response.getPort());
                connectionManager.send(address, responseBytes);
                logger.info("Отправлен ответ на {}, вес: {} байт, сообщение: {}", address, responseBytes.length, new String(responseBytes));
            } catch (IOException e) {
                logger.error("Ошибка отправки", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.debug("Поток отправки {} закрылся", Thread.currentThread().getName());
    }
}
