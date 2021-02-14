package com.github.kwilinsi.jda.command.manager.commandsSystem.types;

import com.github.kwilinsi.jda.command.manager.commandsSystem.manager.CommandManager;
import com.github.kwilinsi.jda.command.manager.exceptions.JsonParseException;
import com.github.kwilinsi.jda.command.manager.commandsSystem.json.JsonParser;
import com.github.kwilinsi.jda.command.manager.tools.*;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * This is the root of the Commands object tree. It contains the basic information required for a command to be
 * implemented in the bot.
 * <p>
 * Instructions for implementing a Command: 1. Create a class that extends this one
 * <p>
 * <p>
 * There are also optional key-value pairs you can include in the JSON that will control the behavior of this command.
 * The following is a list of all the currently supported base Command flags that you can include in the JSON for any
 * Command, along with their DEFAULT values. If you do not include these lines, the default values listed here will be
 * used.
 * <p>
 * "includeInCommandsList": true - If you make this false, the command will not appear in the list of commands for the
 * CommandInitializer and users will have to know
 * <p>
 * "allowNoArgs": false - By default, any command that it sent by itself will be treated the same as `%[that-command]
 * help`, which means that the help argument is unnecessary. Setting this to true will allow the base argument to run
 * code rather than being redirected to the help panel. This is useful for functions or CallResponse commands with a
 * default code execution when you don't pass in any arguments
 */
public abstract class Command {
    private final String name;
    private final String description;
    private final String shortDescription;
    private final String link;
    private final String[] aliases;
    private final String[] typoAliases;
    private final boolean includeInCommandsList;
    // TODO make allowNoArgs work
    @SuppressWarnings("FieldCanBeLocal")
    private final boolean allowNoArgs;

    protected EmbedBuilder infoEmbed;

    protected final CommandManager manager;

    protected Command(@NotNull JsonObject json, @NotNull CommandManager manager) throws JsonParseException {
        this.manager = manager;

        // Required Command arguments (will throw an error if not in the JSON)
        this.name = JsonParser.getString(json, "name");
        this.description = JsonParser.getString(json, "description");

        // Optional Command arguments (can be omitted from the JSON)
        this.includeInCommandsList = JsonParser.getBoolean(json, "includeInCommandsList", true);
        this.allowNoArgs = JsonParser.getBoolean(json, "allowNoArgs", false);
        this.link = JsonParser.getString(json, "link", null);

        if (includeInCommandsList)
            // A short description is only required if the command is included in the command list
            this.shortDescription = JsonParser.getString(json, "shortDescription");
        else
            this.shortDescription = JsonParser.getString(json, "shortDescription", "");

        // All Command aliases are optional. typoAliases don't show up in the command help embed.
        this.aliases = JsonParser.getStringArray(json, "aliases");
        this.typoAliases = JsonParser.getStringArray(json, "typoAliases");

        infoEmbed = getInfo();
    }

    /**
     * Get the name of the command with preserved case. Use getNameLower() for lowercase command name. This is mandatory
     * argument in the JSON and will not be null.
     *
     * @return the name of the command
     */
    @NotNull
    public String getName() {
        return name;
    }

    /**
     * Get the name of the command in all lowercase. Use getName() to preserve the case in the JSON. This is mandatory
     * argument in the JSON and will not be null.
     *
     * @return the name of the command
     */
    @NotNull
    public String getNameLower() {
        return name.toLowerCase(Locale.ROOT);
    }

    /**
     * Get the description of the command that appears in the info embed that provides additional command help. This is
     * mandatory argument in the JSON and will not be null. For the short description included in
     *
     * @return the command description
     */
    @NotNull
    public String getDescription() {
        return description;
    }

    /**
     * Get the short description of this command that appears in the Commands list. If this command does not appear in
     * the commands list (check with doIncludeInCommandsList()) this might be null.
     *
     * @return the short description of the command
     */
    @NotNull
    public String getShortDescription() {
        return shortDescription;
    }

    /**
     * Get the link that goes in the info embed that provides additional help with this command. It is the webpage
     * target if you click on the name of the command at the top of the embed. This may be null if it wasn't set by the
     * user.
     *
     * @return the command link for the help embed
     */
    @NotNull
    public String getLink() {
        return link;
    }

