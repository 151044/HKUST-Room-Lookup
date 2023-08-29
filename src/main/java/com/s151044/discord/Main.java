package com.s151044.discord;

import com.s151044.discord.commands.CommandList;
import com.s151044.discord.commands.SetupInteractions;
import com.s151044.discord.room.interactions.FindRoom;
import com.s151044.discord.room.interactions.ListRooms;
import com.s151044.discord.room.interactions.RoomTimetable;
import com.s151044.discord.commands.interactions.SlashCommandList;
import com.s151044.discord.handlers.MessageHandler;
import com.s151044.discord.handlers.interactions.ButtonHandler;
import com.s151044.discord.handlers.interactions.SlashHandler;
import com.s151044.discord.room.CourseSection;
import com.s151044.discord.room.Room;
import com.s151044.discord.room.TimeRecord;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

public class Main {
    private static JDA jda;
    public static void main(String[] args) throws IOException, InterruptedException {
        List<String> arguments = List.of(args);
        if (arguments.contains("--fetch")) {
            fetchCourses();
        } else {
            System.out.println("Reading from data directory...");
            List<String> deptHtml = Files.list(Path.of("data/"))
                    .map(path -> {
                        try {
                            return Files.readString(path);
                        } catch (IOException e) {
                            System.err.println("Unable to read data: ");
                            e.printStackTrace();
                            throw new RuntimeException(e);
                        }
                    }).toList();
            List<Room> rooms = parseWeb(deptHtml);

            CommandList commandList = new CommandList();
            SlashCommandList slashList = new SlashCommandList();
            ButtonHandler buttonHandler = new ButtonHandler();

            commandList.addCommand(new SetupInteractions(slashList));

            slashList.addCommand(new RoomTimetable(rooms, buttonHandler));
            slashList.addCommand(new ListRooms(rooms, buttonHandler));
            slashList.addCommand(new FindRoom(rooms, buttonHandler));

            jda = JDABuilder.createDefault(System.getenv("BOT_TOKEN"))
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(
                            new MessageHandler("&", commandList),
                            new SlashHandler(slashList),
                            buttonHandler)
                    .build();
            jda.awaitReady();
        }
    }

