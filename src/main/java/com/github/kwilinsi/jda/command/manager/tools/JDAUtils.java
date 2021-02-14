package com.github.kwilinsi.jda.command.manager.tools;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JDAUtils {
    /**
     * Wraps the input string in {@code code block markdown} and applies syntax highlighting for the given language.
     * <p><br>
     * <u>Example:<br></u>
     * Input: {@code hello world}<br> Output: {@code ```\nhello world```}
     *
     * @param input    the input string to transform into code
     * @param language the language to mark the code block as for proper syntax highlighting in Discord
     * @return the final code block
     */
    public static String codeBlock(@NotNull String input, @NotNull String language) {
        return "```" + language + "\n" + input + "```";
    }

    /**
     * Convenience method for {@link #codeBlock(String, String)} that doesn't require declaring a language. Wraps the
     * input string in {@code ```code block markdown```}.
     *
     * @param input the input string to transform into code
     * @return the final code block
     */
    public static String codeBlock(@NotNull String input) {
        return codeBlock(input, "");
    }

    /**
     * This adds an emoji reaction to a message in Discord.
     * <p><br>
     * If the emoji being added is a custom Discord emoji from a server, it must be in the following format:
     * <p>{@code :[emoji]:[id]}
     * <p>For example:<p>{@code :arrow:123456789012345678}
     * <p><br>
     * If the desired reaction is a default Discord emoji, you <i>must</i> use actual unicode. Some unicode characters
     * with Discord emoji names can be found in the {@link Emojis} class, but for the most part you'll need to get the
     * unicode characters elsewhere.
     *
     * @param message the message
     * @param emoji   the reaction to add
     */
    public static void react(@NotNull Message message, @NotNull String emoji) {
        message.addReaction(emoji).queue();
    }

    /**
     * Reads the bot token from the give file path and returns it as a {@link String}. Make sure that the only thing in
     * the file is the bot's token. No line breaks or anything else.
     *
     * @param path the path to the file with the token
     * @return the token
     * @throws IOException if there's an error reading the file
     */
    public static @NotNull String getBotToken(@NotNull Path path) throws IOException {
        return Files.readString(Path.of("bot.token"));
    }

    /**
     * Removes the {@link MessageReaction} from {@link User}.
     *
     * @param reaction    the reaction to remove
     * @param user        the user who added that reaction
     * @param allowErrors true if errors should be thrown if this fails; false otherwise
     */
    public static void removeReaction(@NotNull MessageReaction reaction, @NotNull User user, boolean allowErrors) {
        reaction.removeReaction(user).queue(s -> {
        }, f -> {
            if (allowErrors)
                f.printStackTrace();
        });
    }
}
