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

        try {
            List<File> rubyFilesInProject = moduleFileSystem.files(FileQuery.onSource().onLanguage(project.getLanguageKey()));

            for (File file : rubyFilesInProject) {
                LOG.debug("analyzing functions for classes in the file: " + file.getName());
                File resultsFile = new File(moduleFileSystem.baseDir(), METRIC_FU_REPORT_FILENAME);

                RubyFile resource = new RubyFile(file, moduleFileSystem.sourceDirs());
                LOG.debug("found sonar resourceKey: " + resource);

                FlayDuplicationResults flayDuplicationResults = metricfuDuplicationYamlParser.parseResults(resultsFile);

                Integer duplicatedLines = flayDuplicationResults.getDuplicatedLineCountFor(file);
                // FIXME get should never return null
                if (duplicatedLines != null) {
                    LOG.debug(String.format("Saving duplication metric for %s: %s", resource.getKey(), duplicatedLines));
                    context.saveMeasure(resource, new Measure(CoreMetrics.DUPLICATED_LINES, duplicatedLines.doubleValue()));
                }

                List<FlayDuplicateBlock> duplicatedBlocks = flayDuplicationResults.getDuplicatedBlocksFor(file);
                // FIXME get should never return null
                if (duplicatedBlocks != null) {
                    LOG.debug(String.format("Saving duplication metric for %s: %s", resource.getKey(), duplicatedBlocks));
                    context.saveMeasure(resource, new Measure(CoreMetrics.DUPLICATED_BLOCKS, Double.valueOf(duplicatedBlocks.size())));
//
//                    StringBuilder blocksXML = new StringBuilder();
//                    for (FlayDuplicateBlock block : duplicatedBlocks) {
//                        blocksXML.append(block.asXML(resource.getKey()));
//                    }
//                    LOG.debug(String.format("Saving duplicate block data for %s: %s", resource.getKey(), blocksXML));
//                    context.saveMeasure(resource, new Measure(CoreMetrics.DUPLICATIONS_DATA, makeXML(blocksXML.toString())));
                }
            }
        } catch (IOException e) {
            LOG.error("Cannot parse MetricFu duplications results: ", e);
        }
    }

    private String makeXML(String blocks) {
        return String.format("<duplications><g>%s</g></duplications>", blocks);
    }

}
