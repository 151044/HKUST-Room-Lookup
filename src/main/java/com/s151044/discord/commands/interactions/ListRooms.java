package com.s151044.discord.commands.interactions;

import com.s151044.discord.Embeds;
import com.s151044.discord.room.Room;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public class ListRooms implements SlashCommand {
    private final List<Room> rooms;

    public ListRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
    @Override
    public void action(SlashCommandInteractionEvent evt) {
        if (evt.getOption("area") != null) {
            String area = evt.getOption("area").getAsString();
            List<String> toSend = rooms.stream()
                    .filter(location -> location.getLocation().equals(area))
                    .map(Room::getName).toList();
            if (toSend.isEmpty()) {
                evt.reply("Cannot find supported rooms for " + area + "!").queue();
            } else {
                evt.replyEmbeds(Embeds.getLongEmbed(String.join("\n", toSend), "Rooms found near " + area + ":")).queue();
            }
        } else {
            StringBuilder sb = new StringBuilder();
            for (Room r : rooms) {
                sb.append(r.getName()).append("\n");
            }
            evt.replyEmbeds(Embeds.getLongEmbed(sb.toString(), "Rooms supported:")).queue();
        }
    }

    @Override
    public String callName() {
        return "list-rooms";
    }

    @Override
    public SlashCommandData commandInfo() {
        return Commands.slash("list-rooms", "Lists all rooms supported by the bot.")
                .addOption(OptionType.STRING, "area", "Lift number, or general area to search for"
                , false, true);
    }

    @Override
    public void handleAutocomplete(CommandAutoCompleteInteractionEvent evt) {
        String name = evt.getFocusedOption().getName();
        String prefix = evt.getFocusedOption().getValue();
        if (name.equals("area")) {
            evt.replyChoiceStrings(
                    rooms.stream().map(Room::getLocation).filter(location -> location.startsWith(prefix) && !location.isEmpty())
                    .limit(25).toList()).queue();
        }
    }
}
