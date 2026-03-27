package commands;

import network.Request;

import java.util.List;

public abstract class Command {
    private final String name;
    private final String description;
    // private final int expectArgs;
    // private final List<ArgType> expectArgs;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
        // this.expectArgs = expectArgs;
    }

    public abstract Request execute(String[] tokens);

    public abstract String getSyntax();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
