package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.Faculty;

/** In-memory store of sample faculty members. */
public class FacultyData {

    private static final List<Faculty> faculty = new ArrayList<>(Arrays.asList(
            new Faculty("F101", "Dr. Meera", "meera@edutrack.edu", "CSE"),
            new Faculty("F102", "Dr. Arun", "arun@edutrack.edu", "CSE"),
            new Faculty("F103", "Dr. Priya", "priya@edutrack.edu", "ECE")));

    private FacultyData() {
    }

    /** Returns a read-only view of all faculty members. */
    public static List<Faculty> getAll() {
        return Collections.unmodifiableList(faculty);
    }
}
