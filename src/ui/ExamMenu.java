package ui;

import java.util.Scanner;

import services.ExamScheduleService;

/** Exam planning menu (CO5), opened from the faculty dashboard. */
public class ExamMenu {

    private final ExamScheduleService examService = new ExamScheduleService();

    public void show(Scanner scanner) {
        while (true) {
            System.out.println();
            System.out.println("--- EXAM PLANNING ---");
            System.out.println("1. Show exam clashes");
            System.out.println("2. Build exam timetable");
            System.out.println("3. View current timetable");
            System.out.println("0. Back");

            String choice = InputHelper.readLine(scanner, "Choice: ");
            switch (choice) {
                case "1":
                    examService.showClashes();
                    break;
                case "2":
                    int slots = InputHelper.readIntWithDefault(scanner,
                            "Exam slots available (Enter = " + ExamScheduleService.DEFAULT_SLOTS + "): ",
                            ExamScheduleService.DEFAULT_SLOTS,
                            ExamScheduleService.MIN_SLOTS, ExamScheduleService.MAX_SLOTS);
                    examService.buildTimetable(slots);
                    break;
                case "3":
                    examService.showTimetable();
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }
}
