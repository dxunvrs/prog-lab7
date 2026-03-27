package commands;

import exceptions.InvalidArgumentException;
import io.InputManager;
import network.Request;
import network.RequestType;
import utility.ProductForm;

import java.util.List;
import java.util.stream.Collectors;

public class ServerCommand extends Command {
    private final List<ArgType> expectedArgs;

    @Inject
    private InputManager inputManager;

    public ServerCommand(String name, String description, List<ArgType> expectedArgs) {
        super(name, description);
        this.expectedArgs = expectedArgs;
    }

    @Override
    public Request execute(String[] tokens) {
        Request request = new Request(RequestType.SERVER_COMMAND, getName());
        if (tokens.length-1 != getExpectedArgsLength()) {
            throw new InvalidArgumentException("Передано неверное количество элементов, получено: " + (tokens.length-1) + ", ожидалось: " + getExpectedArgsLength());
        }
        int index = 1;
        try {
            for (ArgType argType: expectedArgs) {
                switch (argType) {
                    case STR -> request.addStringArg(tokens[index]);
                    case INT -> request.addIntArg(Integer.parseInt(tokens[index]));
                    case OBJECT ->  {
                        index--;
                        request.addObjectArg(new ProductForm(inputManager).getProduct());
                    }
                    case NONE -> index--;
                }
                index++;
            }
        } catch (NumberFormatException e) {
            throw new InvalidArgumentException("Неверный формат числа у " + index + " аргумента");
        } catch(IndexOutOfBoundsException e) {
            throw new InvalidArgumentException("Передано неверное количество аргументов, ошибка на " + index + " аргументе");
        }
        return request;
    }

    private int getExpectedArgsLength() {
        return expectedArgs.stream().mapToInt(ArgType::getWeight).sum();
    }

    @Override
    public String getSyntax() {
        return getName() + " " + expectedArgs.stream().map(Enum::name).collect(Collectors.joining(", ", "[", "]"));
    }
}
