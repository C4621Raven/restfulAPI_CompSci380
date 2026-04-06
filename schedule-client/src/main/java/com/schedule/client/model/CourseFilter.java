package com.schedule.client.model;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Builder for the optional query parameters supported by GET /api/courses.
 *
 * Usage:
 *   CourseFilter filter = new CourseFilter()
 *       .term("26FA")
 *       .dept("MACS")
 *       .limit(10);
 *   List<Course> courses = client.getCourses(filter);
 */
public class CourseFilter {

    private String term;
    private String dept;
    private String faculty;
    private String status;
    private String room;
    private String consent;
    private Integer limit;
    private Integer offset;

    public CourseFilter term(String term)       { this.term    = term;    return this; }
    public CourseFilter dept(String dept)       { this.dept    = dept;    return this; }
    public CourseFilter faculty(String faculty) { this.faculty = faculty; return this; }
    public CourseFilter status(String status)   { this.status  = status;  return this; }
    public CourseFilter room(String room)       { this.room    = room;    return this; }
    public CourseFilter consent(String consent) { this.consent = consent; return this; }
    public CourseFilter limit(int limit)        { this.limit   = limit;   return this; }
    public CourseFilter offset(int offset)      { this.offset  = offset;  return this; }

    /** Build the query string (e.g. "?term=26FA&dept=MACS"). */
    public String toQueryString() {
        List<String> params = new ArrayList<>();
        if (term    != null) params.add("term="    + encode(term));
        if (dept    != null) params.add("dept="    + encode(dept));
        if (faculty != null) params.add("faculty=" + encode(faculty));
        if (status  != null) params.add("status="  + encode(status));
        if (room    != null) params.add("room="    + encode(room));
        if (consent != null) params.add("consent=" + encode(consent));
        if (limit   != null) params.add("limit="   + limit);
        if (offset  != null) params.add("offset="  + offset);
        return params.isEmpty() ? "" : "?" + String.join("&", params);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
