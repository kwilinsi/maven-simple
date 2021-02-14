package com.github.kwilinsi.jda.command.manager.commandsSystem.types.callResponse;

import com.github.kwilinsi.jda.command.manager.commandsSystem.manager.CommandManager;
import com.github.kwilinsi.jda.command.manager.exceptions.JsonParseException;
import com.github.kwilinsi.jda.command.manager.commandsSystem.json.JsonParser;
import com.github.kwilinsi.jda.command.manager.commandsSystem.types.Command;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * This is a less intensive Command extension than Functions. It doesn't allow for arguments, but simply sends a
 * pre-programmed response when the user sends a certain command.
 * <p>
 * When creating EmbedBuilders in the json, the following parameters are required: type, title, description, color. You
 * can also add footerText, footerImg (a url), and fields (which is a json array containing objects that each must have
 * a `title`, `text`, and `inline` parameter.)
 */
public class CallResponse extends Command {
    private final Response[] responses;
    private final String defaultResponseKey;

    //                            |
    // TODO implement this stuff \|/
    private final String[] replaceKeys = {
            "PREFIX"};
    private final String[] replaceValues = {
            super.manager.getMainPrefix()};

    public CallResponse(@NotNull JsonObject json, @NotNull CommandManager manager) throws JsonParseException {
        super(json, manager);

        JsonObject[] responseObjects = JsonParser.getJsonObjectArray(json, "responses");
        this.responses = new Response[responseObjects.length];
        for (int i = 0; i < this.responses.length; i++)
            this.responses[i] = Response.of(responseObjects[i]);

        this.defaultResponseKey = JsonParser
                .getString(json, "defaultResponseKey", responses.length == 0 ? "" : responses[0].getMainKey());
    }

    public void process(@NotNull String[] strArgs, @NotNull MessageChannel channel, Method method) {
        String key;

        if (strArgs.length == 1)
            key = defaultResponseKey;
        else
            key = mergeArgs(strArgs, 1);

        for (Response response : responses)
            if (response.matches(key)) {
                respond(response.getMessage(), channel);
                return;
            }

        // If no response was sent it means a matching key wasn't found
        sendError(
                channel,
                "Error loading response (unknown term). Try `" + getHelpString() + "` for more information.");
    }

}
