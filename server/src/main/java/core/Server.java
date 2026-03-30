package core;

import commands.*;
import io.github.cdimascio.dotenv.Dotenv;
import network.ConnectionManager;
import network.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.AuthService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Dotenv dotenv = Dotenv.load();

    private final DBManager dbManager = new DBManager();
    private final CollectionManager collectionManager = new CollectionManager(dbManager);
    private final AuthService authService = new AuthService(dbManager);
    private final RequestHandler requestHandler = new RequestHandler(collectionManager, authService);

    public void launch() {
        try (ConnectionManager connectionManager = new ConnectionManager(1234, requestHandler)) {
            dbManager.connect("localhost", 5432, "study", dotenv.get("DB_USER"), dotenv.get("DB_PASS"));
            collectionManager.initCollection();
            startConsoleThread(connectionManager);
            connectionManager.start();
        } catch (SQLException e) {
            logger.error("Ошибка БД", e);
            System.out.println("Ошибка БД: " + e.getMessage());
        } catch (IOException e) {
            logger.error("Ошибка", e);
            System.out.println("Ошибка: " + e.getMessage());
        }
    }

    private void startConsoleThread(ConnectionManager connectionManager) {
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (!Thread.currentThread().isInterrupted()) {
                if (scanner.hasNextLine()) {
                    String line = scanner.nextLine().trim();
                    if (line.equals("exit")) {
                        connectionManager.stop();
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
