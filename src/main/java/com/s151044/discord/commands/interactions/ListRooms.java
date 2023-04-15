package com.s151044.discord.commands.interactions;

import com.s151044.discord.Embeds;
import com.s151044.discord.room.Room;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
        StringBuilder sb = new StringBuilder();
        for (Room r : rooms) {
            sb.append(r.getName()).append("\n");
        }
        evt.replyEmbeds(Embeds.getLongEmbed(sb.toString(), "Rooms supported:")).queue();
    }

    @Override
    public String callName() {
        return "list-rooms";
    }

    @Override
    public SlashCommandData commandInfo() {
        return Commands.slash("list-rooms", "Lists all rooms supported by the bot.");
    }
}
