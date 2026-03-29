package core;

import exceptions.EndOfInputException;
import exceptions.InvalidAuthorizeException;
import io.InputManager;
import network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.util.Objects;
import java.util.Set;

public class ConsoleApp {
    private static final Logger logger = LoggerFactory.getLogger(ConsoleApp.class);

    private ConnectionManager connectionManager;
    private final CommandManager commandManager = new CommandManager();
    private final InputManager inputManager = new InputManager(commandManager::getCommandNames);

    private boolean isWorking = true;
    private boolean isAuthorized = false;

    public ConsoleApp() {
        try {
            connectionManager = new ConnectionManager("localhost", 1234);
            commandManager.setConnectionManager(connectionManager);
            commandManager.setInputManager(inputManager);
            commandManager.syncCommands();
        } catch (SocketException e) {
            logger.error("Ошибка открытия сокета: {}", e.getMessage(), e);
            System.out.println("Ошибка сокета: " + e.getMessage());
        }
    }

    public void start() {
        System.out.println("Чтобы авторизоваться/зарегистрироваться введите login/register или exit для выхода");
        while (isWorking) {
            try {
                if (isAuthorized) {
                    interactive();
                } else {
                    authorize();
                }
            } catch (InvalidAuthorizeException e) {
                logger.error("Ошибка авторизации", e);
                System.out.println(e.getMessage());
                isAuthorized = false;
            }
        }
    }

    private void interactive() {
        try {
            String line = inputManager.readNextLine("> ", true);
            String formattedLine = line.trim().replaceAll("\\s+", " ");

            if (inputManager.isScriptMode()) System.out.println(formattedLine); // для режима скрипта

            if (formattedLine.isEmpty()) return;

            if (!commandManager.executeCommand(formattedLine)) {
                isWorking = false;
            }
        } catch (EndOfInputException e) {
            logger.error("Конец ввода", e);
            System.out.println(e.getMessage());
            isWorking = false;
        }
    }

    private void authorize() {
        try {
            String line = inputManager.readNextLine("> ", Set.of("login", "register", "exit"), false);
            String formattedLine = line.trim().replaceAll("\\s+", " ");

            switch (formattedLine) {
                case "login" -> isAuthorized = processLogin();
                case "register" -> isAuthorized = processRegister();
                case "exit" -> {
                    System.out.println("Выход из программы");
                    isWorking = false;
                }
                default -> System.out.println("Не поддерживается");
            }
        } catch (EndOfInputException e) {
            logger.error("Конец ввода");
            System.out.println(e.getMessage());
            isWorking = false;
        }
    }

    private boolean processLogin() {
        String username = inputManager.readNextLine("Введите имя пользователя: ", false).trim();
        String password = inputManager.readNextLine("Введите пароль: ", false).trim();

        Request request = new Request(RequestType.LOGIN);
        request.setUsername(username);
        request.setPassword(password);

        Response response = connectionManager.sendAndReceive(request);
        System.out.println(response.getMessage());

        if (response.getType() == ResponseType.AUTH_SUCCESS) {
            commandManager.setToken(response.getToken());
            return true;
        }
        return false;
    }

    private boolean processRegister() {
        String username = inputManager.readNextLine("Введите имя пользователя: ", false).trim();
        String password = inputManager.readNextLine("Введите пароль: ", false).trim();
        String repeatPassword = inputManager.readNextLine("Повторите введенный пароль: ", false).trim();

        if (!Objects.equals(password, repeatPassword)) {
            System.out.println("Пароли не совпадают");
            return false;
        }

        Request request = new Request(RequestType.REGISTER);
        request.setUsername(username);
        request.setPassword(password);

        Response response = connectionManager.sendAndReceive(request);
        System.out.println(response.getMessage());

        if (response.getType() == ResponseType.AUTH_SUCCESS) {
            commandManager.setToken(response.getToken());
            return true;
        }
        return false;
    }
}