package com.s151044.discord.handlers.interactions;

import com.s151044.discord.commands.interactions.SlashCommand;
import com.s151044.discord.commands.interactions.SlashCommandList;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SlashHandler extends ListenerAdapter {

    private final SlashCommandList list;

    public SlashHandler(SlashCommandList list){
        this.list = list;
    }
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Optional<SlashCommand> get = list.tryGet(event.getName());
        if(get.isEmpty()){
            //Messages.sendMessage(event,"Cannot find this command! Sorry!");
            return;
        }
        get.get().action(event);
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        Optional<SlashCommand> get = list.tryGet(event.getName());
        if(get.isEmpty()){
            return;
        }
        get.get().handleAutocomplete(event);
    }
}
