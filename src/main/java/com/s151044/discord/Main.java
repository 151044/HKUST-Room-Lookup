package com.s151044.discord;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws IOException {
        List<String> arguments = List.of(args);
        if (arguments.contains("--fetch")) {
            String url = "https://w5.ab.ust.hk/wcq/cgi-bin/%s/";
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

            url = String.format(url, monthUrl);
            System.out.print("Going to fetch from " + url +". Proceed? (y/n) ");
            String yn = in.nextLine();
            if (!yes.contains(yn.toLowerCase())) {
                System.out.print("Please input alternative year ID (in the form on 2310): ");
                monthUrl = in.nextLine();
                url = String.format(url, monthUrl);
            }

            Path fileRoot = Path.of("data"); // prep dir for writing
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
                    System.out.println("Unable to write file: ");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        } else {
            System.out.println("Reading from data directory...");

        }
    }
}
