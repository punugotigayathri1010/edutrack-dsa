package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * In-memory record of which courses each student takes. The exam planner uses it to find
 * courses that share students (and so cannot be examined at the same time).
 */
public class EnrollmentData {

    private static final Map<String, List<String>> enrolled = new HashMap<>();

    static {
        enrolled.put("S101", Arrays.asList("CS301", "CS302", "CS304"));
        enrolled.put("S102", Arrays.asList("CS301", "CS303", "CS304"));
        enrolled.put("S103", Arrays.asList("CS303", "EC201"));
        enrolled.put("S104", Arrays.asList("CS301", "CS302"));
    }

    private EnrollmentData() {
    }

    /** Course codes taken by a student; empty for a student with no enrolments (for example a new one). */
    public static List<String> coursesOf(String studentId) {
        List<String> courses = enrolled.get(studentId.toUpperCase());
        if (courses == null) {
            return Collections.emptyList();
        }
        return new ArrayList<>(courses);
    }
}
