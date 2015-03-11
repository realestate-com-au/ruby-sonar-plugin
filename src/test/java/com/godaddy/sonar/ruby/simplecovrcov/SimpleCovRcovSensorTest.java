package com.godaddy.sonar.ruby.simplecovrcov;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.CoverageMeasuresBuilder;
import org.sonar.api.measures.Measure;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.ModuleFileSystem;


public class SimpleCovRcovSensorTest 
{
	private static String RESULT_JSON_FILE_MUTLI_SRC_DIR = "src/test/resources/test-data/results.json";
	private static String RESULT_JSON_FILE_ONE_SRC_DIR = "src/test/resources/test-data/results-one-src-dir.json";
	
	private IMocksControl mocksControl;
	private ModuleFileSystem moduleFileSystem;
	private SimpleCovRcovJsonParser simpleCovRcovJsonParser;
	private SimpleCovRcovSensor simpleCovRcovSensor;
	private SensorContext sensorContext;
	
	@Before
	public void setUp() throws Exception
	{
		mocksControl = EasyMock.createControl();
		moduleFileSystem = mocksControl.createMock(ModuleFileSystem.class);
		simpleCovRcovJsonParser = mocksControl.createMock(SimpleCovRcovJsonParser.class);
		
		simpleCovRcovSensor = new SimpleCovRcovSensor(moduleFileSystem, simpleCovRcovJsonParser);
	}
	
	@Test
	public void testConstructor() 
	{	
		assertNotNull(simpleCovRcovSensor);
	}
	
	
	@Test
	public void analyseShouldCatchIOException() throws IOException
	{
		
		File jsonFile = new File("coverage/.resultset.json");
		sensorContext = mocksControl.createMock(SensorContext.class);
		
		expect(moduleFileSystem.sourceDirs()).andReturn(new ArrayList<File>());
		expect(simpleCovRcovJsonParser.parse(jsonFile)).andThrow(new IOException());
		
		mocksControl.replay();
		
		simpleCovRcovSensor.analyse(new Project("key_name"), sensorContext);
		
		mocksControl.verify();
		
		assertTrue(true);
	}
	
	@Test
	public void testShouldExecuteOnRubyProject()
	{
		Configuration config = mocksControl.createMock(Configuration.class);
		expect(config.getString("sonar.language", "java")).andReturn("ruby");
		mocksControl.replay();
		
		Project project = new Project("test project");
		project.setConfiguration(config);
		
		assertTrue(simpleCovRcovSensor.shouldExecuteOnProject(project));
		
		mocksControl.verify();		
	}
	
	@Test
	public void testShouldNotExecuteOnJavascriptProject()
	{
		Configuration config = mocksControl.createMock(Configuration.class);
		expect(config.getString("sonar.language", "java")).andReturn("javascript");
		mocksControl.replay();
		
		Project project = new Project("test project");
		project.setConfiguration(config);
		
		assertFalse(simpleCovRcovSensor.shouldExecuteOnProject(project));
		
		mocksControl.verify();		
	}
	
	
	@Test
	public void testAnalyseWithOneSrcDir() throws IOException
	{
		Map<String, CoverageMeasuresBuilder> jsonResults = new SimpleCovRcovJsonParserImpl().parse(new File(RESULT_JSON_FILE_ONE_SRC_DIR));
		
		List<File> sourceDirs = new ArrayList<File>();	
		for (String fileName : jsonResults.keySet())
		{
			sourceDirs.add(new File(fileName).getParentFile());
		}
		
		Measure measure = new Measure();		
		sensorContext = mocksControl.createMock(SensorContext.class);
		
		expect(moduleFileSystem.sourceDirs()).andReturn(sourceDirs).once();
		expect(simpleCovRcovJsonParser.parse(eq(new File("coverage/.resultset.json")))).andReturn(jsonResults).once();
		expect(sensorContext.saveMeasure(isA(org.sonar.api.resources.File.class), isA(Measure.class))).andReturn(measure).times(9);

		mocksControl.replay();

		simpleCovRcovSensor.analyse(new Project("key_name"), sensorContext);
		
		mocksControl.verify();
	}
	
	@Test
	public void testAnalyseWithMoreThanOneSrcDir() throws IOException
	{
		Map<String, CoverageMeasuresBuilder> jsonResults = new SimpleCovRcovJsonParserImpl().parse(new File(RESULT_JSON_FILE_MUTLI_SRC_DIR));
		
		List<File> sourceDirs = new ArrayList<File>();	
		for (String fileName : jsonResults.keySet())
		{
			sourceDirs.add(new File(fileName).getParentFile());
		}
		
		Measure measure = new Measure();		
		sensorContext = mocksControl.createMock(SensorContext.class);
		
		expect(moduleFileSystem.sourceDirs()).andReturn(sourceDirs).once();
		expect(simpleCovRcovJsonParser.parse(eq(new File("coverage/.resultset.json")))).andReturn(jsonResults).once();
		expect(sensorContext.saveMeasure(isA(org.sonar.api.resources.File.class), isA(Measure.class))).andReturn(measure).times(36);

		mocksControl.replay();

		simpleCovRcovSensor.analyse(new Project("key_name"), sensorContext);
		
		mocksControl.verify();		
	}
}
