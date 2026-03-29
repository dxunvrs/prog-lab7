package commands;

import core.CollectionManager;
import network.Response;
import network.ResponseType;

import java.util.List;

public class SortCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public SortCommand() {
        super("sort", "sort - сортировка коллекции в естественном порядке (по id)",
                List.of(ArgType.NONE));
    }

    @Override
    public Response execute(CommandContext context) {
        collectionManager.sort();
        String responseMessage = "Коллекция отсортирована в естественном порядке, введите show для просмотра";
        return new Response(ResponseType.OK, responseMessage);
    }
}
