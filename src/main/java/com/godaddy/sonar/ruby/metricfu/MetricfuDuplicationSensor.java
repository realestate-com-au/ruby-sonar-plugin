package com.godaddy.sonar.ruby.metricfu;

import com.godaddy.sonar.ruby.core.Ruby;
import com.godaddy.sonar.ruby.core.RubyFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class MetricfuDuplicationSensor implements Sensor
{
    private static final Logger LOG = LoggerFactory.getLogger(MetricfuComplexitySensor.class);

    public static final String METRIC_FU_REPORT_FILENAME = "tmp/metric_fu/report.yml";


    private final MetricfuDuplicationYamlParser metricfuDuplicationYamlParser;
    private final ModuleFileSystem moduleFileSystem;

    public MetricfuDuplicationSensor(ModuleFileSystem moduleFileSystem, MetricfuDuplicationYamlParser metricfuDuplicationYamlParser)
    {
        this.moduleFileSystem = moduleFileSystem;
        this.metricfuDuplicationYamlParser = metricfuDuplicationYamlParser;
    }

    public boolean shouldExecuteOnProject(Project project)
    {
        return Ruby.KEY.equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context) {

        FlayToSonarFilePathMapper mapper = new FlayToSonarFilePathMapper(moduleFileSystem);

        try {
            List<File> rubyFilesInProject = moduleFileSystem.files(FileQuery.onSource().onLanguage(project.getLanguageKey()));

            File resultsFile = new File(moduleFileSystem.baseDir(), METRIC_FU_REPORT_FILENAME);
            FlayDuplicationResults flayDuplicationResults = metricfuDuplicationYamlParser.parseResults(resultsFile);

            for (File file : rubyFilesInProject) {
                Path relativePath = mapper.findRelativePath(file);
                RubyFile resource = new RubyFile(file, moduleFileSystem.sourceDirs());

                LOG.debug("analyzing file: " + file.getName());

                if (flayDuplicationResults.hasResultsFor(relativePath)) {
                    int duplicatedLines = flayDuplicationResults.getDuplicatedLineCountFor(relativePath);
                    LOG.debug(String.format("Saving duplicated lines metric: %s", duplicatedLines));
                    context.saveMeasure(resource, new Measure(CoreMetrics.DUPLICATED_LINES, Double.valueOf(duplicatedLines)));

                    List<FlayDuplicateBlock> duplicatedBlocks = flayDuplicationResults.getDuplicatedBlocksFor(relativePath);
                    LOG.debug(String.format("Saving duplicated blocks metric: %s", duplicatedBlocks.size()));
                    context.saveMeasure(resource, new Measure(CoreMetrics.DUPLICATED_BLOCKS, Double.valueOf(duplicatedBlocks.size())));
                }
            }
        } catch (IOException e) {
            LOG.error("Cannot parse MetricFu duplications results: ", e);
        }
    }
}
