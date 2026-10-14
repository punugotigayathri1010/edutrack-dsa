package data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import models.Mark;

/** In-memory store of sample marks, used by the analytics reports. */
public class MarkData {

    private static final List<Mark> marks = new ArrayList<>(Arrays.asList(
            new Mark("S101", "CS301", 88), new Mark("S101", "CS302", 76), new Mark("S101", "CS304", 91),
            new Mark("S102", "CS301", 92), new Mark("S102", "CS303", 81), new Mark("S102", "CS304", 85),
            new Mark("S103", "CS303", 67), new Mark("S103", "EC201", 74),
            new Mark("S104", "CS301", 58), new Mark("S104", "CS302", 63)));

    private MarkData() {
    }

    /** Returns a read-only view of all marks. */
    public static List<Mark> getAll() {
        return Collections.unmodifiableList(marks);
    }
}
