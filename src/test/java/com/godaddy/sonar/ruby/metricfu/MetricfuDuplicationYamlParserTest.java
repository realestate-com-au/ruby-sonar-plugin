package com.godaddy.sonar.ruby.metricfu;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class MetricfuDuplicationYamlParserTest {

    private MetricfuDuplicationYamlParserImpl parser;
    private Path file;
    private File reportFile;

    @Before
    public void setUp() throws Exception
    {
        parser = new MetricfuDuplicationYamlParserImpl();
        reportFile = new File("src/test/resources/test-data/flay-results.yml");
        file = new File("lib/prices/project_profile_prices/project_profile_price_generator.rb").toPath();
    }

    @Test
    public void testParseResults() throws Exception {
        FlayDuplicationResults results = parser.parseResults(reportFile);
        assertEquals(1, results.getDuplicatedBlocksFor(file).size());
        assertEquals(13, (int) results.getDuplicatedLineCountFor(file));
    }
}