package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.Student;

/**
 * In-memory store of students.
 *
 * <p>This is the only class that changes the student list. Other classes read
 * it through {@link #getAll()} (a read-only view) and change it through
 * {@link #add}, {@link #replace} and {@link #removeById}.</p>
 */
public class StudentData {

    private static final List<Student> students = new ArrayList<>(Arrays.asList(
            new Student("S101", "Gayathri", "gayathri@edutrack.edu", "CSE", 4),
            new Student("S102", "Ananya", "ananya@edutrack.edu", "CSE", 4),
            new Student("S103", "Rahul", "rahul@edutrack.edu", "ECE", 3),
            new Student("S104", "Sameen", "sameen@edutrack.edu", "CSE", 2)));

    private StudentData() {
    }

    /** Returns a read-only view of all students. */
    public static List<Student> getAll() {
        return Collections.unmodifiableList(students);
    }

    /** Finds a student by ID (case-insensitive); returns {@code null} when missing. */
    public static Student findById(String id) {
        for (Student student : students) {
            if (student.getId().equalsIgnoreCase(id)) {
                return student;
            }
        }
        return null;
    }

    /** Appends a new student. */
    public static void add(Student student) {
        students.add(student);
    }

    /**
     * Replaces the stored student that has the same ID as {@code updated}.
     *
     * @return {@code true} if a student was replaced, {@code false} if the ID was not found
     */
    public static boolean replace(Student updated) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equalsIgnoreCase(updated.getId())) {
                students.set(i, updated);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the student with the given ID.
     *
     * @return {@code true} if a student was removed
     */
    public static boolean removeById(String id) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equalsIgnoreCase(id)) {
                students.remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Suggests the next free ID in the "S###" format, one above the highest
     * number currently used (S104 -> S105). IDs of deleted students are not reused
     * while a higher one exists.
     */
    public static String nextId() {
        int highest = 100;
        for (Student student : students) {
            String digits = student.getId().substring(1);
            try {
                highest = Math.max(highest, Integer.parseInt(digits));
            } catch (NumberFormatException ignored) {
                // Non-numeric IDs are ignored when computing the next number.
            }
        }
        return "S" + (highest + 1);
    }
}
