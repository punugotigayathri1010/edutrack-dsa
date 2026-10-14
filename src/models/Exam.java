package models;

/** An examination slot for a course (immutable). */
public class Exam {

    private final String courseCode;
    private final String date;
    private final String room;

    public Exam(String courseCode, String date, String room) {
        this.courseCode = courseCode;
        this.date = date;
        this.room = room;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getDate() {
        return date;
    }

    public String getRoom() {
        return room;
    }

    @Override
    public String toString() {
        return courseCode + " | " + date + " | " + room;
    }
}
