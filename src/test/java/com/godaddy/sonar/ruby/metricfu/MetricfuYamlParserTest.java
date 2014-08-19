package com.godaddy.sonar.ruby.metricfu;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jfree.util.Log;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import static org.easymock.EasyMock.expect;

public class MetricfuYamlParserTest extends TestCase
{
	private final static String YML_FILE_NAME = "resources/test-data/results.yml";

    private IMocksControl mocksControl;
    private ModuleFileSystem moduleFileSystem;
	private MetricfuYamlParser parser = null;
	
	@Before
	public void setUp() throws Exception
	{
        mocksControl = EasyMock.createControl();
        moduleFileSystem = mocksControl.createMock(ModuleFileSystem.class);
	}
		
	@Test
	public void testParseFunction() throws IOException
	{
        expect(moduleFileSystem.baseDir()).andReturn(new File("src/test"));
        mocksControl.replay();

        parser = new MetricfuYamlParser(moduleFileSystem, YML_FILE_NAME);
        List<SaikuroComplexity> rubyFunctions = parser.parseSaikuro("lib/some_path/foo_bar.rb");
        mocksControl.verify();

		SaikuroComplexity rubyFunction0 = new SaikuroComplexity("lib/some_path/foo_bar.rb", 5, "FooBar#validate_user_name", 4);		
		assertTrue(rubyFunctions.size()==2);
		assertTrue(rubyFunctions.get(0).toString().equals(rubyFunction0.toString()));
		
		List<FlayReason> duplications = parser.parseFlay();        
		for (FlayReason duplication : duplications) {
			for (FlayReason.Match match : duplication.getMatches()) {
				Log.debug(match.getFile() + ":" + match.getStartLine());
			}
		}
	}	
}
