package commands;

import core.CollectionManager;
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
    public Response execute(CommandContext context) {
        String responseMessage = collectionManager.getFormattedCollection(product -> true,
                context.getCurrentUserId());
        return new Response(ResponseType.OK, responseMessage);
    }
}
