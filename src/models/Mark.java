package models;

/** A mark (out of 100) that one student scored in one course (immutable). */
public class Mark {

    private final String studentId;
    private final String courseCode;
    private final int marks;

    public Mark(String studentId, String courseCode, int marks) {
        this.studentId = studentId;
        this.courseCode = courseCode;
        this.marks = marks;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public int getMarks() {
        return marks;
    }

    @Override
    public String toString() {
        return studentId + " | " + courseCode + " | " + marks;
    }
}
