package commands;

import core.CollectionManager;
import network.Response;
import network.ResponseType;

import java.util.List;

public class FilterStartsWithNameCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public FilterStartsWithNameCommand() {
        super("filter_starts_with_name", "filter_starts_with_name - вывести элементы, название которых начинается с заданной подстроки",
                List.of(ArgType.STR));
    }

    @Override
    public Response execute(CommandContext context) {
        String responseMessage = collectionManager.getFormattedCollection(product -> product.getName().startsWith(context.getStringArgs().get(0)),
                context.getCurrentUserId());
        return new Response(ResponseType.OK, responseMessage);
    }
}
