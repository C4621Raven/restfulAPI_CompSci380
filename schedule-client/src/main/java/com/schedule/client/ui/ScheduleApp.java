package com.schedule.client.ui;

import com.schedule.client.model.Course;
import com.schedule.client.model.CourseFilter;
import com.schedule.client.model.TermInfo;
import com.schedule.client.service.ApiException;
import com.schedule.client.service.ScheduleApiClient;

import java.util.List;
import java.util.Scanner;

/**
 * Interactive command-line application for the Schedule API.
 *
 * Run with:  mvn exec:java -Dexec.mainClass="com.schedule.client.ui.ScheduleApp"
 * Or build:  mvn package  ->  java -jar target/schedule-client-1.0.0.jar
 */
public class ScheduleApp {

    private static final ScheduleApiClient client = new ScheduleApiClient();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════╗");
        System.out.println("║      Schedule API Client v1.0        ║");
        System.out.println("║  Connecting to http://localhost:5000  ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.println();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            System.out.println();

            try {
                switch (choice) {
                    case "1" -> listTerms();
                    case "2" -> listAllCourses();
                    case "3" -> filterCourses();
                    case "4" -> getCourseBySection();
                    case "5" -> createCourse();
                    case "6" -> updateCourse();
                    case "7" -> deleteCourse();
                    case "0" -> running = false;
                    default  -> System.out.println("Invalid option. Please enter 0-7.");
                }
            } catch (ApiException e) {
                System.out.println("⚠  API Error: " + e.getMessage());
                if (e.getStatusCode() == -1) {
                    System.out.println("   Make sure the Python API server is running on localhost:5000.");
                }
            }
            System.out.println();
        }

