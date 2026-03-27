package commands;

import core.CollectionManager;
import network.Request;
import network.Response;
import network.ResponseType;

import java.util.List;

public class ShowCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public ShowCommand() {
        super("show", "show - вывод элементов коллекции",
                List.of(ArgType.NONE));
    }

    @Override
    public Response execute(Request request) {
        String responseMessage = collectionManager.getFormattedCollection(product -> true);
        return new Response(ResponseType.OK, responseMessage);
    }
}
