package core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import commands.*;
import io.github.cdimascio.dotenv.Dotenv;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.AuthService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Dotenv dotenv = Dotenv.load();

    private RequestHandler requestHandler;

    private final int port;

    private boolean isWorking = true;

    private final ObjectMapper mapper;

    public Server(int port) {
        this.port = port;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public void launch() {
        try (ConnectionManager connectionManager = new ConnectionManager(port)) {
            DBManager dbManager = new DBManager(
                    dotenv.get("DB_HOST"), Integer.parseInt(dotenv.get("DB_PORT")), dotenv.get("DB_NAME"),
                    dotenv.get("DB_USER"), dotenv.get("DB_PASS")
            );
            CollectionManager collectionManager = new CollectionManager(dbManager);
            AuthService authService = new AuthService(dbManager);
            CommandManager commandManager = new CommandManager(collectionManager);
            requestHandler = new RequestHandler(commandManager, authService);

            dbManager.connect();
            collectionManager.initCollection();

            startConsoleThread(connectionManager::selectorWakeUp);

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

                Request request = mapper.readValue(raw.data(), Request.class);
                logger.info("Получен новый запрос от {}, вес: {} байт, сообщение: {}", raw.address(), raw.data().length, new String(raw.data()));
                System.out.println("Новый запрос");
                System.out.println("От: " + raw.address());
                System.out.println("JSON: " + new String(raw.data()));

                Response response = requestHandler.handle(request);

                byte[] responseBytes = mapper.writeValueAsBytes(response);
                connectionManager.send(raw.address(), responseBytes);
                logger.info("Отправлен ответ на {}, вес: {} байт, сообщение: {}", raw.address(), responseBytes.length, new String(responseBytes));

            } catch (IOException e) {
                logger.error("Ошибка при работе с данными", e);
            }
        }
    }

    private void startConsoleThread(Runnable selectorWakeUpCallback) {
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.equals("exit")) {
                        isWorking = false;
                        selectorWakeUpCallback.run();
                    } else {
                        System.out.println("Сервер поддерживает только exit");
                    }
                }
            }
        });
        consoleThread.setDaemon(true); // Чтобы поток не мешал закрытию программы
        consoleThread.start();
    }
}
