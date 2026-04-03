package multithread;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import core.RequestHandler;
import network.ConnectionManager;
import network.RawUDPRequest;
import network.Request;
import network.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

public class RequestExecutor {
    private static final Logger logger = LoggerFactory.getLogger(RequestExecutor.class);

    private final ObjectMapper mapper;

    private final ExecutorService readPool;
    private final ForkJoinPool processPool;
    private final ForkJoinPool sendPool;

    private final ConnectionManager connectionManager;
    private final RequestHandler requestHandler;

    public RequestExecutor(ConnectionManager connectionManager, RequestHandler requestHandler) {
        this.connectionManager = connectionManager;
        this.requestHandler = requestHandler;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        this.readPool = Executors.newFixedThreadPool(10, r -> {
            Thread t = new Thread(r);
            t.setName("Reading-Thread-" + t.getId());
            return t;
        });

        AtomicInteger workerCount = new AtomicInteger(1);
        this.processPool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                pool -> {
                    ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    worker.setName("Processing-Pool-worker-" + workerCount.getAndIncrement());
                    return worker;
                },
                null,
                false
        );

        workerCount.set(1);
        this.sendPool = new ForkJoinPool(
                Runtime.getRuntime().availableProcessors(),
                pool -> {
                    ForkJoinWorkerThread worker = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                    worker.setName("Sending-Pool-worker-" + workerCount.getAndIncrement());
                    return worker;
                },
                null,
                false
        );
    }

    public void execute(RawUDPRequest raw) {
        readPool.submit(() -> {
            try {
                Request request = mapper.readValue(raw.data(), Request.class);
                logger.info("Получен новый запрос от {}, вес: {} байт, сообщение: {}", raw.address(), raw.data().length, new String(raw.data()));

                processPool.submit(() -> {
                    Response response = requestHandler.handle(request);

                    sendPool.submit(() -> {
                        try {
                            byte[] responseBytes = mapper.writeValueAsBytes(response);
                            connectionManager.send(raw.address(), responseBytes);
                            logger.info("Отправлен ответ на {}, вес: {} байт, сообщение: {}", raw.address(), responseBytes.length, new String(responseBytes));
                        } catch (IOException e) {
                            logger.error("Ошибка отправки", e);
                        }
                    });
                });
            } catch (IOException e) {
                logger.error("Ошибка парсинга запроса", e);
            }
        });
    }

    public void shutdown() {
        readPool.shutdown();
        processPool.shutdown();
        sendPool.shutdown();
    }
}
