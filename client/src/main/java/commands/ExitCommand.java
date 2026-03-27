package commands;

import exceptions.EndOfExecutionException;
import exceptions.InvalidArgumentException;
import network.Request;

public class ExitCommand extends Command {
    public ExitCommand() {
        super("exit", "exit - выход");
    }

    @Override
    public Request execute(String[] tokens) {
        if (tokens.length != 1) {
            throw new InvalidArgumentException("Получено слишком много аргументов для команды exit");
        }
        throw new EndOfExecutionException("Завершение работы...");
    }

    @Override
    public String getSyntax() {
        return "exit [NONE]";
    }
}
