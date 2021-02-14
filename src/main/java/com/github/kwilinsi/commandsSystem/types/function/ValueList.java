package com.github.kwilinsi.commandsSystem.types.function;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Represents a collection of Value objects being processed. Every time someone sends a function command, an
 * instance of this object is created. It is built upon the function the user is using, and it contains the
 * arguments in the syntax the user chose along with the specific values provided for those arguments.
 */
public class ValueList extends ArrayList<Value> implements List<Value> {
    private final Argument[] arguments;

    /**
     * Instantiates a new ValueCollection object based on `length`, the number of Value objects that will be assigned.
     * This should be the number of arguments that the user provided in their command, as each argument will become
     * a Value object in the array.
     *
     * @param length the number of arguments the user provided
     */
    public ValueList(int length, Argument[] arguments) {
        super(length);
        this.arguments = arguments;
    }

    /**
     * Adds a new Value object to the end of the list and validates it.
     *
     * @param value the Value object to add
     */
    public void addAndValidate(Value value) {
        add(value);
        value.validate();
    }

    /**
     * Attempts to get the Value with the matching key (case insensitive). If no such value can be found,
     * an exception is thrown.
     *
     * @param key the name of the desired argument/value
     * @return the Value object
     * @throws NoSuchElementException if no Value with the given name was found
     */
    private Value getValue(@NotNull String key) {
        for (Value value : this)
            if (value.matches(key))
                return value;
        throw new NoSuchElementException("Failed to find Value with key '" + key + "'.");
    }

    /**
     * Attempts to get the Argument with the matching key (case insensitive). If no such argument can be found,
     * an exception is thrown.
     *
     * @param key the name of the desired argument
     * @return the Argument object
     * @throws NoSuchElementException if no Argument with the given name was found
     */
    private Argument getArgument(@NotNull String key) {
        for (Argument argument : arguments)
            if (argument.matches(key))
                return argument;
        throw new NoSuchElementException("Failed to find Argument with key '" + key + "'.");
    }

    /**
     * Get the default value of an Argument for this function by specifying its key. If no Argument with that name
     * can be found, null is returned.
     *
     * @param key the name of the Argument to get the default value of.
     * @return the Argument's default value, or null if no Argument with a matching name found.
     */
    private String getDefaultValue(@NotNull String key) {
        try {
            return getArgument(key).getDefaultValue();
        } catch (Exception ignore) {
            return null;
        }
    }

    /**
     * Check to see if there's any Value with the specified key name. If any Value is found with the matching name,
     * true is returned; otherwise false is returned after checking every Value.
     *
     * @param key the name of the argument to look for (as written in the json, but case insensitive)
     * @return true if any Value with a matching name is found; false otherwise
     */
    public boolean hasValue(@NotNull String key) {
        for (Value value : this)
            if (value.matches(key))
                return true;
        return false;
    }

    /**
     * Get a Value by specifying it's name. Though this is case insensitive, it must be written exactly the same
     * as the name of the Argument in the json file for the function. If no Value with a matching key is found, the
     * default value of the Argument is returned. If no Argument with the right name can be found either, or no
     * default value was set for that Argument, 0 is returned.
     *
     * @param argument the name of the Value
     * @return the integer of the Value or the default value for the argument (or 0 if all else fails)
     */
    public int getInt(@NotNull String argument) {
        try {
            return getValue(argument).getValueInt();
        } catch (Exception ignore) {
            String def = getDefaultValue(argument);
            return def == null ? 0 : (int) Double.parseDouble(def);
        }
    }

    /**
     * Get a Value by specifying it's name. Though this is case insensitive, it must be written exactly the same
     * as the name of the Argument in the json file for the function. If no Value with a matching key is found, the
     * default value of the Argument is returned. If no Argument with the right name can be found either, or no
     * default value was set for that Argument, 0 is returned.
     *
     * @param argument the name of the Value
     * @return the double of the Value or the default value for the argument (or 0 if all else fails)
     */
    public double getDouble(@NotNull String argument) {
        try {
            return getValue(argument).getValueDouble();
        } catch (Exception ignore) {
            String def = getDefaultValue(argument);
            return def == null ? 0 : Double.parseDouble(def);
        }
    }

