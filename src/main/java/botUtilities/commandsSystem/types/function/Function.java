package botUtilities.commandsSystem.types.function;

import botUtilities.commandsSystem.json.JsonParser;
import botUtilities.tools.Checks;
import botUtilities.commandsSystem.types.Command;
import botUtilities.commandsSystem.manager.CommandManager;
import botUtilities.exceptions.InvalidMethodException;
import botUtilities.exceptions.JsonParseException;
import botUtilities.exceptions.SyntaxException;
import botUtilities.tools.EmbedField;
import botUtilities.tools.GenericUtils;
import botUtilities.tools.MessageUtils;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * This class takes the Command object a step further by adding support for custom arguments
 */
public class Function extends Command {
    private final Argument[] arguments;
    private final Syntax[] syntaxes;

    protected Function(@NotNull JsonObject json, @NotNull CommandManager manager) throws JsonParseException {
        super(json, manager);
        this.arguments = Argument.ofArray(JsonParser.getJsonObjectArray(json, "arguments"));
        syntaxes = Syntax.ofArray(JsonParser.getJsonArrayArray(json, "syntax"), this);
    }

    /**
     * Parses a command from a user, determines the answer, and sends it to them. Starts by validating all their
     * arguments to be in the required range and the right number of args.
     *
     * @param strArgs the message the user sent separated by spaces. strArgs[0] is the name of the command
     * @param channel the channel the user sent their message in, which is where the reply should go
     */
    public void process(String[] strArgs, @NotNull MessageChannel channel, Method method) {
        try {
            // Parse the arguments from the command. If the result is null, it means the arguments weren't parsed and
            // something else (a help embed or error message) was sent to the user
            ValueList values = parseArguments(strArgs, channel);
            if (values == null)
                return;

            // Confirm that the method takes the right parameters and run it
            Checks.checkMethodParameterTypes(method, new Class<?>[]{ValueList.class, MessageChannel.class});
            method.invoke(values, channel);

        } catch (SyntaxException e) {
            // Catches SyntaxExceptions and ArgumentExceptions, sending the user the proper syntax in the error
            sendSyntaxError(e.getMessage(), channel, e.getSyntax());

        } catch (InvalidMethodException e) {
            sendError(channel, e.getMessage());

        } catch (Exception e) {
            // Unknown/unanticipated exceptions are also printed to the console
            sendError(channel, e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Attempts to parse the arguments provided by the user based on the syntax of this command. If that fails,
     * an error message is sent to the user (or simply the help panel if that's what they wanted) and the method
     * returns null to indicate that the arguments were not parsed.
     *
     * @param strArgs the arguments provided by the user, split by spaces
     * @param channel the channel to send a response to if there's an error
     * @return an array of Values corresponding to strArgs if the method works; null if args weren't parsed
     */
    protected @Nullable ValueList parseArguments(@NotNull String[] strArgs, @NotNull MessageChannel channel)
            throws Exception {

        // Check if the user merely wants the info/help panel for this command
        if (checkInfoRequest(infoEmbed, strArgs, channel, 2))
            return null;

        // Figure out what the data type of each of the arguments the user provided is
        ArgType[] inputTypes = Argument.getMultiTypes(strArgs);

        Exception error = null;

        // For each syntax...
        for (Syntax syntax : syntaxes) {

            // Check to see if it matches the arguments the user entered. If not, try the next syntax
            String[] syntaxArgs = syntax.matches(inputTypes);
            if (syntaxArgs == null)
                continue;

            // Matching syntax found. Parse it to check for violations of upper/lower bounds and stuff and add
            // it to the ValueCollection
            ValueList values = new ValueList(syntaxArgs.length, arguments);
            try {
                for (int i = 0; i < syntaxArgs.length; i++)
                    values.addAndValidate(new Value(getArgument(syntaxArgs[i]), strArgs[i + 1]));
            } catch (Exception e) {
                // There was an error parsing the Syntax, but it's possible that different syntax will fit
                // better. Try all the other syntaxes, and if none of them work then we can send this error
                if (error == null)
                    error = e;
                continue;
            }

            // At this point all of the arguments have been successfully parsed.
            return values;
        }

        // If the syntax loop ends without finding a matching syntax, throw an exception.
        if (error != null)
            // Throw the cached error if a syntax matched but there was an index out of bounds or something
            throw error;
        else
            // Otherwise none of the syntaxes matched at all. Send an error for that.
            throw new SyntaxException("Syntax error. The given argument types do not match " +
                    (syntaxes.length == 1 ? "the command syntax." :
                            (syntaxes.length == 2 ? "either" : "any") + " of the syntaxes for this command."));
    }

    /**
     * Get an {@link Argument} object from its name (case insensitive)
     *
     * @param argName the name of the {@link Argument}
     * @return the matching {@link Argument}, or null if none could be found
     */
    protected @Nullable Argument getArgument(@NotNull String argName) {
        for (Argument arg : arguments)
            if (arg.getName().equalsIgnoreCase(argName))
                return arg;
        return null;
    }

    /**
     * Combines the arguments with their descriptions and puts all that plus the command description and aliases
     * in a nice pretty {@link EmbedBuilder} using {@link MessageUtils}.
     *
     * @return a finished EmbedBuilder with all the info about this function
     * @throws IllegalArgumentException if there is an error assembling the {@link EmbedBuilder}, likely due to
     *                                  unresolvable URLs or exceeding character limits
     */
    @Override
    protected @NotNull EmbedBuilder getInfo() {
        StringBuilder s = new StringBuilder();
        for (Argument arg : getArguments())
            s.append("\n").append(arg.getDescriptionFormat());
        String syntaxDesc = s.substring(1);

        return MessageUtils.makeEmbedBuilder(
                getName() + " Info",
                getLink(),
                "",
                manager.getConfig().getCommandInfoColor(),
                null, null, null, null,
                null, null, null, null,
                new EmbedField[]{
                        EmbedField.of("Description", getDescription()),
                        EmbedField.of("Syntax" + (syntaxes.length > 1 ? "es" : ""), getSyntaxes()),
                        EmbedField.of("Arguments", syntaxDesc),
                        EmbedField.of("Alias" + (getAliases().length > 1 ? "es" : ""),
                                GenericUtils.mergeList(getAliasesArray(), "and"))
                });
    }

    /**
     * Send an error message to the user along with the proper syntax for the command they tried to use. If a
     * Syntax object is provided, only that Syntax is listed in the embed. Otherwise, if null is passed for the
     * Syntax then all the Syntaxes associated with the function are displayed.
     *
     * @param error   the error message to display at the top of the embed
     * @param channel the channel to send the error in
     * @param syntax  the syntax to display under the error (or null to list all syntaxes)
     */
    protected void sendSyntaxError(String error, MessageChannel channel, Syntax syntax) {
        // Set the Syntax part to either all the syntaxes (if syntax is null) or only the one provided
        EmbedField syntaxField = EmbedField.of(
                "Syntax" + (syntaxes.length > 1 && syntax == null ? "es" : ""),
                syntax == null ? getSyntaxes() : "```\n" + syntax.toString() + "```");

        sendError(channel, error, syntaxField);
    }

    /**
     * Gets a list of all the valid Syntaxes for this command each in their own code block appended to one another.
     * If there's only one syntax for the command, this will simply look like the syntax enclosed in a code block.
     *
     * @return the list of syntaxes
     */
    protected String getSyntaxes() {
        StringBuilder s = new StringBuilder();
        for (Syntax syntax : this.syntaxes)
            s.append("```\n").append(syntax.toString()).append("```");

        return s.toString();
    }

    protected Argument[] getArguments() {
        return arguments;
    }
}
