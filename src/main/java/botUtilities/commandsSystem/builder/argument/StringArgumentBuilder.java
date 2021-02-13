package botUtilities.commandsSystem.builder.argument;

import botUtilities.commandsSystem.builder.SyntaxBuilder;
import botUtilities.commandsSystem.json.JsonBuilder;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class StringArgumentBuilder extends ArgumentBuilder {
    private String defaultValue;
    private String[] allowedValues;

    /**
     * Creates a new {@link StringArgumentBuilder} instance with the three default required arguments: name,
     * description, and type (the latter being automatically set to "{@code string}"). See the documentation
     * for {@link ArgumentBuilder#ArgumentBuilder(String, String, String)} for more information on these parameters.
     *
     * @param name        the name of the argument (also used when making a {@link SyntaxBuilder} using this argument)
     * @param description a short description explaining what the argument is/does in a command
     */
    public StringArgumentBuilder(@NotNull String name, @NotNull String description) {
        super(name, description, "string");
    }

    /**
     * Sets the default value for this argument. This allows code executing a function to call the value of the
     * argument even if it wasn't provided by the user (such as for optional arguments not present in all syntaxes).
     * In that case, the method would simply receive the default value set via this method and stored in the JSON.
     * @param defaultValue the default argument value
     * @return this {@link StringArgumentBuilder} instance for chaining
     */
    public StringArgumentBuilder setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Calling this method restricts input for this argument to only the set of allowed values. Any values entered by
     * the user which are not present in this list will throw an error and the user will be given a list of the valid
     * values they can use for the argument. It is suggested to also include this list in the description for the
     * argument while instantiating the {@link StringArgumentBuilder} object.
     * @param option one or more allowed values for the argument
     * @return this {@link StringArgumentBuilder} instance for chaining
     */
    public StringArgumentBuilder setAllowedValues(@NotNull String... option) {
        this.allowedValues = option;
        return this;
    }

    /**
     * Get a {@link JsonObject} which contains key-value pairs for the argument parameters. This includes everything
     * instantiated in the {@link ArgumentBuilder} class along with settings specific for String types.
     *
     * @return the completed {@link JsonObject}
     * @throws ClassNotFoundException if there's an error building the Json
     */
    @Override
    public @NotNull JsonObject getJson() throws ClassNotFoundException {
        // TODO add a list of allowed values that are shown to the user and allowed values that aren't shown to them

        Map<String, Object> map = new HashMap<>();

        map.put("defaultValue", defaultValue);
        map.put("allowedValues", JsonBuilder.buildJsonArray(allowedValues));

        return JsonBuilder.appendJsonObject(map, super.getJson());
    }
}
