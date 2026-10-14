package ui;

import java.util.Scanner;

import services.AnalyticsService;

/** Analytics menu (CO6), opened from the faculty dashboard. */
public class AnalyticsMenu {

    private static final int DEFAULT_SAMPLE = 5;

    private final AnalyticsService analytics = new AnalyticsService();

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("--- ANALYTICS ---");
            System.out.println("1. Student merit ranking");
            System.out.println("2. Random check of recent activity");
            System.out.println("0. Back");

            String choice = InputHelper.readLine(scanner, "Choice: ");
            switch (choice) {
                case "1":
                    analytics.rankStudents();
                    break;
                case "2":
                    int size = InputHelper.readIntWithDefault(scanner,
                            "How many events to sample (Enter = " + DEFAULT_SAMPLE + "): ",
                            DEFAULT_SAMPLE, 1, AnalyticsService.MAX_SAMPLE);
                    analytics.sampleActivity(size);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
