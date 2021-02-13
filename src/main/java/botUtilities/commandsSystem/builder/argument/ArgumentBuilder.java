package botUtilities.commandsSystem.builder.argument;

import botUtilities.commandsSystem.builder.SyntaxBuilder;
import botUtilities.commandsSystem.json.JsonBuilder;
import botUtilities.commandsSystem.json.JsonMap;
import botUtilities.commandsSystem.types.function.ArgType;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class ArgumentBuilder {
    private final String name;
    private final String description;
    private final String type;

    /**
     * Creates a new {@link ArgumentBuilder} instance with the three default required arguments that are used for
     * all argument types: a name, a description, and a type.
     * <p>
     * The name should be a short string that summarizes what the argument represents in a few characters as possible
     * while still being understandable.
     * <p>
     * The description is a short (sentence or two) explanation of the argument that the user sees when they request
     * the syntax help panel for a command.
     * <p>
     * And the type is automatically set when calling one of the subclass methods that extend this. It describes the
     * datatype required of the user for the argument: double, integer, string, or boolean.
     *
     * @param name        the name of the argument (also used when making a {@link SyntaxBuilder} using this argument)
     * @param description a short description explaining what the argument is/does in a command
     * @param type        the argument data type
     */
    public ArgumentBuilder(@NotNull String name, @NotNull String description, @NotNull String type) {
        this.name = name;
        this.description = description;
        this.type = type;
    }

    /**
     * Retrieve the name of the argument, useful for checking against a {@link SyntaxBuilder}.
     *
     * @return the argument name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the all lowercase String (if it was set properly) type representing the data type for this argument.
     *
     * @return the data type
     */
    public String getType() {
        return type;
    }

    /**
     * Get the {@link ArgType} constant representing the data type of this argument. This is the same as calling
     * {@link ArgType#getType(String)} on the result of the standard {@link #getType()} getter.
     *
     * @return the data type integer constant
     */
    public ArgType getTypeInt() {
        return ArgType.getType(type);
    }

    /**
     * Get a {@link JsonObject} which contains key-value pairs for the basic argument parameters: name, description,
     * and type. Extend this class to add more parameters.
     *
     * @return the completed {@link JsonObject}
     * @throws ClassNotFoundException if there's an error building the Json
     */
    public @NotNull JsonObject getJson() throws ClassNotFoundException {
        return JsonBuilder.buildJsonObject(JsonMap.of()
                .add("name", name)
                .add("description", description)
                .add("type", type));
    }
}
