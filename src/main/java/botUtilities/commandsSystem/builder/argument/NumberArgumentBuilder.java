package botUtilities.commandsSystem.builder.argument;

import botUtilities.commandsSystem.builder.SyntaxBuilder;
import botUtilities.commandsSystem.json.JsonBuilder;
import botUtilities.commandsSystem.json.JsonMap;
import botUtilities.commandsSystem.types.function.ArgType;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

/**
 * Expands on {@link ArgumentBuilder} objects by adding additional parameters specific to doubles, like a required
 * floor and ceiling for input validation.
 */
public class NumberArgumentBuilder extends ArgumentBuilder {
    private Double floor = null;
    private Double ceiling = null;
    private Double defaultValue = null;
    private Boolean floorInclusive = true;
    private Boolean ceilingInclusive = true;
    private Integer sigFigs = null;

    /**
     * Defines a {@link NumberArgumentBuilder} instance with the basic required arguments, a name, description,
     * and type. See the {@link ArgumentBuilder#ArgumentBuilder(String, String, String)} documentation for
     * more information.
     *
     * @param name        the name of the argument (also used when making a {@link SyntaxBuilder} using this argument)
     * @param description a short description explaining what the argument is/does in a command
     * @param type        must be one of "{@code integer}" or "{@code double}" (case sensitive)
     * @throws IllegalArgumentException if the type parameter is not "{@code integer}" or "{@code double}"
     */
    public NumberArgumentBuilder(@NotNull String name, @NotNull String description, @NotNull String type) {
        super(name, description, type);
        if (!type.equals("integer") && !type.equals("double"))
            throw new IllegalArgumentException("Type parameter must be one of \"integer\" or \"double\".");
    }

    /**
     * Sets the minimum number a user can input for this argument. By default this is inclusive, meaning the given
     * floor can be used by the user as valid input. If this is the desired result, you can use
     * {@link #setFloor(double)} as shorthand. Otherwise, you can specify a value for isInclusive. If false, the
     * user will be required to give a number greater than the floor.
     *
     * @param floor       the minimum allowed value
     * @param isInclusive true if the floor is valid input; false if the input must be greater than the floor
     * @return this {@link NumberArgumentBuilder} instance for chaining
     */
    public NumberArgumentBuilder setFloor(double floor, boolean isInclusive) {
        this.floor = floor;
        this.floorInclusive = isInclusive;
        return this;
    }

    /**
     * Sets the minimum number a user can input for this argument. By default this in inclusive, meaning the given
     * floor can be used by the user as valid input. If you need the floor to be exclusive, use
     * {@link #setFloor(double, boolean)} instead.
     *
     * @param floor the minimum allowed value
     * @return this {@link NumberArgumentBuilder} instance for chaining
     */
    public NumberArgumentBuilder setFloor(double floor) {
        this.floor = floor;
        return this;
    }

    /**
     * Sets the maximum number a user can input for this argument. By default this is inclusive, meaning the given
     * ceiling can be used by the user as valid input. If this is the desired result, you can use
     * {@link #setCeiling(double)} as shorthand. Otherwise, you can specify a value for isInclusive. If false, the
     * user will be required to give a number less than the ceiling.
     *
     * @param ceiling     the maximum allowed value
     * @param isInclusive true if the ceiling is valid input; false if the input must be less than the ceiling
     * @return this {@link NumberArgumentBuilder} instance for chaining
     */
    public NumberArgumentBuilder setCeiling(double ceiling, boolean isInclusive) {
        this.ceiling = ceiling;
        this.ceilingInclusive = isInclusive;
        return this;
    }

    /**
     * Sets the maximum number a user can input for this argument. By default this in inclusive, meaning the given
     * ceiling can be used by the user as valid input. If you need the ceiling to be exclusive, use
     * {@link #setCeiling(double, boolean)} instead.
     *
     * @param ceiling the maximum allowed value
     * @return this {@link NumberArgumentBuilder} instance for chaining
     */
    public NumberArgumentBuilder setCeiling(double ceiling) {
        this.ceiling = ceiling;
        return this;
    }

    /**
     * Sets the maximum number of significant figures a user can input for this argument. Any input they give will
     * automatically be rounded to this number of sig-figs before being evaluated to see if it matches the
     * floor/ceiling constraints (if applicable).
     *
     * @param sigFigs the maximum significant figures on input for this argument
     * @return this {@link NumberArgumentBuilder} instance for chaining
     */
    public NumberArgumentBuilder setSigFigs(int sigFigs) {
        this.sigFigs = sigFigs;
        return this;
    }

    /**
     * Sets the default value for this argument. This allows code executing a function to call the value of the
     * argument even if it wasn't provided by the user (such as for optional arguments not present in all syntaxes).
     * In that case, the method would simply receive the default value set via this method and stored in the JSON.
     *
     * @param defaultValue the default argument value
     * @return this {@link BooleanArgumentBuilder} instance for chaining
     */
    public NumberArgumentBuilder setDefaultValue(double defaultValue) {
        this.defaultValue = super.getTypeInt() == ArgType.ARGUMENT_DOUBLE ? defaultValue : Math.round(defaultValue);
        return this;
    }

    /**
     * Get a {@link JsonObject} which contains key-value pairs for the argument parameters. This includes everything
     * instantiated in the {@link ArgumentBuilder} class along with settings specific for number types.
     *
     * @return the completed {@link JsonObject}
     * @throws ClassNotFoundException if there's an error building the Json
     */
    @Override
    public @NotNull JsonObject getJson() throws ClassNotFoundException {
        return JsonBuilder.appendJsonObject(JsonMap.of()
                        .add("floor", floor)
                        .add("floorInclusive", floorInclusive)
                        .add("ceiling", ceiling)
                        .add("ceilingInclusive", ceilingInclusive)
                        .add("sigFigs", sigFigs)
                        .add("defaultValue", defaultValue),
                super.getJson());
    }
}