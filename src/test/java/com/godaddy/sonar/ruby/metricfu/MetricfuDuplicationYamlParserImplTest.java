package com.godaddy.sonar.ruby.metricfu;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class MetricfuDuplicationYamlParserImplTest {

    private final static String YML_FILE_NAME = "src/test/resources/test-data/flay-results.yml";

    private MetricfuDuplicationYamlParserImpl parser = null;

    @Before
    public void setUp() throws Exception
    {
//        parser = new MetricfuDuplicationYamlParserImpl();
    }

//    @Test
//    public void testParseResults() throws Exception {
//        File reportFile = new File(YML_FILE_NAME);
//        FlayDuplicationResults duplications = parser.parseResults(reportFile);
//        assertEquals(2, duplications.getDuplicatedLineCountFor());
//
//        FlayDuplication duplication = duplications.get(0);
//        assertEquals("lib/prices/project_profile_prices/project_profile_price_generator.rb", duplication.name);
//        assertEquals("11", duplication.line);
//        assertEquals("104", duplication.mass);
//    }
}