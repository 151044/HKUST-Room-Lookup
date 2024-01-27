package com.s151044.discord.room.interactions;

import com.s151044.discord.commands.interactions.SlashCommand;
import com.s151044.discord.commands.interactions.buttons.PaginateMenu;
import com.s151044.discord.handlers.interactions.ButtonHandler;
import com.s151044.discord.room.Room;
import com.s151044.discord.utils.TimeRecord;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class RoomTimetable implements SlashCommand {
    private final List<Room> rooms;
    private final ButtonHandler handler;

    public RoomTimetable(List<Room> rooms, ButtonHandler handler) {
        this.rooms = rooms;
        this.handler = handler;
    }
    @Override
    public void action(SlashCommandInteractionEvent evt) {
        InteractionHook hook = evt.getHook();
        evt.deferReply().queue();
        String roomStr = evt.getOption("room").getAsString();
        Optional<Room> roomOpt = rooms.stream().filter(r -> r.getName().equals(roomStr)).findFirst();
        if (roomOpt.isEmpty()) {
            hook.sendMessage("Cannot find room!").queue();
            return;
        }
        Room room = roomOpt.get();
        OptionMapping weekdayOpt = evt.getOption("weekday");
        Map<String, String> toOutput;
        if (weekdayOpt != null) {
            DayOfWeek week = TimeRecord.getWeekdays().getOrDefault(weekdayOpt.getAsString(), null);
            if (week == null) {
                hook.sendMessage("Cannot find weekday!").queue();
                return;
            }
            LocalDate date = LocalDate.now().with(TemporalAdjusters.nextOrSame(week));
            toOutput = Room.prettyFormat(room, date);
        } else {
            toOutput = Room.prettyFormat(room);
        }
        List<String> keys = new ArrayList<>();
        List<String> vals = new ArrayList<>();
        for (Map.Entry<String, String> entry : toOutput.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            keys.add(roomStr + " occupation for " + entry.getKey());
            vals.add(entry.getValue());
        }
        //vals = PaginateMenu.splitEntries(vals, 10, Function.identity());
        PaginateMenu menu = new PaginateMenu(vals, keys, hook);
        menu.showMenu();
        handler.addCommand(menu);
    }

    @Override
    public String callName() {
        return "room-tt";
    }

    @Override
    public SlashCommandData commandInfo() {
        return Commands.slash("room-tt", "Finds the timetable for this room.")
                .addOption(OptionType.STRING, "room", "The room to find.", true, true)
                .addOption(OptionType.STRING, "weekday", "Day of the week to search for.", false, true);
    }

    @Override
    public void handleAutocomplete(CommandAutoCompleteInteractionEvent evt) {
        switch (evt.getFocusedOption().getName()) {
            case "room" -> {
                String prefix = evt.getOption("room").getAsString();
                evt.replyChoiceStrings(rooms.stream()
                        .map(Room::getName)
                        .filter(name -> name.startsWith(prefix)).limit(25).collect(Collectors.toList())).queue();
            }
            case "weekday" -> {
                String day = evt.getOption("weekday").getAsString();
                evt.replyChoiceStrings(TimeRecord.getWeekdays().keySet().stream().filter(k -> k.startsWith(day)).collect(Collectors.toList()))
                        .queue();
            }
            default -> evt.replyChoices(List.of()).queue();
        }
    }
}
