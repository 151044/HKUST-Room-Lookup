package com.s151044.discord.handlers;

import com.s151044.discord.EmbedHelper;
import com.s151044.discord.Messages;
import com.s151044.discord.commands.Command;
import com.s151044.discord.commands.CommandList;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.Random;

/**
 * A handler for guild messages.
 */
public class MessageHandler extends ListenerAdapter {
    private String prefix;
    private CommandList list;
    private List<String> funnyHelpMessages = List.of("No help is forthcoming.", "Helping yourself is the first step.",
            "I'm also in need of assistance.", "Help! I'm drowning!", "Did you mean: Hell?");
    private Random rand = new Random();

    /**
     * Constructs a new MessageHandler with the specified prefix to check for and the specified command list.
     * @param prefix The prefix to check messages for commands
     * @param list The command list to use
     */
    public MessageHandler(String prefix, CommandList list){
        this.prefix = prefix;
        this.list = list;
    }


    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String msg = event.getMessage().getContentRaw();
        if(event.getAuthor().isBot()){
            return;
        }
        if(!msg.startsWith(prefix)){
            return;
        }
        String first = msg.substring(1, !msg.contains("\n") ? msg.length() : msg.indexOf("\n")).split(" ")[0];
        String args;
        if(msg.length() < first.length() + 2){
            args = "";
        } else {
            args = msg.substring(first.length() + 2);
        }
        if(first.equals("help")){
            handleHelp(event, args);
            return;
        }
        Optional<Command> get = list.tryGet(first);
        if(get.isEmpty()){
            //Messages.sendMessage(event,"Cannot find this command! Sorry!");
            return;
        }
        get.get().action(event, first, args);
    }

    private void handleHelp(@NotNull MessageReceivedEvent event, String args) {
        if(args.equals("")){
            StringBuilder build = new StringBuilder();
            for(Command c: list.getCommands()){
                if(!c.hidden()) {
                    build.append(c.callName());
                    build.append(" -- ");
                    build.append(c.shortHelp());
                    build.append("\n");
                }
            }
            Messages.send(event, EmbedHelper.getEmbed(build.toString(), "Help"));
        } else if(args.equals("help")){
            Messages.send(event, funnyHelpMessages.get(rand.nextInt(funnyHelpMessages.size())));
        } else {
            Optional<Command> command = list.tryGet(args);
            if(command.isPresent() && !command.get().hidden()){
                Messages.send(event, EmbedHelper.getEmbed(formatHelp(command.get()), command.get().callName()));
            } else {
                Messages.send(event, "Command not found!");
            }
        }
    }

    private String formatHelp(Command command){
        StringBuilder build = new StringBuilder();
        build.append("**Description:**\n");
        build.append(command.longHelp()).append("\n");
        build.append("**Aliases:**\n");
        if(!command.alias().isEmpty()) {
            build.append(String.join(", ", command.alias())).append("\n");
        } else {
            build.append("None\n");
        }
        return build.toString();
    }
}

