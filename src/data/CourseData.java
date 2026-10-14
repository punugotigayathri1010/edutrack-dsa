package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.Course;

/** In-memory store of sample courses. */
public class CourseData {

    private static final List<Course> courses = new ArrayList<>(Arrays.asList(
            new Course("CS301", "Data Structures", "CSE", 4),
            new Course("CS302", "Algorithms", "CSE", 4),
            new Course("CS303", "Operating Systems", "CSE", 3),
            new Course("CS304", "Machine Learning", "CSE", 4),
            new Course("EC201", "Digital Electronics", "ECE", 3)));

    private CourseData() {
    }

    /** Returns a read-only view of all courses. */
    public static List<Course> getAll() {
        return Collections.unmodifiableList(courses);
    }
}
