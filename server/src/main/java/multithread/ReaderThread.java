package multithread;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import network.RawUDPRequest;
import network.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Task;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class ReaderThread implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ReaderThread.class);
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private final BlockingQueue<RawUDPRequest> requestQueue;
    private final BlockingQueue<Task> processQueue;

    public ReaderThread(BlockingQueue<RawUDPRequest> requestQueue, BlockingQueue<Task> processQueue) {
        this.requestQueue = requestQueue;
        this.processQueue = processQueue;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                RawUDPRequest raw = requestQueue.take();
                logger.debug("Поток {} берет задачу чтения из очереди", Thread.currentThread().getName());
                Request request = mapper.readValue(raw.data(), Request.class);
                processQueue.put(new Task(request, raw.address()));
                logger.info("Получен новый запрос от {}, вес: {} байт, сообщение: {}", raw.address(), raw.data().length, new String(raw.data()));
            } catch (IOException e) {
                logger.error("Ошибка парсинга запроса", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        logger.debug("Поток чтения {} закрылся", Thread.currentThread().getName());
    }
}
