package com.github.kwilinsi.jda.command.manager.exceptions;

import com.github.kwilinsi.jda.command.manager.commandsSystem.types.function.Argument;
import com.github.kwilinsi.jda.command.manager.commandsSystem.types.function.Syntax;

/**
 * Reports a problem with a specific Argument in a specified Syntax being parsed
 */
public class ArgumentException extends SyntaxException {
    /**
     * Creates an exception with a message formatting "**Argument Exception:** Failed to parse [arg name].
     * Input [input] [message]."
     *
     * @param message the text to put after the preface
     */
    public ArgumentException(Argument argument, String input, Syntax syntax, String message) {
        super(
                "**Argument Exception:** Failed to parse __" + argument.getName() +
                        "__. Input " + input + " " + message + ".",
                syntax);
    }
}