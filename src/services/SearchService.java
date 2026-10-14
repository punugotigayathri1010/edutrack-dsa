package services;

import data.AssignmentData;
import data.CourseData;
import dynamicprogramming.DamerauLevenshtein;
import models.Assignment;
import models.Course;
import stringalgorithms.KMP;

/**
 * Searching for courses and assignments.
 *
 * <ul>
 *   <li>Exact / partial matches use KMP.</li>
 *   <li>When nothing matches, Damerau-Levenshtein edit distance (typos and swapped letters)
 *       is used to suggest the closest course name.</li>
 * </ul>
 */
public class SearchService {

    /** Searches course names and codes; suggests a correction when nothing matches. */
    public void searchCourses(String query) {
        boolean foundAny = false;

        for (Course course : CourseData.getAll()) {
            boolean nameMatches = !KMP.search(course.getName(), query).isEmpty();
            boolean codeMatches = !KMP.search(course.getCode(), query).isEmpty();
            if (nameMatches || codeMatches) {
                System.out.println("MATCH: " + course);
                foundAny = true;
            }
        }

        if (!foundAny) {
            suggestClosestCourse(query);
        }
    }

    /** Searches assignment titles and descriptions. */
    public void searchAssignments(String query) {
        boolean foundAny = false;

        for (Assignment assignment : AssignmentData.getAll()) {
            boolean titleMatches = !KMP.search(assignment.getTitle(), query).isEmpty();
            boolean descriptionMatches = !KMP.search(assignment.getDescription(), query).isEmpty();
            if (titleMatches || descriptionMatches) {
                System.out.println("MATCH: " + assignment);
                foundAny = true;
            }
        }

        if (!foundAny) {
            System.out.println("No assignment matched \"" + query + "\".");
        }
    }

    /** Suggests the stored course name that is closest to what was typed (handles typos and swapped letters). */
    private void suggestClosestCourse(String query) {
        String bestName = null;
        int bestDistance = Integer.MAX_VALUE;

        for (Course course : CourseData.getAll()) {
            int distance = DamerauLevenshtein.distance(query, course.getName());
            if (distance < bestDistance) {
                bestDistance = distance;
                bestName = course.getName();
            }
        }

        System.out.println("No course found. Did you mean: " + bestName + "?");
    }
}
