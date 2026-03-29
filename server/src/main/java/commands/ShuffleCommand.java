package commands;

import core.CollectionManager;
import network.Response;
import network.ResponseType;

import java.util.List;

public class ShuffleCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public ShuffleCommand() {
        super("shuffle", "shuffle - перемешать коллекцию в случайном порядке",
                List.of(ArgType.NONE));
    }

    @Override
    public Response execute(CommandContext context) {
        collectionManager.randomSort();
        String responseMessage = "Коллекция перемешана, введите show для просмотра";
        return new Response(ResponseType.OK, responseMessage);
    }
}
