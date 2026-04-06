package com.schedule.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a term entry returned by GET /api/terms.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TermInfo {

    @JsonProperty("term")
    private String term;

    @JsonProperty("course_count")
    private int courseCount;

    public String getTerm()      { return term; }
    public int getCourseCount()  { return courseCount; }

    public void setTerm(String term)          { this.term = term; }
    public void setCourseCount(int count)     { this.courseCount = count; }

    @Override
    public String toString() {
        return String.format("%-10s  %d courses", term, courseCount);
    }
}
