package com.godaddy.sonar.ruby.metricfu;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.*;

public class FlayToSonarFilePathMapperTest {

    private IMocksControl mocksControl;
    private ModuleFileSystem moduleFileSystem;

    private FlayToSonarFilePathMapper mapper;

    @Before
    public void setUp() throws Exception {
        mocksControl = EasyMock.createControl();
        moduleFileSystem = mocksControl.createMock(ModuleFileSystem.class);

        mapper = new FlayToSonarFilePathMapper(moduleFileSystem);
    }

    @Test
    public void testFindRelativePath() throws Exception {
        List<File> srcDirs = new ArrayList<File>() {{ add(new File("/foo/bar")); }};
        expect(moduleFileSystem.sourceDirs()).andReturn(srcDirs);
        mocksControl.replay();

        assertEquals("bar/baz.rb", mapper.findRelativePath(new File("/foo/bar/baz.rb")).toString());
        mocksControl.verify();
    }
}