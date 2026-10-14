package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.Assignment;

/** In-memory store of sample assignments. */
public class AssignmentData {

    private static final List<Assignment> assignments = new ArrayList<>(Arrays.asList(
            new Assignment("A101", "CS301", "KMP String Search",
                    "Implement pattern matching using the Knuth Morris Pratt algorithm."),
            new Assignment("A102", "CS302", "Graph Algorithms",
                    "Compare BFS, DFS and shortest path algorithms."),
            new Assignment("A103", "CS304", "Document Similarity",
                    "Find repeated phrases in two academic documents.")));

    private AssignmentData() {
    }

    /** Returns a read-only view of all assignments. */
    public static List<Assignment> getAll() {
        return Collections.unmodifiableList(assignments);
    }
}
