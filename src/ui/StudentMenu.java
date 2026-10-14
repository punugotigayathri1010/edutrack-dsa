package ui;

import java.util.Scanner;

import models.Student;
import services.CourseService;
import services.ExamScheduleService;
import services.SearchService;

/** The student portal. */
public class StudentMenu {

    private final CourseService courseService = new CourseService();
    private final SearchService searchService = new SearchService();
    private final DocumentMenu documentMenu = new DocumentMenu();
    private final ExamScheduleService examService = new ExamScheduleService();

    public void show(Scanner scanner, Student student) {
        while (true) {
            printMenu(student);
            String choice = InputHelper.readLine(scanner, "Choice: ");

            switch (choice) {
                case "1":
                    System.out.println(student);
                    break;
                case "2":
                    courseService.listCourses();
                    break;
                case "3":
                    searchService.searchCourses(InputHelper.readLine(scanner, "Search: "));
                    break;
                case "4":
                    searchService.searchAssignments(InputHelper.readLine(scanner, "Keyword: "));
                    break;
                case "5":
                    documentMenu.show(scanner);
                    break;
                case "6":
                    examService.showTimetableFor(student);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void printMenu(Student student) {
        System.out.println();
        System.out.println("--- STUDENT PORTAL ---");
        System.out.println("Logged in: " + student.getName() + " (" + student.getId() + ")");
        System.out.println("1. My Profile");
        System.out.println("2. View Courses");
        System.out.println("3. Search Courses");
        System.out.println("4. Search Assignments");
        System.out.println("5. Document Analysis");
        System.out.println("6. My Exam Timetable");
        System.out.println("0. Logout");
    }
}
