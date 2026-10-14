package services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import data.MarkData;
import data.StudentData;
import models.Mark;
import models.Student;
import randomizedparallel.RandomizedQuickSort;
import randomizedparallel.ReservoirSampling;
import util.TextUtil;

/**
 * Analytics for CO6 (randomized and parallel algorithms).
 *
 * <ul>
 *   <li><b>Merit ranking</b> - Randomized QuickSort orders students by total marks.</li>
 *   <li><b>Random check of activity</b> - Reservoir Sampling picks a fair sample from a long stream of events.</li>
 * </ul>
 */
public class AnalyticsService {

    public static final int MAX_SAMPLE = 20;
    private static final int STREAM_LENGTH = 5000;

    private static final String[] ACTIONS = {
        "searched a course", "opened an assignment", "ran document analysis",
        "viewed the profile", "viewed the exam timetable", "searched an assignment"
    };

    private final Random random = new Random();

    /** A student with the total of all marks. */
    private static final class Total {
        final Student student;
        final int sum;
        final int subjects;

        Total(Student student, int sum, int subjects) {
            this.student = student;
            this.sum = sum;
            this.subjects = subjects;
        }
    }

    /** Ranks students by total marks, highest first; equal totals share a rank. */
    public void rankStudents() {
        TextUtil.printBanner("STUDENT MERIT RANKING");
        System.out.println("A random pivot keeps the sort fast for every input, even one that is already ordered.");

        List<Total> totals = new ArrayList<>();
        for (Student student : StudentData.getAll()) {
            int sum = 0;
            int subjects = 0;
            for (Mark mark : MarkData.getAll()) {
                if (mark.getStudentId().equalsIgnoreCase(student.getId())) {
                    sum += mark.getMarks();
                    subjects++;
                }
            }
            totals.add(new Total(student, sum, subjects));
        }

        RandomizedQuickSort.sort(totals, new Comparator<Total>() {
            @Override
            public int compare(Total a, Total b) {
                return Integer.compare(b.sum, a.sum);
            }
        }, random);

        System.out.println();
        System.out.println(TextUtil.padRight("Rank", 6) + TextUtil.padRight("ID", 7) + TextUtil.padRight("Name", 12)
                + TextUtil.padRight("Subjects", 10) + TextUtil.padRight("Total", 8) + "Average");
        System.out.println(TextUtil.repeat('-', 52));
        int rank = 0;
        int previous = -1;
        for (int i = 0; i < totals.size(); i++) {
            Total total = totals.get(i);
            if (i == 0 || total.sum != previous) {
                rank = i + 1;
            }
            previous = total.sum;
            String average = total.subjects == 0 ? "-" : String.format("%.1f", (double) total.sum / total.subjects);
            System.out.println(TextUtil.padRight(String.valueOf(rank), 6)
                    + TextUtil.padRight(total.student.getId(), 7)
                    + TextUtil.padRight(total.student.getName(), 12)
                    + TextUtil.padRight(String.valueOf(total.subjects), 10)
                    + TextUtil.padRight(String.valueOf(total.sum), 8) + average);
        }
    }

    /** Picks {@code sampleSize} events from a long simulated activity log in a single pass. */
    public void sampleActivity(int sampleSize) {
        TextUtil.printBanner("RANDOM CHECK OF RECENT ACTIVITY");
        System.out.println("A stream of " + STREAM_LENGTH + " events goes past once. Only " + sampleSize
                + " are ever kept in memory,");
        System.out.println("yet every event had the same chance (" + sampleSize + "/" + STREAM_LENGTH + ") of being picked.");

        final long seed = random.nextLong();
        Iterable<String> stream = new Iterable<String>() {
            @Override
            public Iterator<String> iterator() {
                final Random events = new Random(seed);
                return new Iterator<String>() {
                    private int next = 1;

                    @Override
                    public boolean hasNext() {
                        return next <= STREAM_LENGTH;
                    }

                    @Override
                    public String next() {
                        List<Student> students = StudentData.getAll();
                        String who = students.isEmpty() ? "?" : students.get(events.nextInt(students.size())).getId();
                        return "event " + String.format("%04d", next++) + ": " + who + " "
                                + ACTIONS[events.nextInt(ACTIONS.length)];
                    }
                };
            }
        };

        List<String> sample = ReservoirSampling.sample(stream, sampleSize, random);
        TextUtil.printSection("Sample");
        for (String event : sample) {
            System.out.println("  " + event);
        }
    }
}
