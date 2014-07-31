package com.godaddy.sonar.ruby.metricfu;

import org.sonar.api.BatchExtension;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.nio.file.Path;

public class FlayToSonarFilePathMapper implements BatchExtension
{
    private ModuleFileSystem filesystem;

    public FlayToSonarFilePathMapper(ModuleFileSystem filesystem) {
        this.filesystem = filesystem;
    }

    public Path findRelativePath(File file) {
        // the flay name is relative to the project root. let's guess that is the parent of the first source dir...
        Path root = filesystem.sourceDirs().get(0).getParentFile().toPath();
        return root.relativize(file.toPath());
    }
}
