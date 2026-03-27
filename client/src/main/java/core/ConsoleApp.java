package core;

import exceptions.EndOfInputException;
import io.InputManager;
import network.ConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;

public class ConsoleApp {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleApp.class);

    private final CommandManager commandManager = new CommandManager();
    private final InputManager inputManager = new InputManager(commandManager::getCommandNames);

    private boolean isWorking = true;

    public ConsoleApp() {
        try {
            ConnectionManager connectionManager = new ConnectionManager("localhost", 1234);
            commandManager.setConnectionManager(connectionManager);
            commandManager.setInputManager(inputManager);
            commandManager.syncCommands();
        } catch (SocketException e) {
            logger.error("Ошибка открытия сокета: {}", e.getMessage(), e);
            System.out.println("Ошибка сокета: " + e.getMessage());
        }
    }

    public void interactive() {
        System.out.println("Ожидание ввода команды, для списка доступных команд - help");
        while (isWorking) {
            try {
                String line = inputManager.readNextLine("> ", true);
                String formattedLine = line.trim().replaceAll("\\s+", " ");

                if (inputManager.isScriptMode()) System.out.println(formattedLine); // для режима скрипта

                if (formattedLine.isEmpty()) continue;

                if (!commandManager.executeCommand(formattedLine)) {
                    isWorking = false;
                }
            } catch (EndOfInputException e) {
                logger.error("Конец ввода", e);
                System.out.println(e.getMessage());
                isWorking = false;
            }
        }
    }
}