package com.godaddy.sonar.ruby.metricfu;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class FlayMatchTest {

    private HashMap<String, Object> identical;
    private HashMap<String, Object> similar;

    @Before
    public void setUp() throws Exception {
        identical = new HashMap<String, Object>() {{
            put(":reason", "2) IDENTICAL code found in :if (mass*2 = 64)");
            put(":matches", new ArrayList() {{
                add(makeMatch("foo", "3"));
                add(makeMatch("bar", "7"));
            }});
        }};
        similar = new HashMap<String, Object>() {{
            put(":reason", "1) Similar code found in :module (mass = 152)");
            put(":matches", new ArrayList() {{
                add(makeMatch("baz", "8"));
                add(makeMatch("quux", "19"));
            }});
        }};
    }

    @Test
    public void testForIdenticalDuplication() throws Exception {
        FlayMatch actual = FlayMatch.fromMap(identical);
        assertTrue(actual.isIdentical);
    }

    @Test
    public void testForSimilarDuplication() throws Exception {
        FlayMatch actual = FlayMatch.fromMap(similar);
        assertFalse(actual.isIdentical);
    }

    @Test
    public void testParsesMass() {
        assertEquals("152", FlayMatch.fromMap(similar).mass);
    }

    @Test
    public void testParsesMatches() {
        assertEquals(2, FlayMatch.fromMap(similar).matches.size());
    }

    private Map<String, Object> makeMatch(final String name, final String line) {
        return new HashMap<String, Object>() {{
            put(":name", name);
            put(":line", line);
        }};
    }

}