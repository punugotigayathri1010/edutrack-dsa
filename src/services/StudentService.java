package services;

import java.util.ArrayList;
import java.util.List;

import data.StudentData;
import dynamicprogramming.Levenshtein;
import models.Student;
import stringalgorithms.KMP;
import util.TextUtil;

/**
 * Business logic for student records: listing, searching, adding, updating and deleting.
 *
 * <p>All validation lives here (and in {@link StudentValidator}), so the console menus only
 * have to read input and print the {@link ServiceResult} messages.</p>
 */
public class StudentService {

    // ------------------------------------------------------------------
    // Reading
    // ------------------------------------------------------------------

    public List<Student> getAllStudents() {
        return StudentData.getAll();
    }

    /** Finds a student by ID (case-insensitive); returns {@code null} when there is none. */
    public Student find(String id) {
        return StudentData.findById(id);
    }

    public boolean idExists(String id) {
        return StudentData.findById(id) != null;
    }

    /** True when another student (not {@code ignoreId}) already uses this email address. */
    public boolean emailInUse(String email, String ignoreId) {
        for (Student student : StudentData.getAll()) {
            boolean sameEmail = student.getEmail().equalsIgnoreCase(email);
            boolean isOtherStudent = ignoreId == null || !student.getId().equalsIgnoreCase(ignoreId);
            if (sameEmail && isOtherStudent) {
                return true;
            }
        }
        return false;
    }

    /** The next free ID suggestion, for example "S105". */
    public String suggestNextId() {
        return StudentData.nextId();
    }

    /** Prints all students as a table. */
    public void listStudents() {
        printTable(StudentData.getAll());
    }

    /** Prints the given students as an aligned table followed by a count. */
    public void printTable(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("No students to show.");
            return;
        }
        System.out.println();
        System.out.println(TextUtil.padRight("ID", 7) + TextUtil.padRight("Name", 20)
                + TextUtil.padRight("Dept", 7) + TextUtil.padRight("Sem", 5) + "Email");
        System.out.println(TextUtil.repeat('-', 62));
        for (Student student : students) {
            System.out.println(TextUtil.padRight(student.getId(), 7)
                    + TextUtil.padRight(student.getName(), 20)
                    + TextUtil.padRight(student.getDepartment(), 7)
                    + TextUtil.padRight(String.valueOf(student.getSemester()), 5)
                    + student.getEmail());
        }
        System.out.println(TextUtil.repeat('-', 62));
        System.out.println("Total: " + students.size() + " student(s)");
    }

    // ------------------------------------------------------------------
    // Searching (KMP + Levenshtein)
    // ------------------------------------------------------------------

    /** Students whose name contains {@code query} (case-insensitive, found with KMP). */
    public List<Student> searchByName(String query) {
        List<Student> matches = new ArrayList<>();
        for (Student student : StudentData.getAll()) {
            if (!KMP.search(student.getName(), query).isEmpty()) {
                matches.add(student);
            }
        }
        return matches;
    }

    /**
     * Suggests the stored name that is closest to {@code query} by edit distance.
     * Each whole name AND each word of it is compared, so "mena" can find "Meena Rao".
     *
     * @return the suggested full name, or {@code null} when nothing is reasonably close
     */
    public String closestName(String query) {
        String best = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Student student : StudentData.getAll()) {
            List<String> candidates = new ArrayList<>();
            candidates.add(student.getName());
            for (String word : student.getName().split("\\s+")) {
                candidates.add(word);
            }

            for (String candidate : candidates) {
                int distance = Levenshtein.distance(query, candidate);
                if (distance < bestDistance) {
                    bestDistance = distance;
                    best = student.getName();
                }
            }
        }

        int allowedDistance = Math.max(2, query.length() / 2);
        return bestDistance <= allowedDistance ? best : null;
    }

    // ------------------------------------------------------------------
    // Adding
    // ------------------------------------------------------------------

    /**
     * Adds a new student after checking every field.
     * A new student can log in immediately with the password {@code ID + "@123"}.
     */
    public ServiceResult addStudent(String id, String name, String email, String department, int semester) {
        String error = firstError(
                StudentValidator.checkId(id),
                StudentValidator.checkName(name),
                StudentValidator.checkEmail(email),
                StudentValidator.checkDepartment(department),
                StudentValidator.checkSemester(semester));
        if (error != null) {
            return ServiceResult.fail(error);
        }
        if (idExists(id)) {
            return ServiceResult.fail("A student with ID " + id.toUpperCase() + " already exists.");
        }
        if (emailInUse(email, null)) {
            return ServiceResult.fail("The email " + email + " is already used by another student.");
        }

        Student student = new Student(id.toUpperCase(), name.trim(), email.trim().toLowerCase(),
                department.toUpperCase(), semester);
        StudentData.add(student);
        return ServiceResult.ok("Student added: " + student);
    }

    // ------------------------------------------------------------------
    // Updating
    // ------------------------------------------------------------------

    /**
     * Updates a student. The ID cannot change. Any field passed as {@code null} keeps its current value.
     * (The model is immutable, so a new {@link Student} replaces the old one.)
     */
    public ServiceResult updateStudent(String id, String newName, String newEmail,
                                       String newDepartment, Integer newSemester) {
        Student current = find(id);
        if (current == null) {
            return ServiceResult.fail("No student found with ID " + id + ".");
        }

        String name = newName != null ? newName.trim() : current.getName();
        String email = newEmail != null ? newEmail.trim().toLowerCase() : current.getEmail();
        String department = newDepartment != null ? newDepartment.toUpperCase() : current.getDepartment();
        int semester = newSemester != null ? newSemester : current.getSemester();

        String error = firstError(
                StudentValidator.checkName(name),
                StudentValidator.checkEmail(email),
                StudentValidator.checkDepartment(department),
                StudentValidator.checkSemester(semester));
        if (error != null) {
            return ServiceResult.fail(error);
        }
        if (emailInUse(email, current.getId())) {
            return ServiceResult.fail("The email " + email + " is already used by another student.");
        }

        Student updated = new Student(current.getId(), name, email, department, semester);
        StudentData.replace(updated);
        return ServiceResult.ok("Student updated: " + updated);
    }

    // ------------------------------------------------------------------
    // Deleting
    // ------------------------------------------------------------------

    /** Removes a student permanently. */
    public ServiceResult deleteStudent(String id) {
        Student student = find(id);
        if (student == null) {
            return ServiceResult.fail("No student found with ID " + id + ".");
        }
        StudentData.removeById(student.getId());
        return ServiceResult.ok("Deleted student: " + student);
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    /** Returns the first non-null message, or null when every check passed. */
    private static String firstError(String... errors) {
        for (String error : errors) {
            if (error != null) {
                return error;
            }
        }
        return null;
    }
}
