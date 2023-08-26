package com.s151044.discord.commands;

import com.s151044.discord.utils.Messages;
import com.s151044.discord.commands.interactions.SlashCommand;
import com.s151044.discord.commands.interactions.SlashCommandList;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SetupInteractions implements Command {
    private static final List<String> guildIds = new ArrayList<>();
    private final SlashCommandList slashList;

    public SetupInteractions(SlashCommandList slashList) {
        this.slashList = slashList;
    }

    @Override
    public void action(MessageReceivedEvent evt, String callName, String arguments) {
        if(!evt.getChannelType().isGuild()){
            Messages.send(evt, "This is not a guild channel!");
            return;
        }
        if(guildIds.contains(evt.getGuild().getId())){
            Messages.send(evt, "Interactions have been set up in this server already!");
            return;
        }
        GuildMessageChannel channel = evt.getGuildChannel();
        channel.getGuild().updateCommands()
                .addCommands(slashList.getCommands().stream().map(SlashCommand::commandInfo).collect(Collectors.toList()))
                        .queue();
        guildIds.add(evt.getGuild().getId());
        Messages.send(evt, "Done!");
    }

    @Override
    public List<String> alias() {
        return List.of("setslash", "setupslash");
    }

    @Override
    public String callName() {
        return "SetupInteractions";
    }

    @Override
    public String shortHelp() {
        return "Sets up slash commands in this server.";
    }

    @Override
    public String longHelp() {
        return "Sets up slash commands in this server.";
    }

    @Override
    public boolean hidden() {
        return true;
    }
}
