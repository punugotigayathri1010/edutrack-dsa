package services;

import data.CourseData;
import models.Course;
import util.TextUtil;

/** Business logic for courses. */
public class CourseService {

    /** Prints all courses as a table. */
    public void listCourses() {
        System.out.println();
        System.out.println(TextUtil.padRight("Code", 8) + TextUtil.padRight("Course", 24)
                + TextUtil.padRight("Dept", 7) + "Credits");
        System.out.println(TextUtil.repeat('-', 48));
        for (Course course : CourseData.getAll()) {
            System.out.println(TextUtil.padRight(course.getCode(), 8)
                    + TextUtil.padRight(course.getName(), 24)
                    + TextUtil.padRight(course.getDepartment(), 7)
                    + course.getCredits());
        }
    }
}
