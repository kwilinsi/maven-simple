package com.github.kwilinsi.commandsSystem.types.function;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public enum ArgType {
    ARGUMENT_STRING, ARGUMENT_BOOLEAN, ARGUMENT_INTEGER, ARGUMENT_DOUBLE;

    /**
     * Converts a type in {@link String} form to an int based on one of the static constants defined here. Currently
     * the recognized types are: {@code str, string, bool, boolean, int, integer, dbl, double}.<br><br>
     * Note that the type is not case sensitive, as input is trimmed and converted to lowercase. However, the
     * input must not be null, and an unrecognized input will throw an exception.
     *
     * @param type the input type as a string
     * @return the type as an integer constant
     * @throws IllegalArgumentException if the input string was not recognized as a valid type defined in this class
     */
    public static ArgType getType(@NotNull String type) {
        type = type.trim().toLowerCase(Locale.ROOT);

        return switch (type) {
            case "str", "string" -> ARGUMENT_STRING;
            case "bool", "boolean" -> ARGUMENT_BOOLEAN;
            case "int", "integer" -> ARGUMENT_INTEGER;
            case "dbl", "double" -> ARGUMENT_DOUBLE;
            default -> throw new IllegalArgumentException("Unknown argument type '" + type + "'.");
        };
    }

    /**
     * Converts a type as an integer constant defined here into a readable {@link String} format. The current
     * recognized types and available outputs are: {@code string, boolean, integer, double}. If an unrecognized
     * integer is given, an exception will be thrown.
     * @param type the input type as an integer
     * @return the type as a more readable String
     */
    public @NotNull static String getTypeStr(ArgType type) {
        return switch (type) {
            case ARGUMENT_STRING -> "string";
            case ARGUMENT_BOOLEAN -> "boolean";
            case ARGUMENT_INTEGER -> "integer";
            case ARGUMENT_DOUBLE -> "double";
        };
    }

    /**
     * Checks to see whether the given input type is a number. If it is, true is returned; otherwise, false is
     * returned. The input type is compared against the constants defined in this class.
     * @param type the type to check
     * @return true if it is a number; false if it is not
     */
    public static boolean isNumber(ArgType type) {
        return type == ARGUMENT_INTEGER || type == ARGUMENT_DOUBLE;
    }
}
