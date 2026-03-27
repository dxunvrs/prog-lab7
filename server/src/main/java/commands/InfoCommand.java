package commands;

import core.CollectionManager;
import network.Request;
import network.Response;
import network.ResponseType;

import java.util.List;

public class InfoCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public InfoCommand() {
        super("info", "info - информация о коллекции",
                List.of(ArgType.NONE));
    }

    @Override
    public Response execute(Request request) {
        String responseMessage = collectionManager.getCollectionInfo();
        return new Response(ResponseType.OK, responseMessage);
    }
}
