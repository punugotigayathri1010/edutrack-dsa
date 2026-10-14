package data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import models.Exam;

/** In-memory store of the current exam timetable. It is empty until a timetable is built. */
public class ExamData {

    private static final List<Exam> exams = new ArrayList<>();

    private ExamData() {
    }

    /** Returns a read-only view of the current timetable. */
    public static List<Exam> getAll() {
        return Collections.unmodifiableList(exams);
    }

    /** Throws away the old timetable and keeps {@code newTimetable}. */
    public static void replaceAll(Collection<Exam> newTimetable) {
        exams.clear();
        exams.addAll(newTimetable);
    }

    /** The exam of one course, or {@code null} when it has not been scheduled. */
    public static Exam findByCourse(String courseCode) {
        for (Exam exam : exams) {
            if (exam.getCourseCode().equalsIgnoreCase(courseCode)) {
                return exam;
            }
        }
        return null;
    }
}
