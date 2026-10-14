package models;

/**
 * A student record. Objects are immutable: to "edit" a student, create a new
 * {@code Student} and replace the old one (see {@code StudentService.updateStudent}).
 */
public class Student {

    private final String id;
    private final String name;
    private final String email;
    private final String department;
    private final int semester;

    public Student(String id, String name, String email, String department, int semester) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
        this.semester = semester;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getDepartment() {
        return department;
    }

    public int getSemester() {
        return semester;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + department + " | Sem " + semester + " | " + email;
    }
}
