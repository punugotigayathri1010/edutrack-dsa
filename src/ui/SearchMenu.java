package ui;

import java.util.Scanner;

import services.SearchService;

/** Stand-alone keyword search menu. */
public class SearchMenu {

    public void show(Scanner scanner) {
        String keyword = InputHelper.readLine(scanner, "Enter academic keyword: ");
        new SearchService().searchCourses(keyword);
    }
}
