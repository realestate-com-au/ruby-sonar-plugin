package com.godaddy.sonar.ruby.metricfu;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;
import org.apache.commons.lang.StringUtils;

public class MetricfuComplexityYamlParserImpl implements MetricfuComplexityYamlParser
{
    @SuppressWarnings("unchecked")
    public List<RubyFunction> parseFunctions(String fileNameFromModule, File resultsFile) throws IOException
    {
        List<RubyFunction> rubyFunctionsForFile = new ArrayList<RubyFunction>();

        String fileString = FileUtils.readFileToString(resultsFile, "UTF-8");

        // remove ":hotspots:" section of the yaml so snakeyaml can parse it
        // correctly, snakeyaml throws an error with that section intact
        // Will remove if metric_fu metric filtering works for hotspots in the
        // future
        int hotSpotIndex = fileString.indexOf(":hotspots:");
        if (hotSpotIndex >= 0)
        {
            String stringToRemove = fileString.substring(hotSpotIndex, fileString.length());
            fileString = StringUtils.remove(fileString, stringToRemove);
        }

        // change 'name: !binary' to 'name: tag:yaml.org,2002:binary'
        // normally this !binary tags will be on the metric_fu analysis of .erb files as 'class_name: !binary' and 'name: !binary'
        fileString = StringUtils.replace(fileString, "name: !binary", "name: tag:yaml.org,2002:binary");

        Yaml yaml = new Yaml();

        Map<String, Object> metricfuResult = (Map<String, Object>) yaml.loadAs(fileString, Map.class);
        Map<String, Object> saikuroResult = (Map<String, Object>) metricfuResult.get(":saikuro");
        ArrayList<Map<String, Object>> saikuroFilesResult = (ArrayList<Map<String, Object>>) saikuroResult.get(":files");

        Map<String, Object> fileInfoToWorkWith = new HashMap<String, Object>();
        for (Map<String, Object> fileInfo : saikuroFilesResult)
        {
            String fileNameFromResults = (String) fileInfo.get(":filename");

            if (fileNameFromResults.contains(fileNameFromModule))
            {
                fileInfoToWorkWith = fileInfo;
                break;
            }
        }

        if (fileInfoToWorkWith.size() == 0)
        {
            // file has no methods returning empty function list
            return new ArrayList<RubyFunction>();
        }

        ArrayList<Map<String, Object>> classesInfo = (ArrayList<Map<String, Object>>) fileInfoToWorkWith.get(":classes");

        for (Map<String, Object> classInfo : classesInfo)
        {
            ArrayList<Map<String, Object>> methods = (ArrayList<Map<String, Object>>) classInfo.get(":methods");

            for (Map<String, Object> method : methods)
            {
                RubyFunction rubyFunction = new RubyFunction();
                rubyFunction.setName((String) method.get(":name"));
                rubyFunction.setComplexity((Integer) method.get(":complexity"));
                rubyFunction.setLine((Integer) method.get(":lines"));

                rubyFunctionsForFile.add(rubyFunction);
            }
        }
        return rubyFunctionsForFile;
    }
}
