package com.godaddy.sonar.ruby.simplecovrcov;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import com.godaddy.sonar.ruby.core.Ruby;

public class SimpleCovRcovSensor implements Sensor 
{
    private static final Logger LOG = LoggerFactory.getLogger(SimpleCovRcovSensor.class);

    private SimpleCovRcovJsonParser simpleCovRcovJsonParser;
    private ModuleFileSystem moduleFileSystem;

    /**
     * Use of IoC to get Settings
     */
    public SimpleCovRcovSensor(ModuleFileSystem moduleFileSystem, SimpleCovRcovJsonParser simpleCovRcovJsonParser) 
    {
        this.moduleFileSystem = moduleFileSystem;
        this.simpleCovRcovJsonParser = simpleCovRcovJsonParser;
    }

    public boolean shouldExecuteOnProject(Project project) 
    {
        return Ruby.KEY.equals(project.getLanguageKey());
    }

    public void analyse(Project project, SensorContext context) 
    {
        File jsonFile = new File("coverage/.resultset.json");

        List<File> sourceDirs = moduleFileSystem.sourceDirs();

        try 
        {
            calculateMetrics(sourceDirs, jsonFile, context);
        } 
        catch (IOException e) 
        {
            LOG.error("unable to calculate Metrics");
        }
    }

    private void calculateMetrics(List<File> sourceDirs, File jsonFile, final SensorContext context) throws IOException 
    {
        Map<String, CoverageMeasuresBuilder> jsonResults = simpleCovRcovJsonParser.parse(jsonFile);

        File sourceFile = null;
        for (Entry<String, CoverageMeasuresBuilder> entry : jsonResults.entrySet()) 
        {
            try 
            {
                String fileName = entry.getKey();
                sourceFile = new File(fileName);
                org.sonar.api.resources.File resource = org.sonar.api.resources.File.fromIOFile(sourceFile, sourceDirs);

                CoverageMeasuresBuilder fileCoverage = entry.getValue();
                if (fileCoverage != null) 
                {
                    for (Measure measure : fileCoverage.createMeasures()) 
                    {
                        context.saveMeasure(resource, measure);
                    }
                }
            } 
            catch (Exception e) 
            {
                if (sourceFile != null) 
                {
                    LOG.error("Unable to save metrics for file: " + sourceFile.getName(), e);
                } 
                else
                {
                    LOG.error("Unable to save metrics.", e);
                }
            }
        }
    }
}