        System.out.println("Goodbye!");
    }

    // ── Menu ──────────────────────────────────────────────────────────────────

    private static void printMenu() {
        System.out.println("─────────────────────────────────────────");
        System.out.println("  1. List all terms");
        System.out.println("  2. List all courses");
        System.out.println("  3. Filter courses");
        System.out.println("  4. Get course by section name");
        System.out.println("  5. Create new course");
        System.out.println("  6. Update a course");
        System.out.println("  7. Delete a course");
        System.out.println("  0. Exit");
        System.out.println("─────────────────────────────────────────");
        System.out.print("Choice: ");
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private static void listTerms() {
        List<TermInfo> terms = client.getTerms();
        System.out.println("Available terms (" + terms.size() + "):");
        System.out.println();
        terms.forEach(t -> System.out.println("  " + t));
    }

    private static void listAllCourses() {
        List<Course> courses = client.getCourses();
        printCourseList(courses);
    }

    private static void filterCourses() {
        System.out.println("Enter filter values (press Enter to skip any field):");
        CourseFilter filter = new CourseFilter();

        String term = prompt("  Term (e.g. 26FA, 26SU)");
        if (!term.isEmpty()) filter.term(term);

        String dept = prompt("  Department (e.g. MACS, ECBA)");
        if (!dept.isEmpty()) filter.dept(dept);

        String faculty = prompt("  Faculty name (partial match)");
        if (!faculty.isEmpty()) filter.faculty(faculty);

        String status = prompt("  Status (e.g. A)");
        if (!status.isEmpty()) filter.status(status);

        String consent = prompt("  Faculty consent required? (Y/N)");
        if (!consent.isEmpty()) filter.consent(consent);

        String limitStr = prompt("  Max results (leave blank for all)");
        if (!limitStr.isEmpty()) {
            try { filter.limit(Integer.parseInt(limitStr)); }
            catch (NumberFormatException e) { System.out.println("  (Invalid number, ignored)"); }
        }

        System.out.println();
        List<Course> courses = client.getCourses(filter);
        printCourseList(courses);
    }

    private static void getCourseBySection() {
        String secName = prompt("Section name (e.g. CS*330*A)");
        Course c = client.getCourse(secName);
        printCourseDetail(c);
    }

    private static void createCourse() {
        System.out.println("Enter details for the new course:");
        Course c = new Course();
        c.setTerm(prompt("  Term (required, e.g. 26FA)"));
        c.setSecName(prompt("  Section name (required, e.g. MATH*101*A)"));
        c.setSecShortTitle(prompt("  Short title (required)"));
        c.setSecDepts(prompt("  Department (e.g. MACS)"));
        c.setFacultyName(prompt("  Faculty name"));
        c.setInstrMethods(prompt("  Instruction method (e.g. LEC)"));
        c.setSectStatus(prompt("  Status (e.g. A)"));

        String limitStr = prompt("  Course limit (number)");
        if (!limitStr.isEmpty()) {
            try { c.setCourseLimit(Integer.parseInt(limitStr)); }
            catch (NumberFormatException ignored) {}
        }

        c.setSecMeetingRoom(prompt("  Meeting room"));
        c.setMeetingTimes(prompt("  Meeting times"));
        c.setSecFacultyConsentFlag(prompt("  Faculty consent required? (Y/N)"));
        c.setComments(prompt("  Comments / prerequisites"));

        Course created = client.createCourse(c);
        System.out.println();
        System.out.println("✓ Course created successfully:");
        printCourseDetail(created);
    }

    private static void updateCourse() {
        String secName = prompt("Section name to update (e.g. CS*330*A)");
        System.out.println("Enter new values (press Enter to leave unchanged):");

        Course updates = new Course();
        String faculty = prompt("  New faculty name");
        if (!faculty.isEmpty()) updates.setFacultyName(faculty);

        String room = prompt("  New meeting room");
        if (!room.isEmpty()) updates.setSecMeetingRoom(room);

        String times = prompt("  New meeting times");
        if (!times.isEmpty()) updates.setMeetingTimes(times);

        String status = prompt("  New status");
        if (!status.isEmpty()) updates.setSectStatus(status);

        String limitStr = prompt("  New course limit (number)");
        if (!limitStr.isEmpty()) {
            try { updates.setCourseLimit(Integer.parseInt(limitStr)); }
            catch (NumberFormatException ignored) {}
        }

        String comments = prompt("  New comments");
        if (!comments.isEmpty()) updates.setComments(comments);

        Course updated = client.updateCourse(secName, updates);
        System.out.println();
        System.out.println("✓ Course updated:");
        printCourseDetail(updated);
    }

    private static void deleteCourse() {
        String secName = prompt("Section name to delete (e.g. CS*330*A)");
        System.out.print("Are you sure? (yes/no): ");
        String confirm = scanner.nextLine().trim();
        if (confirm.equalsIgnoreCase("yes")) {
            String msg = client.deleteCourse(secName);
            System.out.println("✓ " + msg);
        } else {
            System.out.println("Cancelled.");
        }
    }

    // ── Display helpers ───────────────────────────────────────────────────────

    private static void printCourseList(List<Course> courses) {
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }
        System.out.printf("Found %d course(s):%n%n", courses.size());
        System.out.printf("%-12s %-20s %-35s %-20s %s%n",
                "Term", "Section", "Title", "Faculty", "Times");
        System.out.println("-".repeat(100));
        for (Course c : courses) {
            System.out.printf("%-12s %-20s %-35s %-20s %s%n",
                    nullSafe(c.getTerm()),
                    nullSafe(c.getSecName()),
                    truncate(nullSafe(c.getSecShortTitle()), 34),
                    truncate(nullSafe(c.getFacultyName()), 19),
                    nullSafe(c.getMeetingTimes()));
        }
    }

    private static void printCourseDetail(Course c) {
        System.out.println();
        System.out.println("  Section   : " + nullSafe(c.getSecName()));
        System.out.println("  Title     : " + nullSafe(c.getSecShortTitle()));
        System.out.println("  Term      : " + nullSafe(c.getTerm()));
        System.out.println("  Department: " + nullSafe(c.getSecDepts()));
        System.out.println("  Faculty   : " + nullSafe(c.getFacultyName()));
        System.out.println("  Status    : " + nullSafe(c.getSectStatus()));
        System.out.println("  Method    : " + nullSafe(c.getInstrMethods()));
        System.out.println("  Consent   : " + nullSafe(c.getSecFacultyConsentFlag()));
        System.out.println("  Limit     : " + (c.getCourseLimit() != null ? c.getCourseLimit() : "N/A"));
        System.out.println("  Room      : " + nullSafe(c.getSecMeetingRoom()));
        System.out.println("  Times     : " + nullSafe(c.getMeetingTimes()));
        System.out.println("  Types     : " + nullSafe(c.getSecCourseTypes()));
        System.out.println("  Comments  : " + nullSafe(c.getComments()));
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private static String prompt(String label) {
        System.out.print(label + ": ");
        return scanner.nextLine().trim();
    }

    private static String nullSafe(String s) {
        return s != null ? s : "";
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}
