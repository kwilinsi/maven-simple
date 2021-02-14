package examples;

import botUtilities.commandsSystem.manager.CommandManager;
import botUtilities.commandsSystem.manager.CommandUtils;
import botUtilities.commandsSystem.manager.ManagerConfig;
import botUtilities.exceptions.ManagerBuildException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;

public class SimpleBot implements EventListener {
    public static JDA jda;
    public static CommandManager commandManager;

    public static void main(String[] args) throws
            LoginException, IOException, ManagerBuildException {

        jda = JDABuilder
                .createDefault("BOT TOKEN GOES HERE")
                .addEventListeners(new SimpleBot())
                .build();

        commandManager = CommandUtils
                .createDefaultManager(jda, new File("commands"), "All")
                .setConfigManager(ManagerConfig.of()
                        .setPrefixes("!", "+"))
                .build();
    }

    @Override
    public void onEvent(@NotNull GenericEvent event) {
        if (event instanceof Event)
            commandManager.run((Event) event);
    }
}