    /**
     * Get a Value by specifying it's name. Though this is case insensitive, it must be written exactly the same
     * as the name of the Argument in the json file for the function. If no Value with a matching key is found, the
     * default value of the Argument is returned. If no Argument with the right name can be found either, or no
     * default value was set for that Argument, null is returned.
     *
     * @param argument the name of the Value
     * @return the String of the Value or the default value for the argument (or null if all else fails)
     */
    public String getString(@NotNull String argument) {
        try {
            return getValue(argument).getValueString();
        } catch (Exception ignore) {
            return getDefaultValue(argument);
        }
    }

    /**
     * Get a Value by specifying it's name. Though this is case insensitive, it must be written exactly the same
     * as the name of the Argument in the json file for the function. If no Value with a matching key is found, the
     * default value of the Argument is returned. If no Argument with the right name can be found either, or no
     * default value was set for that Argument, false is returned (as this is the result of Boolean.parseBoolean()).
     *
     * @param argument the name of the Value
     * @return the boolean of the Value or the default value for the argument (or false if all else fails)
     */
    public boolean getBoolean(@NotNull String argument) {
        try {
            return getValue(argument).getValueBoolean();
        } catch (Exception ignore) {
            String def = getDefaultValue(argument);
            return Boolean.parseBoolean(def);
        }
    }

    /**
     * Get an array of all Values that match the specified name. Though this is case insensitive, it must be written
     * exactly the same as the name of the Argument in the json file for the function. If no Value with a matching
     * key is found, an empty array is returned. The default value for the argument is never used
     *
     * @param argument the name of the Value
     * @return an array of matching double Values, or if none match then an empty array
     */
    public double[] getArrayDouble(@NotNull String argument) {
        List<Double> result = new ArrayList<>();
        for (Value value : this)
            if (value.matches(argument))
                result.add(value.getValueDouble());

        return result.stream().mapToDouble(Double::doubleValue).toArray();
    }

    /**
     * Get an array of all Values that match the specified name. Though this is case insensitive, it must be written
     * exactly the same as the name of the Argument in the json file for the function. If no Value with a matching
     * key is found, an empty array is returned. The default value for the argument is never used
     *
     * @param argument the name of the Value
     * @return an array of matching integer Values, or if none match then an empty array
     */
    public int[] getArrayInteger(@NotNull String argument) {
        List<Integer> result = new ArrayList<>();
        for (Value value : this)
            if (value.matches(argument))
                result.add(value.getValueInt());

        return result.stream().mapToInt(Integer::intValue).toArray();
    }

    /**
     * Get an array of all Values that match the specified name. Though this is case insensitive, it must be written
     * exactly the same as the name of the Argument in the json file for the function. If no Value with a matching
     * key is found, an empty array is returned. The default value for the argument is never used
     *
     * @param argument the name of the Value
     * @return an array of matching String Values, or if none match then an empty array
     */
    public String[] getArrayString(@NotNull String argument) {
        ArrayList<String> result = new ArrayList<>();
        for (Value value : this)
            if (value.matches(argument))
                result.add(value.getValueString());

        return result.toArray(new String[0]);
    }

    /**
     * Get an array of all Values that match the specified name. Though this is case insensitive, it must be written
     * exactly the same as the name of the Argument in the json file for the function. If no Value with a matching
     * key is found, an empty array is returned. The default value for the argument is never used
     *
     * @param argument the name of the Value
     * @return an array of matching boolean Values, or if none match then an empty array
     */
    public boolean[] getArrayBoolean(@NotNull String argument) {
        ArrayList<Boolean> result = new ArrayList<>();
        for (Value value : this)
            if (value.matches(argument))
                result.add(value.getValueBoolean());

        // There's no convenient method for using a stream to convert ArrayList<Boolean> to boolean[] so for loop used
        boolean[] r = new boolean[result.size()];
        for (int i = 0; i < result.size(); i++)
            r[i] = result.get(i);
        return r;
    }
}