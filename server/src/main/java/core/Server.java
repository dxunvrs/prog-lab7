package core;

import commands.*;
import io.github.cdimascio.dotenv.Dotenv;
import multithread.DBConnectionPool;
import multithread.threads.ProcessThread;
import multithread.threads.ReaderThread;
import multithread.threads.SenderThread;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.AuthService;
import utility.Task;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.*;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().systemProperties().load();

    private final int port;

    private volatile boolean isWorking = true;

    private final BlockingQueue<RawUDPRequest> requestQueue = new LinkedBlockingQueue<>(10);
    private final BlockingQueue<Task> processQueue = new LinkedBlockingQueue<>(10);
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>(10);

    private final ExecutorService readPool = Executors.newFixedThreadPool(10);
    private final ForkJoinPool processPool = new ForkJoinPool();
    private final ForkJoinPool sendPool = new ForkJoinPool();

    public Server(int port) {
        this.port = port;
    }

    public void launch() {
        try (ConnectionManager connectionManager = new ConnectionManager(port);
             DBConnectionPool dbConnectionPool = new DBConnectionPool(
                     dotenv.get("DB_HOST"), Integer.parseInt(dotenv.get("DB_PORT")), dotenv.get("DB_NAME"),
                     dotenv.get("DB_USER"), dotenv.get("DB_PASS")
             )) {

            DBManager dbManager = new DBManager(dbConnectionPool);
            CollectionManager collectionManager = new CollectionManager(dbManager);
            AuthService authService = new AuthService(dbManager);
            CommandManager commandManager = new CommandManager(collectionManager);
            RequestHandler requestHandler = new RequestHandler(commandManager, authService);

            // инициализируем потоки-воркеры
            for (int i = 0; i < 10; i++) {
                readPool.execute(new ReaderThread(requestQueue, processQueue));
            }
            for (int i = 0; i < 10; i++) {
                processPool.execute(new ProcessThread(processQueue, responseQueue, requestHandler));
            }
            for (int i = 0; i < 10; i++) {
                sendPool.execute(new SenderThread(responseQueue, connectionManager));
            }

            collectionManager.initCollection();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(connectionManager::selectorWakeUp)));

            runMainLoop(connectionManager);
        } catch (SQLException e) {
            logger.error("Ошибка БД", e);
        } catch (IOException e) {
            logger.error("Ошибка", e);
        }
    }

    private void runMainLoop(ConnectionManager connectionManager) {
        System.out.println("Сервер запущен на порту " + port);

        while (isWorking) {
            try {
                RawUDPRequest raw = connectionManager.receive();
                if (raw == null) continue;

                requestQueue.put(raw);

            } catch (InterruptedException e) {
                logger.error("Не удалось создать задачу чтения", e);
                isWorking = false;
                Thread.currentThread().interrupt();
            }
            catch (IOException e) {
                logger.error("Ошибка при работе с данными", e);
            }
        }
    }

    private void stop(Runnable selectorWakeUpCallback) {
        logger.debug("Закрытие сервера");
        isWorking = false;

        selectorWakeUpCallback.run();

        readPool.shutdownNow();
        processPool.shutdown();
        sendPool.shutdown();

        try {
            if (!readPool.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Пул чтения не успел закрыться, принудительная остановка");
                readPool.shutdownNow();
            }
            if (!processPool.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Пул процессов не успел закрыться, принудительная остановка");
                processPool.shutdownNow();
            }
            if (!sendPool.awaitTermination(5, TimeUnit.SECONDS)) {
                logger.warn("Пул отправки не успел закрыться, принудительная остановка");
                sendPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            readPool.shutdownNow();
            processPool.shutdownNow();
            sendPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.debug("Все пулы потоков закрыты");
    }
}
