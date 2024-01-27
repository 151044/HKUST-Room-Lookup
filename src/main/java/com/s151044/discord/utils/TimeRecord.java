package com.s151044.discord.utils;

import org.jetbrains.annotations.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

/**
 * Class representing a "time record" for a specific period of time.
 * Can be scheduled weekly, or within a specific period of time.
 * <p>
 * Note: Do NOT compare TimeRecords with .equals().
 * It is deliberately not overridden in order to preserve correct behaviour in Room.
 * i.e. Each TimeRecord is unique even if they represent the same time.
 */
public class TimeRecord implements Comparable<TimeRecord> {
    private static final LocalDate NONE = LocalDate.of(1000, 1, 1);
    private static final Map<String, DayOfWeek> FULL_DATES = new HashMap<>();
    private LocalDate beginDate = NONE; // can be empty, i.e. default value
    private LocalDate endDate = NONE;// can be empty, i.e. default value
    private final List<DayOfWeek> days;
    private LocalTime beginTime;
    private LocalTime endTime;

    static {
        initDays();
    }

    /**
     * Initializes a TimeRecord with the specified starting and ending dates.
     * Does not handle 12 AM as there is no such record in the website.
     * Both dates are inclusive.
     * @param beginDate The beginning date, in DD-MMM-YYYY
     * @param endDate The ending date, in DD-MMM-YYYY
     * @param timeString The recurring time, in the [Weekdays] BeginTime-EndTime format
     *                   Note that [Weekdays] can be multiple days, i.e. [MoTuWe], or a single day [Fr].
     *                   Times are in HH:MM{A, P}M.
     */
    public TimeRecord(String beginDate, String endDate, String timeString) {
        this(timeString);
        this.beginDate = toLocalDate(beginDate);
        this.endDate = toLocalDate(endDate);
    }

    /**
     * Initializes a TimeRecord with no starting or ending dates.
     * Does not handle 12 AM as there is no such record in the website.
     * @param timeString The recurring time, in the [Weekdays] BeginTime - EndTime format
     *                   Note that [Weekdays] can be multiple days, i.e. [MoTuWe], or a single day [Fr].
     *                   Times are in HH:MM{A, P}M.
     */
    public TimeRecord(String timeString) {
        String[] array = timeString.split(" ");
        days = getDays(array[0]);
        // I *assume* that there will be no 12AM classes...
        // If not, please PR
        String starting = array[1];
        beginTime = LocalTime.of(Integer.parseInt(starting.substring(0, 2)), Integer.parseInt(starting.substring(3, 5)));
        if (starting.endsWith("PM") && !starting.startsWith("12")) {
            beginTime = beginTime.plusHours(12);
        }
        String ending = array[3];
        endTime = LocalTime.of(Integer.parseInt(ending.substring(0, 2)), Integer.parseInt(ending.substring(3, 5)));
        if (ending.endsWith("PM") && !ending.startsWith("12")) {
            endTime = endTime.plusHours(12);
        }
    }

    /**
     * Checks if the given LocalDateTime is "within" this time record.
     * Returns true if the LocalDateTime lies between the beginning and end dates, and is on the dates specified in the
     * recurring time.
     * @param moment The date and time to compare against
     * @return True if the LocalDateTime is within this time record; false otherwise
     */
    public boolean in(LocalDateTime moment) {
        LocalDate localDate = moment.toLocalDate();
        if (!beginDate.equals(NONE)) {
            if (!beginDate.isBefore(localDate) || !endDate.isAfter(localDate)) {
                return false;
            }
        }
        if (!days.contains(moment.getDayOfWeek())) {
            return false;
        }
        LocalTime localTime = moment.toLocalTime();
        return beginTime.isBefore(localTime) && endTime.isAfter(localTime);
    }

    /**
     * Checks if the given LocalDate is "within" this time record.
     * Returns true if the LocalDate lies between the beginning and end dates, and is on the dates specified in the
     * recurring time.
     * @param date The date and time to compare against
     * @return True if the LocalDate is within this time record; false otherwise
     */
    public boolean in(LocalDate date) {
        if (!beginDate.equals(NONE)) {
            if (!beginDate.isBefore(date) || !endDate.isAfter(date)) {
                return false;
            }
        }
        return days.contains(date.getDayOfWeek());
    }

    public List<DayOfWeek> getDays() {
        return new ArrayList<>(days);
    }

    private static LocalDate toLocalDate(String time) {
        String[] array = time.split("-");
        return LocalDate.of(Integer.parseInt(array[2]), toMonths(array[1]), Integer.parseInt(array[0]));
    }

    private static int toMonths(String str) {
        // ...
        return switch (str) {
            case "JAN" -> 1;
            case "FEB" -> 2;
            case "MAR" -> 3;
            case "APR" -> 4;
            case "MAY" -> 5;
            case "JUN" -> 6;
            case "JUL" -> 7;
            case "AUG" -> 8;
            case "SEP" -> 9;
            case "OCT" -> 10;
            case "NOV" -> 11;
            case "DEC" -> 12;
            default -> throw new IllegalStateException("Unexpected value: " + str);
        };
    }

    private static List<DayOfWeek> getDays(String input) {
        // ...
        List<DayOfWeek> weeks = new ArrayList<>();
        int index = 0;
        input = input.trim();
        while (index < input.length()) {
            weeks.add(switch(input.substring(index, index + 2)) {
                case "Mo" -> DayOfWeek.MONDAY;
                case "Tu" -> DayOfWeek.TUESDAY;
                case "We" -> DayOfWeek.WEDNESDAY;
                case "Th" -> DayOfWeek.THURSDAY;
                case "Fr" -> DayOfWeek.FRIDAY;
                case "Sa" -> DayOfWeek.SATURDAY;
                case "Su" -> DayOfWeek.SUNDAY;
                default -> throw new IllegalStateException("Unexpected value: " + input.substring(index, index + 2));
            });
            index += 2;
        }
        return weeks;
    }

    private static void initDays() {
        FULL_DATES.put("Monday", DayOfWeek.MONDAY);
        FULL_DATES.put("Tuesday", DayOfWeek.TUESDAY);
        FULL_DATES.put("Wednesday", DayOfWeek.WEDNESDAY);
        FULL_DATES.put("Thursday", DayOfWeek.THURSDAY);
        FULL_DATES.put("Friday", DayOfWeek.FRIDAY);
        FULL_DATES.put("Saturday", DayOfWeek.SATURDAY);
        FULL_DATES.put("Sunday", DayOfWeek.SUNDAY);
    }

    public String getTimes() {
        return beginTime + " - " + endTime;
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!beginDate.equals(NONE)) {
            sb.append(beginDate).append(" - ").append(endDate).append(" ");
        }
        days.forEach(day -> sb.append(day.toString().charAt(0)).append(Character.toLowerCase(day.toString().charAt(1))));
        sb.append(" ");
        sb.append(beginTime).append(" - ").append(endTime);
        return sb.toString();
    }

    @Override
    public int compareTo(@NotNull TimeRecord timeRecord) {
        // Note: this compareTo only takes into account the time, not the date
        return Comparator.comparing((TimeRecord rec) -> rec.beginTime).thenComparing((TimeRecord rec) -> rec.endTime)
                .compare(this, timeRecord);
    }

    public static DayOfWeek getWeekday(String longName) {
        return FULL_DATES.get(longName);
    }

    public static Map<String, DayOfWeek> getWeekdays() {
        return new HashMap<>(FULL_DATES);
    }
}
