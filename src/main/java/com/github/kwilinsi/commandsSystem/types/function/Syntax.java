package com.github.kwilinsi.commandsSystem.types.function;

import com.github.kwilinsi.exceptions.JsonParseException;
import com.github.kwilinsi.commandsSystem.json.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Syntax {
    private final ArrayList<ArgumentGroup> arguments = new ArrayList<>();
    private final Function function;

    private Syntax(@NotNull JsonArray array, @NotNull Function function) throws JsonParseException {
        this.function = function;
        for (Object j : array)
            arguments.add(new ArgumentGroup(j, function));
    }

    /**
     * Creates a new {@link Syntax} instance based on a {@link JsonArray} and then {@link Function} that this syntax
     * belongs to.
     *
     * @param array    the {@link JsonArray} with the Json data for this syntax
     * @param function the {@link Function} this syntax belongs to, which contains the {@link Argument} array
     * @return a newly created {@link Syntax}
     * @throws JsonParseException if there is an error parsing the Json
     */
    public static Syntax of(@NotNull JsonArray array, @NotNull Function function) throws JsonParseException {
        return new Syntax(array, function);
    }

    /**
     * Converts multiple {@link JsonArray} instances into an array of {@link Syntax} instances by iterating over each of
     * the arrays and converting them with {@link Syntax}.{@link #of(JsonArray, Function)}. All of the {@link Syntax}
     * instances will be assigned the same {@link Function} parent.
     *
     * @param arrays   the input array of {@link JsonArray} instances from a {@link Function} Json file
     * @param function the {@link Function} that owns all of these syntaxes
     * @return an array of newly created {@link Syntax} instances
     * @throws JsonParseException if there is an error parsing the Json for any of the syntaxes
     */
    public static Syntax[] ofArray(@NotNull JsonArray[] arrays, @NotNull Function function) throws JsonParseException {
        Syntax[] array = new Syntax[arrays.length];
        for (int i = 0; i < arrays.length; i++)
            array[i] = of(arrays[i], function);
        return array;
    }

    /**
     * Checks to see whether this {@link Syntax} matches all the data types given by a user in the proper order. If it
     * does match, an array containing the names of all the arguments matched in correct order is returned. Otherwise
     * null is returned.
     *
     * @param inputTypes an array of the types of variables the user used
     * @return an array of the names of the arguments that match what the user provided, or null if nothing matched
     */
    public String[] matches(ArgType[] inputTypes) {
        ArrayList<String> argNames = new ArrayList<>();

        int argumentIndex = 0;
        int repetitions = 0;
        // Represents the argumentIndex when repetitions were last invoked
        int indexOfLastRepetition = -1;

        // Go through each of the types given by the user and see if it can match this syntax
        try {

            while (inputTypes != null && inputTypes.length > 0) {

                boolean canRepeat =
                        argumentIndex > 0 && arguments.get(argumentIndex - 1).getRepetitions() > repetitions + 1;

                // If argumentIndex is out of bounds, do a repetition on the last one
                if (canRepeat && argumentIndex >= arguments.size()) {
                    argumentIndex--;
                    repetitions++;
                    indexOfLastRepetition = argumentIndex;
                    continue;
                }

                ArgumentGroup arg = arguments.get(argumentIndex);

                // Check to see if all the arguments in this group match the upcoming ones from inputTypes
                boolean matches = true;
                for (int subIndex = 0; subIndex < arg.groupSize(); subIndex++)
                    // If a single one doesn't work, make matches false and stop checking
                    if (!Argument.doesArgumentTypeMatch(arg.getType(subIndex), inputTypes[subIndex])) {
                        matches = false;
                        break;
                    }

                // If all args in the group matched remove them from the queue and move on to the next group
                if (matches) {
                    // If the current repetition counter isn't referencing this argument group, reset it
                    // Otherwise leave the counter going because we might be coming back to this argumentIndex
                    if (indexOfLastRepetition != argumentIndex)
                        repetitions = 0;

                    inputTypes = removeFirstItems(inputTypes, arg.groupSize());
                    argNames.addAll(Arrays.asList(arg.getNames()));
                    argumentIndex++;
                    continue;
                }

                // Otherwise check to see if the previous argument can do repetitions and if so try it
                if (canRepeat) {
                    argumentIndex--;
                    repetitions++;
                    indexOfLastRepetition = argumentIndex;
                    continue;
                }

                // If we can't do repetitions of the previous group and this group didn't match, the Syntax doesn't
                // work. Return false to indicate that it's not a match.
                return null;
            }
        } catch (Exception ignore) {
            // There's tons of potential for index out of bounds errors and other runtime stuff in this while loop.
            // If any of that happens though, it means this Syntax doesn't work so just return false.
            return null;
        }

        // If we made it through the entire Syntax without anything breaking, it works!
        return argNames.toArray(new String[0]);
    }

    /**
     * Takes an array of integers as input and removes the first numToRemove of them. Then returns an output array
     * identical to the first but with those first few items removed.
     *
     * @param array       the input array
     * @param numToRemove the number of items to remove from the start of the input array
     * @return the output array with the specified number of items removed
     */
    private static ArgType[] removeFirstItems(ArgType[] array, int numToRemove) {
        ArgType[] newArray = new ArgType[array.length - numToRemove];
        if (newArray.length >= 0) System.arraycopy(array, numToRemove, newArray, 0, newArray.length);
        return newArray;
    }

    public String toString() {
        StringBuilder s = new StringBuilder(function.getManager().getMainPrefix() + function.getNameLower());
        for (ArgumentGroup g : arguments)
            s.append(" ").append(g.toString());
        return s.toString();
    }

    private static class ArgumentGroup {
        private final String[] names;
        private final ArgType[] types;
        private final int repetitions;

        public ArgumentGroup(Object j, Function function) throws JsonParseException {
            JsonObject json;

            // Attempt to treat the input Object as a JSONObject with data on the ArgumentGroup
            try {
                json = (JsonObject) j;
            } catch (Exception ignore) {
                // If that fails it's just a string so easy to parse. Set the instance variables and exit.
                this.names = new String[]{(String) j};
                this.repetitions = 1;
                try {
                    this.types = new ArgType[]{Objects.requireNonNull(function.getArgument(names[0])).getType()};
                } catch (Exception ignore2) {
                    throw new JsonParseException("There should have been an argument assigned to this function " +
                            "called '" + names[0] + "' because it was referenced in a syntax, but no matching " +
                            "argument could be found.");
                }
                return;
            }

            // If this point is reached we need to parse the ArgumentGroup data out of the JSONObject
            this.names = JsonParser.getStringArray(json, "args");

            // Set the argument types by getting the actual Argument object referenced by the name string
            this.types = new ArgType[this.names.length];
            for (int i = 0; i < this.names.length; i++)
                try {
                    types[i] = Objects.requireNonNull(function.getArgument(names[i])).getType();
                } catch (Exception ignore) {
                    throw new JsonParseException("There should have been an argument assigned to this function " +
                            "called '" + names[i] + "' because it was referenced in a syntax, but no matching " +
                            "argument could be found.");
                }

            this.repetitions = JsonParser.getInteger(json, "maxRepetitions");
        }

        public String[] getNames() {
            return names;
        }

        public int getRepetitions() {
            return repetitions;
        }

        public String getName(int index) {
            return names[index];
        }

        public String getName() {
            return getName(0);
        }

        public ArgType getType(int index) {
            return types[index];
        }

        public ArgType getType() {
            return getType(0);
        }

        public int groupSize() {
            return names.length;
        }

        public String toString() {
            StringBuilder s = new StringBuilder();

            for (String name : names)
                s.append(" [").append(name).append("]");

            // If there's multiple allowed repetitions enclose the arguments in a curly braces with a factor
            if (repetitions > 1)
                return "{" + s.substring(1) + " x" + repetitions + "}";
            else
                return s.substring(1);
        }
    }
}
