package commands;

import core.CollectionManager;
import network.Request;
import network.Response;
import network.ResponseType;

import java.util.List;

public class ClearCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public ClearCommand() {
        super("clear", "clear - очистить коллекцию", List.of(ArgType.NONE));
    }

    @Override
    public Response execute(Request request) {
        collectionManager.clearCollection();
        return new Response(ResponseType.OK, "Коллекция очищена");
    }
}
