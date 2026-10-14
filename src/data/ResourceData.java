package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.Resource;

/** In-memory store of sample teaching resources (labs, halls, classrooms). */
public class ResourceData {

    private static final List<Resource> resources = new ArrayList<>(Arrays.asList(
            new Resource("R101", "CSE Lab 1", "LAB", 60),
            new Resource("R102", "Seminar Hall", "HALL", 120),
            new Resource("R103", "Room 204", "CLASSROOM", 50),
            new Resource("R104", "CSE Lab 2", "LAB", 60)));

    private ResourceData() {
    }

    /** Returns a read-only view of all resources. */
    public static List<Resource> getAll() {
        return Collections.unmodifiableList(resources);
    }
}
