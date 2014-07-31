package com.godaddy.sonar.ruby.metricfu;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class FlayDuplicationResults
{
    private static final Logger LOG = LoggerFactory.getLogger(FlayDuplicationResults.class);

    private final ModuleFileSystem filesystem;
    private final Map<String, Integer> lineCounts;
    private final Map<String, List<FlayDuplicateBlock>> blocks;

    public FlayDuplicationResults(ModuleFileSystem fs, Map<String, Integer> lineCounts, Map<String, List<FlayDuplicateBlock>> blocks) {
        this.filesystem = fs;
        this.lineCounts = lineCounts;
        this.blocks = blocks;
    }

    public Integer getDuplicatedLineCountFor(File file) {
        Path relative = findRelativePath(file);
        LOG.debug(String.format("Getting flay lineCounts for %s using key %s", file, relative));
        return lineCounts.get(relative.toString());
    }

    public List<FlayDuplicateBlock> getDuplicatedBlocksFor(File file) {
        Path relative = findRelativePath(file);
        LOG.debug(String.format("Getting flay blocks for %s using key %s", file, relative));
        return blocks.get(relative.toString());
    }

    private Path findRelativePath(File file) {
        // the flay name is relative to the project root. let's guess that is the parent of the first source dir...
        Path root = filesystem.sourceDirs().get(0).getParentFile().toPath();
        return root.relativize(file.toPath());
    }

}
