package com.s151044.discord.commands.interactions;

import com.s151044.discord.Embeds;
import com.s151044.discord.room.CourseSection;
import com.s151044.discord.room.Room;
import com.s151044.discord.room.TimeRecord;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

// TODO: Try out buttons later
public class RoomTimetable implements SlashCommand {
    private final Map<String, DayOfWeek> weeks = new HashMap<>();
    private final List<Room> rooms;

    public RoomTimetable(List<Room> rooms) {
        this.rooms = rooms;
        // more nonsense
        weeks.put("Monday", DayOfWeek.MONDAY);
        weeks.put("Tuesday", DayOfWeek.TUESDAY);
        weeks.put("Wednesday", DayOfWeek.WEDNESDAY);
        weeks.put("Thursday", DayOfWeek.THURSDAY);
        weeks.put("Friday", DayOfWeek.FRIDAY);
        weeks.put("Saturday", DayOfWeek.SATURDAY);
        weeks.put("Sunday", DayOfWeek.SUNDAY);
    }
    @Override
    public void action(SlashCommandInteractionEvent evt) {
        InteractionHook hook = evt.getHook();
        evt.deferReply().queue();
        String roomStr = evt.getOption("room").getAsString();
        Optional<Room> roomOpt = rooms.stream().filter(r -> r.getName().equals(roomStr)).findFirst();
        if (roomOpt.isEmpty()) {
            hook.sendMessage("Cannot find room with name " + roomStr + "!").queue();
            return;
        }
        Room room = roomOpt.get();
        OptionMapping weekdayOpt = evt.getOption("weekday");
        Map<String, String> toOutput;
        if (weekdayOpt != null) {
            DayOfWeek week = weeks.getOrDefault(weekdayOpt.getAsString(), null);
            if (week == null) {
                hook.sendMessage("Cannot find weekday " + weekdayOpt).queue();
                return;
            }
            LocalDate date = LocalDate.now().with(TemporalAdjusters.next(week));
            toOutput = prettyFormat(room, date);
        } else {
            toOutput = prettyFormat(room);
        }
        for (Map.Entry<String, String> entry : toOutput.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            hook.sendMessageEmbeds(Embeds.getEmbed(entry.getValue(), "Room Occupation for " + entry.getKey()))
                    .queue();
        }
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
                evt.replyChoiceStrings(weeks.keySet().stream().filter(k -> k.startsWith(day)).collect(Collectors.toList()))
                        .queue();
            }
            default -> evt.replyChoices(List.of()).queue();
        }
    }

    private static Map<String, String> prettyFormat(Room room) {
        Map<String, String> timetables = new HashMap<>();
        LocalDate day = LocalDate.now();
        LocalDate adj;
        for (DayOfWeek week : room.occupiedDays()) {
            adj = day.with(TemporalAdjusters.nextOrSame(week));
            Set<Map.Entry<TimeRecord, CourseSection>> timetable = room.timetable(adj);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<TimeRecord, CourseSection> sec : timetable.stream()
                    .sorted(Map.Entry.comparingByKey()).toList()) {
                sb.append("**").append(sec.getKey()).append("**: ").append(sec.getValue())
                        .append("\n");
            }
            timetables.put(adj + " (" + adj.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.getDefault())+ ")", sb.toString());
        }
        return timetables;
    }
    private static Map<String, String> prettyFormat(Room room, LocalDate week) {
        Map<String, String> timetables = new HashMap<>();
        Set<Map.Entry<TimeRecord, CourseSection>> timetable = room.timetable(week);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TimeRecord, CourseSection> sec : timetable.stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            sb.append("**").append(sec.getKey()).append("**: ").append(sec.getValue())
                    .append("\n");
        }
        timetables.put(week.toString() + " (" + week.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.getDefault()) + ")", sb.toString());
        return timetables;
    }
}
