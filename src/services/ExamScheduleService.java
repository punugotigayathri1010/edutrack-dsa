package services;

import java.util.ArrayList;
import java.util.List;

import data.CourseData;
import data.EnrollmentData;
import data.ExamData;
import data.ResourceData;
import data.StudentData;
import models.Course;
import models.Exam;
import models.Resource;
import models.Student;
import npcompleteness.SATSolver;
import npcompleteness.SimpleGraph;
import util.TextUtil;

/**
 * Exam planning for module M5 (NP-completeness).
 *
 * <ul>
 *   <li><b>Clash graph</b> - courses are vertices; two courses are joined when a student takes both.</li>
 *   <li><b>Timetable</b> - giving every course a slot so that joined courses never share one is the
 *       graph-colouring problem (NP-complete). It is written as a SAT formula and solved by
 *       {@link SATSolver}.</li>
 * </ul>
 */
public class ExamScheduleService {

    public static final int DEFAULT_SLOTS = 3;
    public static final int MIN_SLOTS = 1;
    public static final int MAX_SLOTS = 8;

    // ------------------------------------------------------------------
    // Building blocks (also used by SelfTest)
    // ------------------------------------------------------------------

    /** Codes of all courses, in the order of CourseData; vertex i of the clash graph is code i. */
    public static List<String> courseCodes() {
        List<String> codes = new ArrayList<>();
        for (Course course : CourseData.getAll()) {
            codes.add(course.getCode());
        }
        return codes;
    }

    /** Joins two courses whenever at least one current student takes both. */
    public static SimpleGraph buildClashGraph(List<String> codes) {
        SimpleGraph graph = new SimpleGraph(codes.size());
        for (Student student : StudentData.getAll()) {
            List<String> taken = EnrollmentData.coursesOf(student.getId());
            for (int i = 0; i < taken.size(); i++) {
                for (int j = i + 1; j < taken.size(); j++) {
                    int a = codes.indexOf(taken.get(i));
                    int b = codes.indexOf(taken.get(j));
                    if (a >= 0 && b >= 0) {
                        graph.addEdge(a, b);
                    }
                }
            }
        }
        return graph;
    }

    /**
     * Gives every vertex one of {@code slots} slots so that joined vertices differ, by solving a SAT
     * formula. Variable (course c, slot s) means "course c is examined in slot s".
     *
     * @return the slot of every vertex, or {@code null} when {@code slots} slots are not enough
     */
    public static int[] colourWithSat(SimpleGraph graph, int slots) {
        return colourWithSat(graph, slots, null);
    }

    private static int[] colourWithSat(SimpleGraph graph, int slots, int[] statistics) {
        int n = graph.size();
        List<int[]> clauses = new ArrayList<>();

        for (int c = 0; c < n; c++) {
            // Rule 1: every course gets at least one slot.
            int[] atLeastOne = new int[slots];
            for (int s = 0; s < slots; s++) {
                atLeastOne[s] = variable(c, s, slots);
            }
            clauses.add(atLeastOne);

            // Rule 2: and not two different slots.
            for (int s = 0; s < slots; s++) {
                for (int t = s + 1; t < slots; t++) {
                    clauses.add(new int[] {-variable(c, s, slots), -variable(c, t, slots)});
                }
            }
        }

        // Rule 3: courses that share a student must not share a slot.
        for (int[] edge : graph.edges()) {
            for (int s = 0; s < slots; s++) {
                clauses.add(new int[] {-variable(edge[0], s, slots), -variable(edge[1], s, slots)});
            }
        }

        int[][] formula = clauses.toArray(new int[0][]);
        SATSolver solver = new SATSolver(n * slots, formula);
        boolean[] model = solver.solve();
        if (statistics != null) {
            statistics[0] = n * slots;
            statistics[1] = formula.length;
            statistics[2] = solver.getDecisions();
        }
        if (model == null) {
            return null;
        }

        int[] slotOf = new int[n];
        for (int c = 0; c < n; c++) {
            for (int s = 0; s < slots; s++) {
                if (model[variable(c, s, slots)]) {
                    slotOf[c] = s;
                }
            }
        }
        return slotOf;
    }

    private static int variable(int course, int slot, int slots) {
        return course * slots + slot + 1;
    }

    /** "Day 1 Morning", "Day 1 Afternoon", "Day 2 Morning" ... */
    public static String slotLabel(int slot) {
        return "Day " + (slot / 2 + 1) + (slot % 2 == 0 ? " Morning" : " Afternoon");
    }

    // ------------------------------------------------------------------
    // Reports
    // ------------------------------------------------------------------

    /** Lists every pair of courses that share students, and who those students are. */
    public void showClashes() {
        List<String> codes = courseCodes();
        SimpleGraph graph = buildClashGraph(codes);

        TextUtil.printBanner("EXAM CLASHES");
        System.out.println("Two courses clash when at least one student takes both of them,");
        System.out.println("so their exams cannot be held at the same time.");
        TextUtil.printSection("Clashing courses");

        List<int[]> edges = graph.edges();
        if (edges.isEmpty()) {
            System.out.println("No clashes: every exam can be held at the same time.");
            return;
        }
        for (int[] edge : edges) {
            String a = codes.get(edge[0]);
            String b = codes.get(edge[1]);
            System.out.println(TextUtil.padRight(a + " & " + b, 16) + "shared by " + sharedStudents(a, b));
        }
        System.out.println();
        System.out.println(codes.size() + " courses, " + edges.size() + " clashing pairs.");
    }

