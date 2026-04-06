package com.schedule.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single course section returned by the Schedule API.
 * Field names match the snake_case JSON keys produced by the Python API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Course {

    @JsonProperty("term")
    private String term;

    @JsonProperty("sec_depts")
    private String secDepts;

    @JsonProperty("sec_name")
    private String secName;

    @JsonProperty("sec_short_title")
    private String secShortTitle;

    @JsonProperty("sect_status")
    private String sectStatus;

    @JsonProperty("faculty_name")
    private String facultyName;

    @JsonProperty("instr_methods")
    private String instrMethods;

    @JsonProperty("sec_faculty_consent_flag")
    private String secFacultyConsentFlag;

    @JsonProperty("course_limit")
    private Integer courseLimit;

    @JsonProperty("sec_meeting_room")
    private String secMeetingRoom;

    @JsonProperty("meeting_times")
    private String meetingTimes;

    @JsonProperty("sec_course_types")
    private String secCourseTypes;

    @JsonProperty("xsec_reqs_printed_comments")
    private String comments;

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getTerm()                  { return term; }
    public String getSecDepts()              { return secDepts; }
    public String getSecName()               { return secName; }
    public String getSecShortTitle()         { return secShortTitle; }
    public String getSectStatus()            { return sectStatus; }
    public String getFacultyName()           { return facultyName; }
    public String getInstrMethods()          { return instrMethods; }
    public String getSecFacultyConsentFlag() { return secFacultyConsentFlag; }
    public Integer getCourseLimit()          { return courseLimit; }
    public String getSecMeetingRoom()        { return secMeetingRoom; }
    public String getMeetingTimes()          { return meetingTimes; }
    public String getSecCourseTypes()        { return secCourseTypes; }
    public String getComments()              { return comments; }

    // ── Setters ──────────────────────────────────────────────────────────────

    public void setTerm(String term)                                { this.term = term; }
    public void setSecDepts(String secDepts)                        { this.secDepts = secDepts; }
    public void setSecName(String secName)                          { this.secName = secName; }
    public void setSecShortTitle(String secShortTitle)              { this.secShortTitle = secShortTitle; }
    public void setSectStatus(String sectStatus)                    { this.sectStatus = sectStatus; }
    public void setFacultyName(String facultyName)                  { this.facultyName = facultyName; }
    public void setInstrMethods(String instrMethods)                { this.instrMethods = instrMethods; }
    public void setSecFacultyConsentFlag(String flag)               { this.secFacultyConsentFlag = flag; }
    public void setCourseLimit(Integer courseLimit)                  { this.courseLimit = courseLimit; }
    public void setSecMeetingRoom(String secMeetingRoom)            { this.secMeetingRoom = secMeetingRoom; }
    public void setMeetingTimes(String meetingTimes)                { this.meetingTimes = meetingTimes; }
    public void setSecCourseTypes(String secCourseTypes)            { this.secCourseTypes = secCourseTypes; }
    public void setComments(String comments)                        { this.comments = comments; }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s | Faculty: %s | Room: %s | Times: %s",
                term, secName, secShortTitle, facultyName, secMeetingRoom, meetingTimes);
    }
}
