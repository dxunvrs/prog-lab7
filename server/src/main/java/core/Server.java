package core;

import commands.*;
import io.github.cdimascio.dotenv.Dotenv;
import network.ConnectionManager;
import network.DBManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Dotenv dotenv = Dotenv.load();

    private final CollectionManager collectionManager = new CollectionManager();
    private final DBManager dbManager = new DBManager();
    private final CommandHandler commandHandler = new CommandHandler(collectionManager);

    public void launch() {
        try (ConnectionManager connectionManager = new ConnectionManager(1234, commandHandler)) {
            dbManager.connect("localhost", 5432, "studs", dotenv.get("DB_USER"), dotenv.get("DB_PASS"));
            collectionManager.setDbManager(dbManager);
            collectionManager.initCollection();
            registerAllCommands();
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

    private void registerAllCommands() {
        commandHandler.addCommand(new AddCommand());
        commandHandler.addCommand(new AverageOfPriceCommand());
        commandHandler.addCommand(new ClearCommand());
        commandHandler.addCommand(new FilterStartsWithNameCommand());
        commandHandler.addCommand(new InfoCommand());
        commandHandler.addCommand(new RemoveCommand());
        commandHandler.addCommand(new ShowCommand());
        commandHandler.addCommand(new ShuffleCommand());
        commandHandler.addCommand(new SortCommand());
        commandHandler.addCommand(new SumOfPriceCommand());
        commandHandler.addCommand(new UpdateCommand());
    }
}
