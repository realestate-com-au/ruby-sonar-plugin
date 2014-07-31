package com.godaddy.sonar.ruby.metricfu;

import org.apache.commons.io.FileUtils;
import org.sonar.api.scan.filesystem.ModuleFileSystem;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetricfuDuplicationYamlParserImpl implements MetricfuDuplicationYamlParser
{

    private final ModuleFileSystem filesystem;

    public MetricfuDuplicationYamlParserImpl(ModuleFileSystem fs) {
        filesystem = fs;
    }

    @SuppressWarnings("unchecked")
    public FlayDuplicationResults parseResults(File resultsFile) throws IOException {

        String fileString = FileUtils.readFileToString(resultsFile, "UTF-8");

        Map results = new Yaml().loadAs(fileString, Map.class);
        Map flay = (Map) results.get(":flay");
        List<Map> matches = (List) flay.get(":matches");

        List<FlayDuplication> duplications = new ArrayList<FlayDuplication>();
        for (Map match : matches) {
            FlayMatch flayMatch = FlayMatch.fromMap(match);
            if (flayMatch.isIdentical) {
                duplications.addAll(flayMatch.matches);
            }
        }
        return new FlayDuplicationResults(filesystem, duplicatedLinesByFile(duplications), duplicatedBlocksByFile(duplications));
    }

    private Map<String, Integer> duplicatedLinesByFile(List<FlayDuplication> duplications) {
        Map<String, Integer> map = new HashMap<>();

        for (FlayDuplication duplication : duplications) {
            Integer count = map.get(duplication.name);
            if (count == null) {
                count = 0;
            }
            count += lengthFromMass(Integer.valueOf(duplication.mass));
            map.put(duplication.name, count);
        }
        return map;
    }

    private Map<String, List<FlayDuplicateBlock>> duplicatedBlocksByFile(List<FlayDuplication> duplications) {
        Map<String, List<FlayDuplicateBlock>> map = new HashMap<>();

        for (FlayDuplication duplication : duplications) {
            List<FlayDuplicateBlock> blocks = map.get(duplication.name);
            if (blocks == null) {
                blocks = new ArrayList<>();
            }
            blocks.add(FlayDuplicateBlock.fromDuplication(duplication));
            map.put(duplication.name, blocks);
        }
        return map;
    }

    private Integer lengthFromMass(int mass) {
    /*
     * Just a SWAG to derive a duplication "length" from flay's "mass", which is more or less the number of sexprs:
     *
     *  def mass
     *    @mass ||= self.structure.flatten.size
     *  end
     *
     *  https://github.com/seattlerb/sexp_processor/blob/master/lib/sexp.rb#L201
     */
        return mass / 8;
    }
}


