package com.s151044.discord.commands.interactions;

import com.s151044.discord.Embeds;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FindRoom implements SlashCommand {
    private List<Room> rooms;
    private final ButtonHandler handler;

    public FindRoom(List<Room> rooms, ButtonHandler handler) {
        this.rooms = rooms;
        this.handler = handler;
    }

    @Override
    public void action(SlashCommandInteractionEvent evt) {
        //evt.deferReply().queue();
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
        List<String> embed = new ArrayList<>();
        StringBuilder builder = new StringBuilder();
        int index = 0;
        for (Room r : matched) {
            if (index != 0 && index % 10 == 0) {
                embed.add(builder.toString());
                builder = new StringBuilder();
            }
            builder.append(r.toString());
            builder.append("\n");
            index++;
        }
        embed.add(builder.toString());
        PaginateMenu menu = new PaginateMenu(embed,
                "Rooms available for " + lookupDate + " at " + time.truncatedTo(ChronoUnit.SECONDS) + ":", evt);
        handler.addCommand(menu);
        menu.showMenu();

        /*hook.sendMessageEmbeds(Embeds.getLongEmbed(
                matched.stream().map(Room::toString).collect(Collectors.joining("\n")),
                "Rooms available for " + lookupDate + " at " + time.truncatedTo(ChronoUnit.SECONDS) + ":")).queue();*/
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
                    rooms.stream().filter(r -> r.getLocation().startsWith(prefix)).map(Room::getLocation)
                            .limit(25)
                            .collect(Collectors.toList())).queue();
            case "weekday" -> evt.replyChoiceStrings(
                    TimeRecord.getWeekdays().keySet()
                            .stream().filter(s -> s.startsWith(prefix)).limit(25)
                            .collect(Collectors.toList())).queue();
            default -> {}
        }
    }
}
