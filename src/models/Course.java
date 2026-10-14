package models;

/** A course offered by a department (immutable). */
public class Course {

    private final String code;
    private final String name;
    private final String department;
    private final int credits;

    public Course(String code, String name, String department, int credits) {
        this.code = code;
        this.name = name;
        this.department = department;
        this.credits = credits;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDepartment() {
        return department;
    }

    public int getCredits() {
        return credits;
    }

    @Override
    public String toString() {
        return code + " | " + name + " | " + department + " | " + credits + " credits";
    }
}