    private static void fetchCourses() throws IOException {
        String templateUrl = "https://w5.ab.ust.hk/wcq/cgi-bin/%s/";
        List<String> yes = List.of("yes", "y");

        Scanner in = new Scanner(System.in);
        LocalDateTime time = LocalDateTime.now();
        int year = time.getYear();
        int month = time.getMonthValue(); // 1 to 12
        int day = time.getDayOfMonth();
        String monthUrl = String.valueOf(month < 9 ? year % 100 - 1: year % 100);
        if (month < 2 && day < 20) {
            monthUrl += "20";
        } else if (month < 6) {
            monthUrl += "30";
        } else if (month < 8) {
            monthUrl += "40";
        } else {
            monthUrl += "10";
        }

        String url = String.format(templateUrl, monthUrl);
        System.out.print("Going to fetch from " + url +". Proceed? (y/n) ");
        String yn = in.nextLine();
        if (!yes.contains(yn.toLowerCase())) {
            System.out.print("Please input alternative year ID (in the form of 2310): ");
            monthUrl = in.nextLine();
            url = String.format(templateUrl, monthUrl);
        }

        Path fileRoot = Path.of("data"); // prep dir for writing
        if (Files.exists(fileRoot)) {
            try (Stream<Path> p = Files.walk(fileRoot)) {
                p.filter(Files::isRegularFile)
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        }
        Files.deleteIfExists(fileRoot);
        Files.createDirectories(fileRoot);

        String finalUrl = url;
        System.out.println("Downloading from " + finalUrl + ".");
        Document root = Jsoup.connect(finalUrl).get();
        Element depts = root.getElementsByClass("depts").get(0);
        List<String> deptList = depts.children().eachText();
        deptList.forEach(str -> {
            try {
                Files.writeString(Path.of("data/" + str + ".html"), Jsoup.connect(finalUrl + "subject/" + str).get().html());
            } catch (IOException e) {
                System.err.println("Unable to write file: ");
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    private static List<Room> parseWeb(List<String> deptHtml) {
        List<Room> room = new ArrayList<>();
        for (String s : deptHtml) {
            Document doc = Jsoup.parse(s);
            for (Element course : doc.getElementsByClass("course")) {
                String courseName = course.getElementsByTag("h2").get(0).text();
                courseName = courseName.trim();
                String[] courseArr = courseName.split(" ");
                String dept = courseArr[0];
                String code = courseArr[1];
                String name = courseName.substring(courseName.indexOf("-") + 1, courseName.lastIndexOf("(")).trim();
                String unitString = courseName.substring(courseName.lastIndexOf("(") + 1, courseName.lastIndexOf(")"));
                unitString = unitString.replace("units", "")
                        .replace("unit", "").trim();
                int units = Integer.parseInt(unitString);

                // Yes, this String is deliberately outside to account for multiple timeslots
                String sectionName;
                Elements sections = course.getElementsByTag("tr");
                sections.removeIf(e -> !e.hasClass("sectodd") && !e.hasClass("secteven"));
                for (int i = 0; i < sections.size(); i++) { // header removed by filter
                    Element section = sections.get(i);
                    Elements info = section.getElementsByTag("td");
                    sectionName = info.get(0).text();
                    sectionName = sectionName.substring(0, sectionName.indexOf("(")).trim();
                    String roomStr = info.get(2).text();
                    if (!roomStr.toUpperCase().contains("TBA")) {
                        Room r = getRoom(roomStr);
                        if (room.contains(r)) {
                            r = room.get(room.indexOf(r));
                        } else {
                            room.add(r);
                        }

                        if (info.get(0).hasAttr("rowspan")) {
                            int toSkip = Integer.parseInt(info.get(0).attr("rowspan"));
                            for (int j = 1; j < toSkip; j++) {
                                Element innerSection = sections.get(j + i);
                                Elements innerInfo = innerSection.getElementsByTag("td");
                                if (innerInfo.get(1).text().toUpperCase().contains("TBA")) {
                                    continue;
                                }
                                Room innerRoom = getRoom(innerInfo.get(1).text());
                                if (room.contains(innerRoom)) {
                                    innerRoom = room.get(room.indexOf(innerRoom));
                                } else {
                                    room.add(innerRoom);
                                }
                                String timeString = innerInfo.get(0).text();
                                innerRoom.addRecord(getTime(timeString), new CourseSection(
                                        name, dept, code, sectionName, units));
                            }
                            i += (toSkip - 1);
                        }

                        String timeString = info.get(1).text();
                        if (timeString.toUpperCase().contains("TBA")) {
                            continue;
                        }
                        r.addRecord(getTime(timeString), new CourseSection(
                            name, dept, code, sectionName, units));
                    } else {
                        if (info.get(0).hasAttr("rowspan")) {
                            i += Integer.parseInt(info.get(0).attr("rowspan")) - 1;
                        }
                    }
                }
            }
        }
        return room;
    }

    private static Room getRoom(String roomStr) {
        String roomName = "";
        String location = "";
        int capacity = -1;
        String[] roomArr = roomStr.split(",");
        roomName = roomArr[0];
        boolean isBracketed = false;
        if (roomStr.contains("(")) {
            String number = roomStr.substring(roomStr.indexOf("(") + 1, roomStr.indexOf(")"));
            capacity = Integer.parseInt(number);
            isBracketed = true;
        }
        if (roomStr.contains(",")) {
            location = roomArr[1].substring(0, isBracketed ? roomArr[1].indexOf("(") : roomArr[1].length()).trim();
        } else if (isBracketed) {
            roomName = roomName.substring(0, roomName.indexOf("(")).trim();
        }
        return new Room(roomName, location, capacity);
    }

    private static TimeRecord getTime(String timeString) {
        String[] arr = timeString.split(" ");
        if (arr.length > 4) { // Exceeds normal cases like TuTh 03:00PM - 04:20PM
            StringBuilder sb = new StringBuilder();
            for (int i = 3; i < arr.length; i++) {
                sb.append(arr[i]);
                sb.append(" ");
            }
            return new TimeRecord(arr[0], arr[2], sb.toString().trim());
        }
        return new TimeRecord(timeString);
    }
}
