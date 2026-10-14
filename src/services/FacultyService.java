package services;

import data.FacultyData;
import models.Faculty;
import util.TextUtil;

/** Business logic for faculty records. */
public class FacultyService {

    /** Prints all faculty members as a table. */
    public void listFaculty() {
        System.out.println();
        System.out.println(TextUtil.padRight("ID", 7) + TextUtil.padRight("Name", 16)
                + TextUtil.padRight("Dept", 7) + "Email");
        System.out.println(TextUtil.repeat('-', 52));
        for (Faculty faculty : FacultyData.getAll()) {
            System.out.println(TextUtil.padRight(faculty.getId(), 7)
                    + TextUtil.padRight(faculty.getName(), 16)
                    + TextUtil.padRight(faculty.getDepartment(), 7)
                    + faculty.getEmail());
        }
    }

    /** Finds a faculty member by ID (case-insensitive); returns {@code null} when there is none. */
    public Faculty find(String id) {
        for (Faculty faculty : FacultyData.getAll()) {
            if (faculty.getId().equalsIgnoreCase(id)) {
                return faculty;
            }
        }
        return null;
    }
}
