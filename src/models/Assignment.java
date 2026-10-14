package models;

/** An assignment that belongs to a course (immutable). */
public class Assignment {

    private final String id;
    private final String courseCode;
    private final String title;
    private final String description;

    public Assignment(String id, String courseCode, String title, String description) {
        this.id = id;
        this.courseCode = courseCode;
        this.title = title;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return id + " | " + courseCode + " | " + title;
    }
}
