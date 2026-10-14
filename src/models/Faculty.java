package models;

/** A faculty member (immutable). */
public class Faculty {

    private final String id;
    private final String name;
    private final String email;
    private final String department;

    public Faculty(String id, String name, String email, String department) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.department = department;
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

    @Override
    public String toString() {
        return id + " | " + name + " | " + department + " | " + email;
    }
}
