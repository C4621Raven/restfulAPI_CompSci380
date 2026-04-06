package com.schedule.client.service;

import com.schedule.client.model.CourseFilter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CourseFilterTest {

    @Test
    void emptyFilterProducesNoQueryString() {
        assertEquals("", new CourseFilter().toQueryString());
    }

    @Test
    void singleParam() {
        String qs = new CourseFilter().term("26FA").toQueryString();
        assertEquals("?term=26FA", qs);
    }

    @Test
    void multipleParams() {
        String qs = new CourseFilter().term("26FA").dept("MACS").limit(10).toQueryString();
        assertTrue(qs.contains("term=26FA"));
        assertTrue(qs.contains("dept=MACS"));
        assertTrue(qs.contains("limit=10"));
    }

    @Test
    void spacesInFacultyNameAreEncoded() {
        String qs = new CourseFilter().faculty("John Smith").toQueryString();
        assertTrue(qs.contains("faculty=John+Smith") || qs.contains("faculty=John%20Smith"));
    }
}
