package services;

import java.util.ArrayList;
import java.util.List;

import data.CourseData;
import data.FacultyData;
import models.Course;
import models.Faculty;
import networkflow.Dinic;
import networkflow.EdmondsKarp;
import networkflow.FlowNetwork;
import networkflow.FlowResult;
import networkflow.FlowResult.AugmentingStep;
import networkflow.FordFulkerson;
import util.TextUtil;

/**
 * Gives every course a teacher, using as many courses as possible.
 *
 * <p>How it works (in plain words): the program keeps handing a free course to a faculty member
 * who is allowed to teach it and still has room in their timetable. When a course cannot be
 * given directly, it may move a course from one faculty member to another to make room.
 * It stops when no more courses can be assigned.</p>
 *
 * <p>Behind the scenes this is a network-flow problem
 * (SOURCE -> faculty -> course -> SINK). Three flow algorithms are run and checked against each
 * other, but the report itself only uses everyday words.</p>
 */
public class AllocationService {

    public static final int DEFAULT_MAX_COURSES_PER_FACULTY = 2;
    public static final int MIN_COURSES_PER_FACULTY = 1;
    public static final int MAX_COURSES_PER_FACULTY = 5;

    /** Node numbering of the flow network: SOURCE, all faculty, all courses, SINK. */
    static class Layout {
        final List<Faculty> faculty;
        final List<Course> courses;
        final int source = 0;
        final int firstFaculty = 1;
        final int firstCourse;
        final int sink;

        Layout(List<Faculty> faculty, List<Course> courses) {
            this.faculty = faculty;
            this.courses = courses;
            this.firstCourse = firstFaculty + faculty.size();
            this.sink = firstCourse + courses.size();
        }

        int facultyNode(int index) {
            return firstFaculty + index;
        }

        int courseNode(int index) {
            return firstCourse + index;
        }

        Faculty facultyAt(int node) {
            return faculty.get(node - firstFaculty);
        }

        Course courseAt(int node) {
            return courses.get(node - firstCourse);
        }
    }

    /** Runs the whole allocation report with the given teaching limit per faculty member. */
    public void runFacultyCourseAllocation(int maxCoursesPerFaculty) {
        Layout layout = new Layout(new ArrayList<>(FacultyData.getAll()), new ArrayList<>(CourseData.getAll()));
        FlowNetwork network = buildNetwork(layout, maxCoursesPerFaculty);

        FlowResult result = EdmondsKarp.solve(network, layout.source, layout.sink);
        verifyWithOtherMethods(network, layout, result);

        TextUtil.printBanner("FACULTY-COURSE ALLOCATION");
        printRules(maxCoursesPerFaculty);
        printEligibilityMatrix(layout);
        printSteps(layout, result);
        printAssignments(layout, result);
        printReasonsForUnassigned(layout, result, maxCoursesPerFaculty);
        System.out.println();
    }

    // ------------------------------------------------------------------
    // Building the network
    // ------------------------------------------------------------------

    private FlowNetwork buildNetwork(Layout layout, int maxLoad) {
        String[] names = new String[layout.sink + 1];
        names[layout.source] = "SOURCE";
        names[layout.sink] = "SINK";
        for (int i = 0; i < layout.faculty.size(); i++) {
            names[layout.facultyNode(i)] = layout.faculty.get(i).getId();
        }
        for (int j = 0; j < layout.courses.size(); j++) {
            names[layout.courseNode(j)] = layout.courses.get(j).getCode();
        }

        FlowNetwork network = new FlowNetwork(names);
        for (int i = 0; i < layout.faculty.size(); i++) {
            network.addEdge(layout.source, layout.facultyNode(i), maxLoad);
            for (int j = 0; j < layout.courses.size(); j++) {
                if (isEligible(layout.faculty.get(i), layout.courses.get(j))) {
                    network.addEdge(layout.facultyNode(i), layout.courseNode(j), 1);
                }
            }
        }
        for (int j = 0; j < layout.courses.size(); j++) {
            network.addEdge(layout.courseNode(j), layout.sink, 1);
        }
        return network;
    }

    /** A faculty member may teach a course of their own department. */
    private boolean isEligible(Faculty faculty, Course course) {
        return faculty.getDepartment().equalsIgnoreCase(course.getDepartment());
    }

    /**
     * Silent safety check: two other flow algorithms must find the same total.
     * Nothing is printed unless they disagree.
     */
    private void verifyWithOtherMethods(FlowNetwork network, Layout layout, FlowResult result) {
        int fordFulkerson = FordFulkerson.solve(network, layout.source, layout.sink).getMaxFlow();
        int dinic = Dinic.solve(network, layout.source, layout.sink).getMaxFlow();
        if (fordFulkerson != result.getMaxFlow() || dinic != result.getMaxFlow()) {
            System.out.println("Warning: internal consistency check failed. Please report this.");
        }
    }

    // ------------------------------------------------------------------
    // Report sections (plain language)
    // ------------------------------------------------------------------

