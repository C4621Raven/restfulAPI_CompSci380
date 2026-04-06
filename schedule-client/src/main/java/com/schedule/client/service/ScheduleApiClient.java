package com.schedule.client.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.schedule.client.model.Course;
import com.schedule.client.model.CourseFilter;
import com.schedule.client.model.TermInfo;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.Closeable;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for the Schedule RESTful API running on localhost:5000.
 *
 * Implements Closeable so it can be used in try-with-resources blocks.
 *
 * All methods throw {@link ApiException} on HTTP errors or connectivity problems.
 */
public class ScheduleApiClient implements Closeable {

    private static final String BASE_URL = "http://localhost:5000/api";

    private final CloseableHttpClient http;
    private final ObjectMapper mapper;

    public ScheduleApiClient() {
        this.http   = HttpClients.createDefault();
        this.mapper = new ObjectMapper();
    }

    // ── Terms ─────────────────────────────────────────────────────────────────

    /**
     * GET /api/terms
     * Returns all terms and their course counts.
     */
    public List<TermInfo> getTerms() {
        String json = get(BASE_URL + "/terms");
        try {
            JsonNode root = mapper.readTree(json);
            return mapper.convertValue(root.get("terms"),
                    new TypeReference<List<TermInfo>>() {});
        } catch (IOException e) {
            throw new ApiException("Failed to parse terms response", e);
        }
    }

    // ── Courses ───────────────────────────────────────────────────────────────

    /**
     * GET /api/courses
     * Returns all courses with no filtering.
     */
    public List<Course> getCourses() {
        return getCourses(new CourseFilter());
    }

    /**
     * GET /api/courses?{filter params}
     * Returns courses matching the supplied filter.
     */
    public List<Course> getCourses(CourseFilter filter) {
        String url  = BASE_URL + "/courses" + filter.toQueryString();
        String json = get(url);
        try {
            JsonNode root = mapper.readTree(json);
            return mapper.convertValue(root.get("courses"),
                    new TypeReference<List<Course>>() {});
        } catch (IOException e) {
            throw new ApiException("Failed to parse courses response", e);
        }
    }

    /**
     * GET /api/courses/{secName}
     * Returns a single course by its section name (e.g. "CS*330*A").
     */
    public Course getCourse(String secName) {
        String url  = BASE_URL + "/courses/" + encodePath(secName);
        String json = get(url);
        try {
            return mapper.readValue(json, Course.class);
        } catch (IOException e) {
            throw new ApiException("Failed to parse course response", e);
        }
    }

    /**
     * POST /api/courses
     * Creates a new course section.
     *
     * @param course  Must have term, secName, and secShortTitle set at minimum.
     * @return        The created course as returned by the API.
     */
    public Course createCourse(Course course) {
        try {
            // Build a flat map matching the API's snake_case field names
            Map<String, Object> body = courseToMap(course);
            String requestJson  = mapper.writeValueAsString(body);
            String responseJson = post(BASE_URL + "/courses", requestJson);
            JsonNode root = mapper.readTree(responseJson);
            return mapper.convertValue(root.get("course"), Course.class);
        } catch (IOException e) {
            throw new ApiException("Failed to create course", e);
        }
    }

    /**
     * PUT /api/courses/{secName}
     * Updates fields on an existing course. Only non-null fields in {@code updates} are sent.
     *
     * @param secName  Section name identifying the course to update.
     * @param updates  A Course whose non-null fields will be applied.
     * @return         The updated course as returned by the API.
     */
    public Course updateCourse(String secName, Course updates) {
        try {
            Map<String, Object> body = courseToMap(updates);
            // Remove null values so the API only sees fields we want to change
            body.values().removeIf(v -> v == null);
            String requestJson  = mapper.writeValueAsString(body);
            String responseJson = put(BASE_URL + "/courses/" + encodePath(secName), requestJson);
            JsonNode root = mapper.readTree(responseJson);
            return mapper.convertValue(root.get("course"), Course.class);
        } catch (IOException e) {
            throw new ApiException("Failed to update course", e);
        }
    }

    /**
     * DELETE /api/courses/{secName}
     * Deletes a course section.
     *
     * @return The API's confirmation message.
     */
    public String deleteCourse(String secName) {
        String url  = BASE_URL + "/courses/" + encodePath(secName);
        String json = delete(url);
        try {
            JsonNode root = mapper.readTree(json);
            return root.get("message").asText();
        } catch (IOException e) {
            throw new ApiException("Failed to parse delete response", e);
        }
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private String get(String url) {
        HttpGet request = new HttpGet(url);
        return execute(request);
    }

    private String post(String url, String jsonBody) {
        HttpPost request = new HttpPost(url);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        return execute(request);
    }

    private String put(String url, String jsonBody) {
        HttpPut request = new HttpPut(url);
        request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        return execute(request);
    }

    private String delete(String url) {
        HttpDelete request = new HttpDelete(url);
        return execute(request);
    }

    private String execute(org.apache.hc.core5.http.ClassicHttpRequest request) {
        try {
            return http.execute(request, response -> {
                int status = response.getCode();
                String body = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                if (status < 200 || status >= 300) {
                    String msg = extractApiMessage(body, status);
                    throw new ApiException(msg, status);
                }
                return body;
            });
        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            throw new ApiException("Could not connect to API at " + BASE_URL
                    + ". Is the server running?", e);
        }
    }

    // ── Utilities ─────────────────────────────────────────────────────────────

    /** URL-encode a path segment (encodes * and other special chars). */
    private static String encodePath(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20");   // spaces as %20 not +
    }

    /** Extract the API's "message" field from an error JSON body, or fall back to raw body. */
    private String extractApiMessage(String body, int status) {
        try {
            JsonNode root = new ObjectMapper().readTree(body);
            if (root.has("message")) return "HTTP " + status + ": " + root.get("message").asText();
        } catch (Exception ignored) {}
        return "HTTP " + status + ": " + body;
    }

    /** Convert a Course object to the snake_case map the API expects. */
    private Map<String, Object> courseToMap(Course c) {
        return new java.util.LinkedHashMap<>(Map.ofEntries(
                Map.entry("term",                       c.getTerm()),
                Map.entry("sec_depts",                  c.getSecDepts()),
                Map.entry("sec_name",                   c.getSecName()),
                Map.entry("sec_short_title",             c.getSecShortTitle()),
                Map.entry("sect_status",                c.getSectStatus()),
                Map.entry("faculty_name",               c.getFacultyName()),
                Map.entry("instr_methods",              c.getInstrMethods()),
                Map.entry("sec_faculty_consent_flag",   c.getSecFacultyConsentFlag()),
                Map.entry("course_limit",               c.getCourseLimit()),
                Map.entry("sec_meeting_room",           c.getSecMeetingRoom()),
                Map.entry("meeting_times",              c.getMeetingTimes()),
                Map.entry("sec_course_types",           c.getSecCourseTypes()),
                Map.entry("xsec_reqs_printed_comments", c.getComments())
        ));
    }

    @Override
    public void close() throws IOException {
        http.close();
    }
}
