package main;

import java.util.Scanner;

import models.Faculty;
import models.Student;
import services.AuthService;
import ui.FacultyMenu;
import ui.InputHelper;
import ui.StudentMenu;

/** Entry point of the EduTrack console application: shows the login menu. */
public class Main {

    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthService authService = new AuthService();

    public static void main(String[] args) {
        printWelcome();

        while (true) {
            System.out.println();
            System.out.println("MAIN LOGIN");
            System.out.println("1. Student Login");
            System.out.println("2. Faculty Login");
            System.out.println("0. Exit");

            String choice = InputHelper.readLine(scanner, "Choice: ");
            if (choice.equals("1")) {
                studentLogin();
            } else if (choice.equals("2")) {
                facultyLogin();
            } else if (choice.equals("0")) {
                System.out.println("Thank you for using EduTrack.");
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    private static void printWelcome() {
        System.out.println("==============================================");
        System.out.println("              EDUTRACK PLATFORM");
        System.out.println("     Intelligent University Academic System");
        System.out.println("==============================================");
    }

    private static void studentLogin() {
        String id = InputHelper.readLine(scanner, "Student ID: ");
        String password = InputHelper.readLine(scanner, "Password: ");

        Student student = authService.loginStudent(id, password);
        if (student == null) {
            System.out.println("Invalid student ID/password.");
            return;
        }
        System.out.println("Login successful.");
        new StudentMenu().show(scanner, student);
    }

    private static void facultyLogin() {
        String id = InputHelper.readLine(scanner, "Faculty ID: ");
        String password = InputHelper.readLine(scanner, "Password: ");

        Faculty faculty = authService.loginFaculty(id, password);
        if (faculty == null) {
            System.out.println("Invalid faculty ID/password.");
            return;
        }
        System.out.println("Login successful.");
        new FacultyMenu().show(scanner, faculty);
    }
}