    /** Builds a clash-free timetable using at most {@code slots} slots (more are tried if needed). */
    public void buildTimetable(int slots) {
        List<String> codes = courseCodes();
        SimpleGraph graph = buildClashGraph(codes);

        TextUtil.printBanner("EXAM TIMETABLE");
        System.out.println("Rule: no student may have two exams in the same slot.");
        System.out.println("The rules are written as a yes/no formula and solved automatically.");

        int[] statistics = new int[3];
        int used = slots;
        int[] slotOf = colourWithSat(graph, used, statistics);
        if (slotOf == null) {
            System.out.println();
            System.out.println("Not possible with " + slots + " slot(s). Trying more slots...");
            while (slotOf == null && used < Math.max(slots, codes.size())) {
                used++;
                slotOf = colourWithSat(graph, used, statistics);
            }
        }
        if (slotOf == null) {
            System.out.println("No clash-free timetable was found.");
            return;
        }

        List<Exam> timetable = new ArrayList<>();
        List<Resource> rooms = new ArrayList<>(ResourceData.getAll());
        int[] roomsUsedInSlot = new int[used];
        for (int c = 0; c < codes.size(); c++) {
            int slot = slotOf[c];
            String room = rooms.isEmpty() ? "TBA" : rooms.get(roomsUsedInSlot[slot] % rooms.size()).getName();
            roomsUsedInSlot[slot]++;
            timetable.add(new Exam(codes.get(c), slotLabel(slot), room));
        }
        ExamData.replaceAll(timetable);

        System.out.println();
        System.out.println("Formula size : " + statistics[0] + " yes/no variables, " + statistics[1] + " rules");
        System.out.println("Solver effort: " + statistics[2] + " guess(es)");
        System.out.println("Slots used   : " + used + (used > slots ? " (more than the " + slots + " asked for)" : ""));
        printTimetable(codes, slotOf, used);
        System.out.println();
        System.out.println(allSlotsClashFree(graph, slotOf)
                ? "Checked: no two clashing courses share a slot."
                : "WARNING: the check found a clash.");
    }

    /** Prints the stored timetable (or a hint when none has been built yet). */
    public void showTimetable() {
        TextUtil.printBanner("CURRENT EXAM TIMETABLE");
        if (ExamData.getAll().isEmpty()) {
            System.out.println("No timetable yet. Build one first.");
            return;
        }
        System.out.println(TextUtil.padRight("Course", 10) + TextUtil.padRight("When", 20) + "Room");
        System.out.println(TextUtil.repeat('-', 46));
        for (Exam exam : ExamData.getAll()) {
            System.out.println(TextUtil.padRight(exam.getCourseCode(), 10)
                    + TextUtil.padRight(exam.getDate(), 20) + exam.getRoom());
        }
    }

    /** Prints the exams of the courses one student takes. */
    public void showTimetableFor(Student student) {
        TextUtil.printSection("Exam timetable for " + student.getName());
        List<String> taken = EnrollmentData.coursesOf(student.getId());
        if (taken.isEmpty()) {
            System.out.println("You are not enrolled in any course yet.");
            return;
        }
        if (ExamData.getAll().isEmpty()) {
            System.out.println("The exam timetable has not been published yet.");
            return;
        }
        for (String code : taken) {
            Exam exam = ExamData.findByCourse(code);
            System.out.println(TextUtil.padRight(code, 10)
                    + (exam == null ? "not scheduled" : TextUtil.padRight(exam.getDate(), 20) + exam.getRoom()));
        }
    }

    // ------------------------------------------------------------------
    // Small helpers
    // ------------------------------------------------------------------

    private void printTimetable(List<String> codes, int[] slotOf, int slotCount) {
        TextUtil.printSection("Timetable");
        System.out.println(TextUtil.padRight("Slot", 20) + TextUtil.padRight("Courses", 24) + "Rooms");
        System.out.println(TextUtil.repeat('-', 62));
        for (int s = 0; s < slotCount; s++) {
            StringBuilder courses = new StringBuilder();
            StringBuilder rooms = new StringBuilder();
            for (int c = 0; c < codes.size(); c++) {
                if (slotOf[c] == s) {
                    Exam exam = ExamData.findByCourse(codes.get(c));
                    appendWithComma(courses, codes.get(c));
                    appendWithComma(rooms, exam == null ? "TBA" : exam.getRoom());
                }
            }
            System.out.println(TextUtil.padRight(slotLabel(s), 20)
                    + TextUtil.padRight(courses.length() == 0 ? "-" : courses.toString(), 24)
                    + (rooms.length() == 0 ? "-" : rooms.toString()));
        }
    }

    private static void appendWithComma(StringBuilder builder, String text) {
        if (builder.length() > 0) {
            builder.append(", ");
        }
        builder.append(text);
    }

    private static boolean allSlotsClashFree(SimpleGraph graph, int[] slotOf) {
        for (int[] edge : graph.edges()) {
            if (slotOf[edge[0]] == slotOf[edge[1]]) {
                return false;
            }
        }
        return true;
    }

    private String sharedStudents(String courseA, String courseB) {
        StringBuilder result = new StringBuilder();
        for (Student student : StudentData.getAll()) {
            List<String> taken = EnrollmentData.coursesOf(student.getId());
            if (taken.contains(courseA) && taken.contains(courseB)) {
                appendWithComma(result, student.getName() + " (" + student.getId() + ")");
            }
        }
        return result.toString();
    }

    private static String names(List<String> codes, List<Integer> vertices) {
        List<Integer> sorted = new ArrayList<>(vertices);
        java.util.Collections.sort(sorted);
        StringBuilder text = new StringBuilder();
        for (int v : sorted) {
            appendWithComma(text, codes.get(v));
        }
        return text.length() == 0 ? "(none)" : text.toString();
    }
}
