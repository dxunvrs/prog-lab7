package core;

import db.DBManager;
import io.github.cdimascio.dotenv.Dotenv;
import db.DBConnectionPool;
import multithread.ProcessThread;
import multithread.ReaderThread;
import multithread.SenderThread;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import auth.AuthService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.*;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().systemProperties().load();

    // private final int port;
    // private ConnectionManager connectionManager;
    private TCPConnectionManager connectionManager;

    private volatile boolean isWorking = true;
    private volatile boolean isShuttingDown = false;

    // private final BlockingQueue<RawUDPRequest> requestQueue = new LinkedBlockingQueue<>(10);
    // private final BlockingQueue<Task> processQueue = new LinkedBlockingQueue<>(10);
    // private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>(10);

    private final BlockingQueue<byte[]> requestQueue = new LinkedBlockingQueue<>(10);
    private final BlockingQueue<Request> processQueue = new LinkedBlockingQueue<>(10);
    private final BlockingQueue<Response> responseQueue = new LinkedBlockingQueue<>(10);

    private final ExecutorService readPool = Executors.newFixedThreadPool(10);
    private final ForkJoinPool processPool = new ForkJoinPool();
    private final ForkJoinPool sendPool = new ForkJoinPool();

//    public Server(int port) {
//        this.port = port;
//    }

    public void launch() {
        try (DBConnectionPool dbConnectionPool = new DBConnectionPool(
                     dotenv.get("DB_HOST"), Integer.parseInt(dotenv.get("DB_PORT")), dotenv.get("DB_NAME"),
                     dotenv.get("DB_USER"), dotenv.get("DB_PASS")
             )) {

            // connectionManager = new ConnectionManager(port);
            connectionManager = new TCPConnectionManager(dotenv.get("GATEWAY_HOST"), Integer.parseInt(dotenv.get("GATEWAY_SERVER_PORT")),
                    this::stop);

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

            startConsoleThread();
            Runtime.getRuntime().addShutdownHook(new Thread(this::serverShutdown));

            runMainLoop();
        } catch (SQLException e) {
            logger.error("Ошибка БД", e);
        } catch (IOException e) {
            logger.error("Ошибка", e);
        }
    }

    private void runMainLoop() {
        // System.out.println("Сервер запущен на порту " + port);
        logger.info("Сервер запущен");

        while (isWorking) {
            try {
                // RawUDPRequest raw = connectionManager.receive();
                byte[] data = connectionManager.receive();
                if (data == null) continue;

                requestQueue.put(data);

            } catch (InterruptedException e) {
                logger.error("Не удалось создать задачу чтения", e);
                isWorking = false;
                Thread.currentThread().interrupt();
            } catch (IOException e) {
                logger.error("Ошибка", e);
            }
        }
        serverShutdown();
    }

    private void startConsoleThread() {
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.equals("exit")) {
                        stop();
                    } else {
                        System.out.println("Сервер поддерживает только exit");
                    }
                }
            }
        });
        consoleThread.setDaemon(true); // Чтобы поток не мешал закрытию программы
        consoleThread.start();
    }

    private void stop() {
        logger.debug("Закрытие сервера");
        isWorking = false;

        connectionManager.selectorWakeUp();
    }

    private void serverShutdown() {
        if (isShuttingDown) {
            return;
        }
        isShuttingDown = true;

        connectionManager.close();

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
