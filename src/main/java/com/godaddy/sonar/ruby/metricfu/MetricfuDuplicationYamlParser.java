package com.godaddy.sonar.ruby.metricfu;

import org.sonar.api.BatchExtension;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface MetricfuDuplicationYamlParser extends BatchExtension
{
    FlayDuplicationResults parseResults(File resultsFile) throws IOException;
}