    private void printRules(int maxLoad) {
        TextUtil.printSection("Rules");
        System.out.println("  - A faculty member can only teach courses of their own department.");
        System.out.println("  - Each faculty member can teach at most " + maxLoad + " course(s).");
        System.out.println("  - Each course needs exactly one teacher.");
    }

    private void printEligibilityMatrix(Layout layout) {
        TextUtil.printSection("Who can teach what (X = can teach)");
        StringBuilder header = new StringBuilder(TextUtil.padRight("", 20));
        for (Course course : layout.courses) {
            header.append(TextUtil.padRight(course.getCode(), 7));
        }
        System.out.println("  " + header);

        for (Faculty faculty : layout.faculty) {
            StringBuilder row = new StringBuilder(TextUtil.padRight(faculty.getId() + " " + faculty.getName(), 20));
            for (Course course : layout.courses) {
                row.append(TextUtil.padRight(isEligible(faculty, course) ? "X" : ".", 7));
            }
            System.out.println("  " + row);
        }
    }

    private void printSteps(Layout layout, FlowResult result) {
        TextUtil.printSection("Assigning the courses");
        int number = 1;
        for (AugmentingStep step : result.getSteps()) {
            System.out.println("  Step " + number + ": " + describeStep(layout, step));
            number++;
        }
        System.out.println("  No more courses can be assigned.");
    }

    /**
     * Turns one path of the network into a sentence.
     *
     * <p>A path looks like SOURCE, F1, C1, [F2, C2, ...], SINK. The first pair means "F1 takes C1".
     * Every further pair means "F2 gives up C1 and takes C2 instead", which is how a course is
     * moved to make room.</p>
     */
    String describeStep(Layout layout, AugmentingStep step) {
        List<Integer> path = step.path;
        StringBuilder text = new StringBuilder();
        Course previousCourse = null;

        for (int i = 1; i < path.size() - 1; i += 2) {
            Faculty faculty = layout.facultyAt(path.get(i));
            Course course = layout.courseAt(path.get(i + 1));
            String person = faculty.getId() + " " + faculty.getName();

            if (previousCourse == null) {
                text.append(person).append(" takes ").append(course.getCode()).append(" ").append(course.getName());
            } else {
                text.append("; ").append(person).append(" switches from ").append(previousCourse.getCode())
                        .append(" to ").append(course.getCode()).append(" ").append(course.getName());
            }
            previousCourse = course;
        }
        return text.toString();
    }

    private void printAssignments(Layout layout, FlowResult result) {
        TextUtil.printSection("Result");
        for (int i = 0; i < layout.faculty.size(); i++) {
            Faculty faculty = layout.faculty.get(i);

            List<String> taught = new ArrayList<>();
            for (int j = 0; j < layout.courses.size(); j++) {
                if (result.flowOn(layout.facultyNode(i), layout.courseNode(j)) > 0) {
                    taught.add(layout.courses.get(j).getCode() + " " + layout.courses.get(j).getName());
                }
            }
            System.out.println("  " + TextUtil.padRight(faculty.getId() + " " + faculty.getName(), 16)
                    + TextUtil.padRight(taught.size() + " course(s)", 14)
                    + (taught.isEmpty() ? "-" : join(taught, ", ")));
        }

        List<String> unassigned = unassignedCourses(layout, result);
        int total = layout.courses.size();
        System.out.println();
        System.out.println("  Courses with a teacher    : " + (total - unassigned.size()) + " of " + total);
        System.out.println("  Courses without a teacher : " + (unassigned.isEmpty() ? "none" : join(unassigned, ", ")));
    }

    /** Explains, in one sentence per course, why a course could not get a teacher. */
    private void printReasonsForUnassigned(Layout layout, FlowResult result, int maxLoad) {
        List<String> unassigned = unassignedCourses(layout, result);
        if (unassigned.isEmpty()) {
            return;
        }

        TextUtil.printSection("Why are some courses left out?");
        for (int j = 0; j < layout.courses.size(); j++) {
            if (result.flowOn(layout.courseNode(j), layout.sink) > 0) {
                continue;
            }
            Course course = layout.courses.get(j);

            List<String> eligible = new ArrayList<>();
            for (Faculty faculty : layout.faculty) {
                if (isEligible(faculty, course)) {
                    eligible.add(faculty.getId() + " " + faculty.getName());
                }
            }

            if (eligible.isEmpty()) {
                System.out.println("  " + course.getCode() + ": no faculty member belongs to the "
                        + course.getDepartment() + " department.");
            } else {
                System.out.println("  " + course.getCode() + ": " + join(eligible, " and ")
                        + (eligible.size() == 1 ? " already teaches" : " already teach")
                        + " the maximum of " + maxLoad + " course(s).");
            }
        }
    }

    private List<String> unassignedCourses(Layout layout, FlowResult result) {
        List<String> unassigned = new ArrayList<>();
        for (int j = 0; j < layout.courses.size(); j++) {
            if (result.flowOn(layout.courseNode(j), layout.sink) == 0) {
                unassigned.add(layout.courses.get(j).getCode());
            }
        }
        return unassigned;
    }

    private String join(List<String> items, String separator) {
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                text.append(separator);
            }
            text.append(items.get(i));
        }
        return text.toString();
    }
}
