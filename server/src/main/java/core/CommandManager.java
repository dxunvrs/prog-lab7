package core;

import commands.*;
import exceptions.CommandExecutionException;
import exceptions.IdNotFoundException;
import network.Request;
import network.Response;
import network.ResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class CommandManager {
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    private final Map<String, Command> commands = new HashMap<>();

    private final CollectionManager collectionManager;

    public CommandManager(CollectionManager collectionManager) {
        this.collectionManager = collectionManager;
        registerAllCommands();
    }

    public Response executeCommand(Request request) {
        Command command = commands.get(request.getCommandName());
        if (command == null) {
            logger.warn("Команда не найдена");
            System.out.println("Команда не найдена");
            return new Response(ResponseType.OUTDATED, "Данная команда не поддерживается");
        }
        try {
            return command.execute(request);
        } catch (IdNotFoundException | CommandExecutionException e) {
            return handleError(e.getMessage(), e);
        } catch (Exception e) {
            return handleError("Неизвестная ошибка", e);
        }
    }

    private Response handleError(String message, Exception e) {
        logger.error(message, e);
        return new Response(ResponseType.ERROR, e.getMessage());
    }

    public Response syncCommands() {
        Map<String, CommandDef> commandDefMap = new HashMap<>();
        commands.forEach((name, serverCommand) ->
                commandDefMap.put(name, new CommandDef(serverCommand.getName(), serverCommand.getDescription(), serverCommand.getExpectedArgs()))
        );
        Response response = new Response(ResponseType.SYNC_DATA, "Актуальные команды");
        response.setSyncData(commandDefMap);
        return response;
    }

    public void addCommand(Command command) {
        logger.debug("Регистрация новой команды: {}", command.getName());
        Field[] fields = command.getClass().getDeclaredFields();

        for (Field field: fields) {
            if (!field.isAnnotationPresent(Inject.class)) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object toInject = resolveDependency(field.getType());
                if (toInject == null) {
                    continue;
                }
                field.set(command, toInject);
                logger.debug("В команду {} внедрен {}", command.getName(), field.getType().getSimpleName());

            } catch (IllegalAccessException e) {
                logger.error("Не удалось внедрить зависимость в поле {}", field.getName(), e);
            }
        }
        commands.put(command.getName(), command);
        logger.info("Команда {} зарегистрирована", command.getName());
    }

    private Object resolveDependency(Class<?> type) {
        return switch (type.getSimpleName()) {
            case "CollectionManager" -> collectionManager;
            case "CommandManager" -> this;
            default -> null;
        };
    }

    private void registerAllCommands() {
        addCommand(new AddCommand());
        addCommand(new AverageOfPriceCommand());
        addCommand(new ClearCommand());
        addCommand(new FilterStartsWithNameCommand());
        addCommand(new InfoCommand());
        addCommand(new RemoveCommand());
        addCommand(new ShowCommand());
        addCommand(new ShuffleCommand());
        addCommand(new SortCommand());
        addCommand(new SumOfPriceCommand());
        addCommand(new UpdateCommand());
    }
}
