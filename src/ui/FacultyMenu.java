package ui;

import java.util.Scanner;

import models.Faculty;
import services.AllocationService;
import services.CourseService;
import services.FacultyService;
import services.SearchService;

/**
 * The faculty dashboard.
 *
 * <p>Options 1-5 are the original faculty features, options 6-10 are student management, 11-12 are exam planning (M5) and analytics (M6).</p>
 */
public class FacultyMenu {

    private final FacultyService facultyService = new FacultyService();
    private final CourseService courseService = new CourseService();
    private final SearchService searchService = new SearchService();
    private final AllocationService allocationService = new AllocationService();
    private final StudentManagementMenu studentManagement = new StudentManagementMenu();
    private final ExamMenu examMenu = new ExamMenu();
    private final AnalyticsMenu analyticsMenu = new AnalyticsMenu();

    public void show(Scanner scanner, Faculty faculty) {
        while (true) {
            printMenu(faculty);
            String choice = InputHelper.readLine(scanner, "Choice: ");

            switch (choice) {
                case "1":
                    System.out.println(faculty);
                    break;
                case "2":
                    facultyService.listFaculty();
                    break;
                case "3":
                    courseService.listCourses();
                    break;
                case "4":
                    runAllocation(scanner);
                    break;
                case "5":
                    searchService.searchCourses(InputHelper.readLine(scanner, "Search: "));
                    break;
                case "6":
                    studentManagement.viewAll();
                    break;
                case "7":
                    studentManagement.add(scanner);
                    break;
                case "8":
                    studentManagement.update(scanner);
                    break;
                case "9":
                    studentManagement.delete(scanner);
                    break;
                case "10":
                    studentManagement.searchByName(scanner);
                    break;
                case "11":
                    examMenu.show(scanner);
                    break;
                case "12":
                    analyticsMenu.show(scanner);
                    break;
                case "0":
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private void printMenu(Faculty faculty) {
        System.out.println();
        System.out.println("=========== FACULTY DASHBOARD ===========");
        System.out.println("Logged in: " + faculty.getName() + " (" + faculty.getId() + ")");
        System.out.println();
        System.out.println("  1. My Profile");
        System.out.println("  2. View Faculty");
        System.out.println("  3. View Courses");
        System.out.println("  4. Faculty-Course Allocation");
        System.out.println("  5. Search Courses");
        System.out.println("  --- Student Management ---");
        System.out.println("  6. View All Students");
        System.out.println("  7. Add Student");
        System.out.println("  8. Update Student");
        System.out.println("  9. Delete Student");
        System.out.println(" 10. Search Student by Name");
        System.out.println("  --- Exams & Analytics ---");
        System.out.println(" 11. Exam Planning");
        System.out.println(" 12. Analytics");
        System.out.println("  0. Logout");
    }

    private void runAllocation(Scanner scanner) {
        // Pressing Enter uses the default limit.
        int maxLoad = InputHelper.readIntWithDefault(scanner, "Maximum courses per faculty member: ",
                AllocationService.DEFAULT_MAX_COURSES_PER_FACULTY,
                AllocationService.MIN_COURSES_PER_FACULTY, AllocationService.MAX_COURSES_PER_FACULTY);
        allocationService.runFacultyCourseAllocation(maxLoad);
    }
}
