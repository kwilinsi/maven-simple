package com.github.kwilinsi.commandsSystem.builder.argument;

import com.github.kwilinsi.commandsSystem.builder.SyntaxBuilder;
import com.github.kwilinsi.commandsSystem.json.JsonBuilder;
import com.github.kwilinsi.commandsSystem.json.JsonMap;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

public class BooleanArgumentBuilder extends ArgumentBuilder {
    private boolean defaultValue;

    /**
     * Creates a new {@link BooleanArgumentBuilder} instance with the three default required arguments: name,
     * description, and type (the latter being automatically set to "{@code boolean}"). See the documentation
     * for {@link ArgumentBuilder#ArgumentBuilder(String, String, String)} for more information on these parameters.
     *
     * @param name        the name of the argument (also used when making a {@link SyntaxBuilder} using this argument)
     * @param description a short description explaining what the argument is/does in a command
     */
    public BooleanArgumentBuilder(@NotNull String name, @NotNull String description) {
        super(name, description, "boolean");
    }

    /**
     * Sets the default value for this argument. This allows code executing a function to call the value of the
     * argument even if it wasn't provided by the user (such as for optional arguments not present in all syntaxes).
     * In that case, the method would simply receive the default value set via this method and stored in the JSON.
     *
     * @param defaultValue the default argument value
     * @return this {@link BooleanArgumentBuilder} instance for chaining
     */
    public BooleanArgumentBuilder setDefaultValue(boolean defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    /**
     * Get a {@link JsonObject} which contains key-value pairs for the argument parameters. This includes everything
     * instantiated in the {@link ArgumentBuilder} class along with settings specific for boolean types.
     *
     * @return the completed {@link JsonObject}
     * @throws ClassNotFoundException if there's an error building the Json
     */
    @Override
    public @NotNull JsonObject getJson() throws ClassNotFoundException {
        return JsonBuilder.appendJsonObject(
                JsonMap.of().add("defaultValue", defaultValue),
                super.getJson());
    }
}
