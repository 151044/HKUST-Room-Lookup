package com.s151044.discord.room;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class representing a room in HKUST.
 * Contains information about name, location, capacity, and running classes.
 */
public class Room {
    private final String name;
    private String location = ""; // Acad Concourse, etc. Can be empty (for cases such as LTD).
    private int capacity = -1; // Some rooms, like labs, may not have a capacity
    private final Map<TimeRecord, CourseSection> occupiedCourses = new HashMap<>();
    public Room(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
    }
    public Room(String name, String location) {
        this.name = name;
        this.location = location;
    }
    public Room(String name, String location, int capacity) {
        this.name = name;
        this.location = location;
        this.capacity = capacity;
    }

    public void addRecord(TimeRecord record, CourseSection c) {
        occupiedCourses.put(record, c);
    }

    public List<Map.Entry<TimeRecord, CourseSection>> blockingTimes(LocalDateTime time) {
        return occupiedCourses.entrySet().stream().filter(e -> !e.getKey().in(time))
                .collect(Collectors.toList());
    }
    public Set<Map.Entry<TimeRecord, CourseSection>> timetable(LocalDate day) {
        return occupiedCourses.entrySet().stream().filter(e -> e.getKey().in(day))
                .collect(Collectors.toSet());
    }

    public Set<Map.Entry<TimeRecord, CourseSection>> timetable() {
        return occupiedCourses.entrySet();
    }

    public Set<DayOfWeek> occupiedDays() {
        return occupiedCourses.keySet().stream().map(TimeRecord::getDays).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public boolean isBlockedAt(LocalDateTime dateTime) {
        return occupiedCourses.keySet().stream().anyMatch(record -> record.in(dateTime));
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(name, room.name) && Objects.equals(location, room.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(name);
        if (!location.equals("")) {
            sb.append(", ").append(location);
        }
        if (capacity != -1) {
            sb.append(" (").append(capacity).append(")");
        }
        return sb.toString();
    }

    public static Map<String, String> prettyFormat(Room room) {
        Map<String, String> timetables = new HashMap<>();
        LocalDate day = LocalDate.now();
        LocalDate adj;
        for (DayOfWeek week : room.occupiedDays()) {
            adj = day.with(TemporalAdjusters.nextOrSame(week));
            Set<Map.Entry<TimeRecord, CourseSection>> timetable = room.timetable(adj);
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<TimeRecord, CourseSection> sec : timetable.stream()
                    .sorted(Map.Entry.comparingByKey()).toList()) {
                sb.append("**").append(sec.getKey().getTimes()).append("**: ").append(sec.getValue())
                        .append("\n");
            }
            timetables.put(adj + " (" + adj.getDayOfWeek()
                    .getDisplayName(TextStyle.FULL, Locale.getDefault())+ ")", sb.toString());
        }
        return timetables;
    }
    public static Map<String, String> prettyFormat(Room room, LocalDate week) {
        Map<String, String> timetables = new HashMap<>();
        Set<Map.Entry<TimeRecord, CourseSection>> timetable = room.timetable(week);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<TimeRecord, CourseSection> sec : timetable.stream()
                .sorted(Map.Entry.comparingByKey()).toList()) {
            sb.append("**").append(sec.getKey().getTimes()).append("**: ").append(sec.getValue())
                    .append("\n");
        }
        timetables.put(week.toString() + " (" + week.getDayOfWeek()
                .getDisplayName(TextStyle.FULL, Locale.getDefault()) + ")", sb.toString());
        return timetables;
    }
}