    /**
     * Get an array with all the main aliases for this command. This does not include the typoAliases, but rather only
     * those that appear in the info embed that provides additional help with this command. This will never be null, but
     * the returned list might be empty.
     *
     * @return the command aliases in an array
     */
    @NotNull
    public String[] getAliases() {
        return aliases;
    }

    /**
     * Same as getAliases() except the result is in an ArrayList rather than an Array of Strings
     *
     * @return the command aliases in an array
     */
    public List<String> getAliasesArray() {
        return Arrays.asList(aliases);
    }

    /**
     * Check whether this command is listed in the commands list. All commands are listed by default, but if the
     * optional flag was included in the JSON this command will not be listed.
     *
     * @return the command aliases in an array
     */
    public boolean doIncludeInCommandsList() {
        return includeInCommandsList;
    }

    /**
     * Returns the name of this command followed by the "help" argument and preceded by the prefix. Note that it is not
     * enclosed in code block tick marks.
     *
     * @return the command a user must type to get help for this command
     */
    public String getHelpString() {
        return manager.getMainPrefix() + name.toLowerCase(Locale.ROOT) + " help";
    }

    /**
     * Gets the {@link CommandManager} that manages this Command instance. Useful for things like getting the default
     * prefix.
     *
     * @return the associated {@link CommandManager} instance
     */
    public CommandManager getManager() {
        return manager;
    }

    /**
     * Checks to see whether the given user message matches this command by comparing the given name against this
     * command's name and aliases. Also checks typo aliases for matches.
     * <p><br>
     * This method assumes the given args come from a message with a matching prefix and that the prefix has been
     * removed. It also assumes that the message was split into args with the regex "{@code \s+}".
     *
     * @param args the message a user sent in Discord with the prefix removed and split by spaces
     * @return true if the user's message matches this command and false otherwise
     */
    public final boolean matches(String[] args) {
        // Check to see if it matches the name of this command
        if (Checks.stringArrayStartsWith(args, getNameLower().split("\\s+")))
            return true;

        // If not, check the aliases
        for (String alias : aliases)
            if (Checks.stringArrayStartsWith(args, alias.split("\\s+")))
                return true;

        // If still no matches, check the typo aliases
        for (String alias : typoAliases)
            if (Checks.stringArrayStartsWith(args, alias.split("\\s+")))
                return true;

        // And return because there was no matches for this command whatsoever
        return false;
    }

    /**
     * Start by checking to see if the user indicated that they want to see the help panel for this command. If so send
     * it to them. Otherwise, run the command. If a valid {@link Method} was provided, execute that.
     * <p>
     * This method should deal with all exceptions and return them to the user where applicable. It should not throw any
     * exceptions (even runtime ones).
     *
     * @param strArgs the message the user sent separated by spaces. strArgs[0] is the name of the command
     * @param channel the channel the user sent their message in, which is where the reply should go
     * @param method  the optional method to execute which allows for custom code in another class to run
     */
    public abstract void process(@NotNull String[] strArgs, @NotNull MessageChannel channel, Method method);

    /**
     * Check to see if the user just wants the help embed for this command
     *
     * @param info         the info help embed retrieved with getInfo() for this command
     * @param strArgs      the command the user sent separated by spaces
     * @param channel      the channel to send a response in
     * @param argThreshold minimum number of arguments user must have sent (including command) to not get info panel
     * @return true if the user just wanted the info embed and it was sent to them; false otherwise
     */
    protected boolean checkInfoRequest(
            @NotNull EmbedBuilder info, @NotNull String[] strArgs, @NotNull MessageChannel channel, int argThreshold) {
        // If the user didn't meet the minimum argument threshold or their second argument was info/help send them info
        if (strArgs.length < argThreshold ||
                (strArgs.length > 1 &&
                        (strArgs[1].equalsIgnoreCase("help") ||
                                strArgs[1].equalsIgnoreCase("info") ||
                                strArgs[1].equalsIgnoreCase("information")))) {
            channel.sendMessage(info.build()).queue(
                    m -> m.delete().queueAfter(2, TimeUnit.MINUTES, s -> {
                    }, f -> {
                    }),
                    f -> {
                        System.out.println("Error sending help embed for: " + Arrays.toString(strArgs));
                        f.printStackTrace();
                    });
            return true;
        }
        return false;
    }

