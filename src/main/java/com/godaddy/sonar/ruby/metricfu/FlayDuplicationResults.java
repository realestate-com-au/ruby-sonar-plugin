package com.godaddy.sonar.ruby.metricfu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlayDuplicationResults
{
    private final Map<String, Integer> lineCounts;
    private final Map<String, List<FlayDuplicateBlock>> blocks;

    public FlayDuplicationResults(Map<String, Integer> lineCounts, Map<String, List<FlayDuplicateBlock>> blocks) {
        this.lineCounts = new HashMap<>();
        this.lineCounts.putAll(lineCounts);

        this.blocks = new HashMap<>();
        this.blocks.putAll(blocks);
    }

    public boolean hasResultsFor(Path file) {
        String key = toKey(file);
        return lineCounts.containsKey(key) && blocks.containsKey(key);
    }

    public Integer getDuplicatedLineCountFor(Path file) {
        return lineCounts.get(toKey(file));
    }

    public List<FlayDuplicateBlock> getDuplicatedBlocksFor(Path file) {
        return blocks.get(toKey(file));
    }

    private String toKey(Path file) {
        return file.toString();
    }
}

