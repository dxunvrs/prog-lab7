package core;

import commands.*;
import io.FileManager;
import network.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Scanner;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    private final CollectionManager collectionManager = new CollectionManager();
    private final FileManager fileManager = new FileManager();
    private final CommandHandler commandHandler = new CommandHandler(collectionManager);

    public void launch(String[] args) {
        try (ConnectionManager connectionManager = new ConnectionManager(1234, commandHandler)) {
            checkArgs(args);
            registerAllCommands();
            registerShutdownHook();
            startConsoleThread(connectionManager);
            connectionManager.start();
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
                    if (line.equals("save")) {
                        synchronized (collectionManager) {
                            fileManager.save(collectionManager);
                        }
                    } else if (line.equals("exit")) {
                        connectionManager.stop();
                    } else {
                        System.out.println("Сервер поддерживает только команды: save, exit");
                    }
                }
            }
        });
        consoleThread.setDaemon(true); // Чтобы поток не мешал закрытию программы
        consoleThread.start();
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nСохранение коллекции");
            synchronized (collectionManager) {
                fileManager.save(collectionManager);
            }
            logger.info("Завершение работы сервера");
            System.out.println("Завершение работы...");
        }));
    }

    private void checkArgs(String[] args) {
        if (args.length == 0) {
            System.out.println("Имя файла не указано, создана новая коллекция");
        } else {
            if (args.length > 1) System.out.println("Указано больше одного аргумента, в качестве имени файла взят первый");
            fileManager.setFileName(args[0]);
            fileManager.load(collectionManager);
        }
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
