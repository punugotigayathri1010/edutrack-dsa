package services;

import java.util.regex.Pattern;

/**
 * Input rules for student records.
 *
 * <p>Every {@code check...} method returns {@code null} when the value is valid,
 * or a short error message that can be shown to the user.</p>
 */
public class StudentValidator {

    public static final int MIN_SEMESTER = 1;
    public static final int MAX_SEMESTER = 8;

    private static final Pattern ID_PATTERN = Pattern.compile("^[Ss]\\d{3,6}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,39}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern DEPARTMENT_PATTERN = Pattern.compile("^[A-Za-z]{2,6}$");

    private StudentValidator() {
    }

    public static String checkId(String id) {
        if (!ID_PATTERN.matcher(id).matches()) {
            return "ID must be the letter S followed by 3 to 6 digits (for example S105).";
        }
        return null;
    }

    public static String checkName(String name) {
        if (!NAME_PATTERN.matcher(name).matches()) {
            return "Name must be 2-40 characters: letters, spaces, dots, hyphens or apostrophes.";
        }
        return null;
    }

    public static String checkEmail(String email) {
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Email must look like name@example.com.";
        }
        return null;
    }

    public static String checkDepartment(String department) {
        if (!DEPARTMENT_PATTERN.matcher(department).matches()) {
            return "Department must be 2-6 letters (for example CSE or ECE).";
        }
        return null;
    }

    public static String checkSemester(int semester) {
        if (semester < MIN_SEMESTER || semester > MAX_SEMESTER) {
            return "Semester must be between " + MIN_SEMESTER + " and " + MAX_SEMESTER + ".";
        }
        return null;
    }
}
