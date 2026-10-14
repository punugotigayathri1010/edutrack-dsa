package services;

import data.FacultyData;
import data.StudentData;
import models.Faculty;
import models.Student;

/**
 * Prototype login checks.
 *
 * <p>The password of every account is its ID followed by "@123" (for example S101 / S101@123).
 * A real system would store hashed passwords instead.</p>
 */
public class AuthService {

    private static final String PASSWORD_SUFFIX = "@123";

    /** Returns the student when the ID and password are right, otherwise {@code null}. */
    public Student loginStudent(String id, String password) {
        Student student = StudentData.findById(id);
        if (student != null && passwordMatches(student.getId(), password)) {
            return student;
        }
        return null;
    }

    /** Returns the faculty member when the ID and password are right, otherwise {@code null}. */
    public Faculty loginFaculty(String id, String password) {
        for (Faculty faculty : FacultyData.getAll()) {
            if (faculty.getId().equalsIgnoreCase(id) && passwordMatches(faculty.getId(), password)) {
                return faculty;
            }
        }
        return null;
    }

    /** The password a given ID must use (shown to faculty when they add a student). */
    public static String passwordFor(String id) {
        return id.toUpperCase() + PASSWORD_SUFFIX;
    }

    private boolean passwordMatches(String id, String password) {
        return password.equalsIgnoreCase(id + PASSWORD_SUFFIX);
    }
}
