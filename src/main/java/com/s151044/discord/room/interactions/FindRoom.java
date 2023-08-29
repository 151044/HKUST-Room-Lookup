package com.s151044.discord.room.interactions;

import com.s151044.discord.commands.interactions.SlashCommand;
import com.s151044.discord.commands.interactions.buttons.PaginateMenu;
import com.s151044.discord.handlers.interactions.ButtonHandler;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

public class FindRoom implements SlashCommand {
    private final List<Room> rooms;
    private final ButtonHandler handler;

    public FindRoom(List<Room> rooms, ButtonHandler handler) {
        this.rooms = rooms;
        this.handler = handler;
    }

    @Override
    public void action(SlashCommandInteractionEvent evt) {
        evt.deferReply().queue();
        InteractionHook hook = evt.getHook();
        List<Room> list = rooms;
        OptionMapping areaMapping = evt.getOption("area");
        OptionMapping weekdayMapping = evt.getOption("weekday");
        OptionMapping timeMapping = evt.getOption("time");
        if (areaMapping != null) {
            list = list.stream().filter(s -> s.getLocation().equals(areaMapping.getAsString()))
                    .collect(Collectors.toList());
        }
        LocalDate lookupDate = LocalDate.now();
        if (weekdayMapping != null) {
            DayOfWeek week = TimeRecord.getWeekday(weekdayMapping.getAsString());
            if (week == null) {
                hook.sendMessage("Unable to find weekday " + weekdayMapping.getAsString() + ".").queue();
                return;
            }
            lookupDate = lookupDate.with(TemporalAdjusters.nextOrSame(week));
        }
        LocalTime time = LocalTime.now();
        if (timeMapping != null) {
            String timeString = timeMapping.getAsString();
            String[] arr = timeString.split(":");
            if (arr.length < 2) {
                hook.sendMessage("Bad time format.").queue();
                return;
            }
            for (String s : arr) {
                if (!s.chars().allMatch(Character::isDigit)) {
                    hook.sendMessage("Bad time format.").queue();
                    return;
                }
            }
            int hour = Integer.parseInt(arr[0]);
            int minutes = Integer.parseInt(arr[1]);
            time = LocalTime.of(hour, minutes);
        }
        LocalDateTime dateTime = LocalDateTime.of(lookupDate, time);
        List<Room> matched = list.stream().filter(r -> !r.isBlockedAt(dateTime)).toList();
        List<String> embed = PaginateMenu.splitEntries(matched, 10, Room::toString);
        PaginateMenu menu = new PaginateMenu(embed,
                "Rooms available" +
                        (areaMapping != null ? " near " + areaMapping.getAsString() : "")
                        + " for " + lookupDate + " at "
                        + time.truncatedTo(ChronoUnit.SECONDS) + ":", hook);
        handler.addCommand(menu);
        menu.showMenu();
    }

    @Override
    public String callName() {
        return "find-room";
    }

    @Override
    public SlashCommandData commandInfo() {
        return Commands.slash("find-room", "Finds all the empty rooms at a specified time.")
                .addOption(OptionType.STRING, "area", "The area to search within.", false, true)
                .addOption(OptionType.STRING, "weekday", "Weekday to find this information for.", false, true)
                .addOption(OptionType.STRING, "time", "Time to find this information for, in 24h HH:MM.", false, false);
    }

    @Override
    public void handleAutocomplete(CommandAutoCompleteInteractionEvent evt) {
        String name = evt.getFocusedOption().getName();
        String prefix = evt.getFocusedOption().getValue();
        switch (name) {
            case "area" -> evt.replyChoiceStrings(
                    rooms.stream().map(Room::getLocation).filter(location -> location.startsWith(prefix))
                            .filter(s -> !s.isEmpty())
                            .limit(25)
                            .collect(Collectors.toList())).queue();
            case "weekday" -> evt.replyChoiceStrings(
                    TimeRecord.getWeekdays().keySet()
                            .stream().filter(s -> s.startsWith(prefix))
                            .filter(s -> !s.isEmpty())
                            .limit(25)
                            .collect(Collectors.toList())).queue();
            default -> {}
        }
    }
}
