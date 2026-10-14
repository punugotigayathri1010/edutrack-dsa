package ui;

import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import models.Student;
import services.AuthService;
import services.ServiceResult;
import services.StudentService;
import services.StudentValidator;
import util.TextUtil;

/**
 * The student-management part of the faculty dashboard: view, add, update, delete and search.
 *
 * <p>This class only talks to the user. All rules (validation, duplicate checks, storing)
 * are in {@link StudentService}.</p>
 */
public class StudentManagementMenu {

    private static final String EMAIL_DOMAIN = "@edutrack.edu";

    private final StudentService studentService = new StudentService();

    // ------------------------------------------------------------------
    // View
    // ------------------------------------------------------------------

    public void viewAll() {
        TextUtil.printSection("ALL STUDENTS");
        studentService.listStudents();
    }

    // ------------------------------------------------------------------
    // Add
    // ------------------------------------------------------------------

    public void add(Scanner scanner) {
        TextUtil.printSection("ADD STUDENT");

        String suggestedId = studentService.suggestNextId();
        // Pressing Enter accepts the suggested ID.
        String id = InputHelper.readWithDefault(scanner, "Student ID: ", suggestedId, this::checkNewId);

        String name = InputHelper.readRequired(scanner, "Name: ", StudentValidator::checkName);

        String suggestedEmail = suggestEmail(name);
        // Pressing Enter accepts the email built from the name.
        String email = InputHelper.readWithDefault(scanner, "Email: ", suggestedEmail,
                value -> checkEmail(value, null));

        String department = InputHelper.readRequired(scanner, "Department: ", StudentValidator::checkDepartment);

        int semester = InputHelper.readInt(scanner, "Semester: ",
                StudentValidator.MIN_SEMESTER, StudentValidator.MAX_SEMESTER);

        ServiceResult result = studentService.addStudent(id, name, email, department, semester);
        System.out.println(result.getMessage());
        if (result.isSuccess()) {
            System.out.println("The student can log in with ID " + id.toUpperCase()
                    + " and password " + AuthService.passwordFor(id) + ".");
        }
    }

    // ------------------------------------------------------------------
    // Update
    // ------------------------------------------------------------------

    public void update(Scanner scanner) {
        TextUtil.printSection("UPDATE STUDENT");

        Student current = askForExistingStudent(scanner, "Student ID: ");
        if (current == null) {
            System.out.println("Update cancelled.");
            return;
        }

        System.out.println("Current record:");
        studentService.printTable(Collections.singletonList(current));

        String newName = InputHelper.readOptional(scanner,
                "New name [" + current.getName() + "]: ", StudentValidator::checkName);
        String newEmail = InputHelper.readOptional(scanner,
                "New email [" + current.getEmail() + "]: ", value -> checkEmail(value, current.getId()));
        String newDepartment = InputHelper.readOptional(scanner,
                "New department [" + current.getDepartment() + "]: ", StudentValidator::checkDepartment);
        Integer newSemester = InputHelper.readOptionalInt(scanner,
                "New semester [" + current.getSemester() + "]: ",
                StudentValidator.MIN_SEMESTER, StudentValidator.MAX_SEMESTER);

        if (newName == null && newEmail == null && newDepartment == null && newSemester == null) {
            System.out.println("Nothing was changed.");
            return;
        }

        ServiceResult result = studentService.updateStudent(current.getId(), newName, newEmail,
                newDepartment, newSemester);
        System.out.println(result.getMessage());
        if (result.isSuccess()) {
            printChanges(current, studentService.find(current.getId()));
        }
    }

    /** Lists only the fields that differ between the old and the new record. */
    private void printChanges(Student before, Student after) {
        System.out.println("Changes:");
        printIfChanged("Name", before.getName(), after.getName());
        printIfChanged("Email", before.getEmail(), after.getEmail());
        printIfChanged("Department", before.getDepartment(), after.getDepartment());
        printIfChanged("Semester", String.valueOf(before.getSemester()), String.valueOf(after.getSemester()));
    }

    private void printIfChanged(String label, String oldValue, String newValue) {
        if (!oldValue.equals(newValue)) {
            System.out.println("  " + TextUtil.padRight(label + ":", 13) + oldValue + "  ->  " + newValue);
        }
    }

    // ------------------------------------------------------------------
    // Delete
    // ------------------------------------------------------------------

    public void delete(Scanner scanner) {
        TextUtil.printSection("DELETE STUDENT");

        Student student = askForExistingStudent(scanner, "Student ID: ");
        if (student == null) {
            System.out.println("Delete cancelled.");
            return;
        }

        studentService.printTable(Collections.singletonList(student));
        if (!InputHelper.confirm(scanner, "Delete this student permanently?")) {
            System.out.println("Nothing was deleted.");
            return;
        }

        ServiceResult result = studentService.deleteStudent(student.getId());
        System.out.println(result.getMessage());
        if (result.isSuccess()) {
            System.out.println("This student can no longer log in.");
        }
    }

    // ------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------

    public void searchByName(Scanner scanner) {
        TextUtil.printSection("SEARCH STUDENT BY NAME");
        String query = InputHelper.readLine(scanner, "Name: ");
        if (query.isEmpty()) {
            System.out.println("Nothing to search for.");
            return;
        }

        List<Student> matches = studentService.searchByName(query);
        if (!matches.isEmpty()) {
            studentService.printTable(matches);
            return;
        }

        String closest = studentService.closestName(query);
        System.out.println("No student matches \"" + query + "\"."
                + (closest == null ? "" : " Did you mean \"" + closest + "\"?"));
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Keeps asking for an ID until an existing student is found; an empty answer cancels and returns null. */
    private Student askForExistingStudent(Scanner scanner, String prompt) {
        while (true) {
            String id = InputHelper.readLine(scanner, prompt);
            if (id.isEmpty()) {
                return null;
            }
            Student student = studentService.find(id);
            if (student != null) {
                return student;
            }
            System.out.println("  ! No student found with ID " + id + ".");
        }
    }

    private String checkNewId(String id) {
        String formatError = StudentValidator.checkId(id);
        if (formatError != null) {
            return formatError;
        }
        if (studentService.idExists(id)) {
            return "ID " + id.toUpperCase() + " is already used by another student.";
        }
        return null;
    }

    private String checkEmail(String email, String ignoreId) {
        String formatError = StudentValidator.checkEmail(email);
        if (formatError != null) {
            return formatError;
        }
        if (studentService.emailInUse(email, ignoreId)) {
            return "This email is already used by another student.";
        }
        return null;
    }

    /** "Priya Nair" -> "priya@edutrack.edu" (first word, letters only). */
    private String suggestEmail(String name) {
        String firstWord = name.trim().split("\\s+")[0].toLowerCase().replaceAll("[^a-z]", "");
        return (firstWord.isEmpty() ? "student" : firstWord) + EMAIL_DOMAIN;
    }
}