    /**
     * Combines the arguments with their descriptions and puts all that plus the command description and aliases in a
     * nice pretty {@link EmbedBuilder} using {@link MessageUtils}.
     *
     * @return a finished EmbedBuilder with all the info about this function
     * @throws IllegalArgumentException if there is an error assembling the {@link EmbedBuilder}, likely due to
     *                                  unresolvable URLs or exceeding character limits
     */
    protected @NotNull EmbedBuilder getInfo() {
        EmbedBuilder e = MessageUtils.makeEmbedBuilder(
                getName() + " Command Info",
                "",
                manager.getConfig().getCommandInfoColor(),
                EmbedField.of("Description", getDescription()),
                EmbedField.of("Alias" + (aliases.length > 1 ? "es" : ""),
                        GenericUtils.mergeList(getAliasesArray(), "and")));

        if (link != null)
            e.setTitle(getName() + " Info", link);
        return e;
    }

    /**
     * Convenience method to call {@link CommandManager#sendError(MessageChannel, ErrorBuilder)} from the {@link
     * CommandManager} associated with this {@link Command}.
     *
     * @param error   the error to send
     * @param channel the channel to send the error in
     */
    public final void sendError(@NotNull MessageChannel channel, @NotNull ErrorBuilder error) {
        manager.sendError(channel, error);
    }

    /**
     * Convenience method to call {@link CommandManager#sendError(MessageChannel, String)} from the {@link
     * CommandManager} associated with this {@link Command}.
     *
     * @param error   the error to send
     * @param channel the channel to send the error in
     */
    public final void sendError(@NotNull MessageChannel channel, @NotNull String error) {
        manager.sendError(channel, error);
    }

    /**
     * Convenience method to call {@link CommandManager#sendError(MessageChannel, ErrorBuilder)} from the {@link
     * CommandManager} associated with this {@link Command}.
     *
     * @param error   the error to send
     * @param channel the channel to send the error in
     * @param fields  any fields to add to the error, such as syntax
     */
    public final void sendError(@NotNull MessageChannel channel, @NotNull String error, @Nullable EmbedField... fields) {
        manager.sendError(channel, ErrorBuilder.of(error, manager.getConfig().getErrorColor()).addField(fields));
    }

    /**
     * Convince method for using {@link #respond(MessageBuilder, MessageChannel)} with an {@link Number}.
     *
     * @param output  the message to send
     * @param channel the channel to send it in
     */
    protected final void respond(Number output, MessageChannel channel) {
        respond(String.valueOf(output), channel);
    }

    /**
     * Convince method for using {@link #respond(MessageBuilder, MessageChannel)} with a {@link String}.
     *
     * @param output  the message to send
     * @param channel the channel to send it in
     */
    protected final void respond(String output, MessageChannel channel) {
        respond(new MessageBuilder(output), channel);
    }

    /**
     * Convince method for using {@link #respond(MessageBuilder, MessageChannel)} with an {@link EmbedBuilder}.
     *
     * @param output  the message to send
     * @param channel the channel to send it in
     */
    protected final void respond(EmbedBuilder output, MessageChannel channel) {
        respond(new MessageBuilder(output), channel);
    }

    /**
     * Send a response to the user in the specified channel. If there's an error, it is printed to the console and an
     * attempt is made to notify the user of the error in Discord.
     *
     * @param output  the message to send
     * @param channel the channel to send it in
     */
    protected final void respond(MessageBuilder output, MessageChannel channel) {
        channel.sendMessage(output.build()).queue(s -> {
                },
                f -> {
                    f.printStackTrace();
                    sendError(channel, "Encountered fatal exception. Try again later. Reason: " + f.toString());
                });
    }

    /**
     * Merge all the arguments from the start index onwards into a single String separated by spaces.
     *
     * @param strArgs the array of arguments given by the user
     * @param start   the first index to merge from
     * @return the merged string, or an empty string if there was an index out of bounds error
     */
    @NotNull
    public static String mergeArgs(String[] strArgs, int start) {
        StringBuilder s = new StringBuilder();
        for (int i = start; i < strArgs.length; i++)
            s.append(" ").append(strArgs[i]);

        if (s.length() == 0)
            return "";
        return s.substring(1);
    }
}