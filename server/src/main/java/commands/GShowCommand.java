package commands;

import core.CollectionManager;

import java.util.List;

public class GShowCommand extends Command {
    @Inject
    private CollectionManager collectionManager;

    public GShowCommand() {
        super("gshow", "gshow - вывод элементов коллекции для gui",
                List.of(ArgType.NONE));
    }

    @Override
    public CommandData execute(CommandContext context) {
        CommandData commandData = new CommandData(collectionManager.getCollectionInfo());
        commandData.setProducts(collectionManager.getCollection());
        return commandData;
    }
}
