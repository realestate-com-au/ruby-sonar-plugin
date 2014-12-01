package com.godaddy.sonar.ruby.metricfu;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertNotNull;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import static org.easymock.EasyMock.isA;
import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import com.godaddy.sonar.ruby.RubySensor;


public class MetricfuComplexitySensorTest 
{
	private IMocksControl mocksControl;
	private ModuleFileSystem moduleFileSystem;
	private SensorContext sensorContext;
	private MetricfuComplexityYamlParser metricfuComplexityYamlParser;
	private MetricfuComplexitySensor metricfuComplexitySensor;
	private Configuration config;
	private Project project;

	@Before
	public void setUp() throws Exception
	{
		mocksControl = EasyMock.createControl();
		moduleFileSystem = mocksControl.createMock(ModuleFileSystem.class);
		metricfuComplexityYamlParser = mocksControl.createMock(MetricfuComplexityYamlParser.class);
		
		metricfuComplexitySensor = new MetricfuComplexitySensor(moduleFileSystem, metricfuComplexityYamlParser);
		config = mocksControl.createMock(Configuration.class);
		expect(config.getString("sonar.language", "java")).andStubReturn("ruby");

		project = new Project("test project");
		project.setConfiguration(config);
		
	}
	
	@Test
	public void testConstructor() 
	{	
		assertNotNull(metricfuComplexitySensor);
	}
	
	@Test
	public void testShouldExecuteOnRubyProject()
	{		
		Configuration config = mocksControl.createMock(Configuration.class);
		expect(config.getString("sonar.language", "java")).andReturn("ruby");
		mocksControl.replay();
		
		Project project = new Project("test project");
		project.setConfiguration(config);

		RubySensor sensor = new RubySensor(moduleFileSystem);
		sensor.shouldExecuteOnProject(project);
		
		mocksControl.verify();			
	}

	@Test
	public void testShouldAnalyzeProject() throws IOException
	{
		List<File> sourceFiles= new ArrayList<File>();
		List<File> sourceDirs = new ArrayList<File>();
		
		sourceDirs.add(new File("lib"));
		
		sourceFiles.add(new File("lib/some_path/foo_bar.rb"));

		sensorContext = mocksControl.createMock(SensorContext.class);
		List<RubyFunction> functions = new ArrayList<RubyFunction>();
		functions.add(new RubyFunction("validate", 5, 10));
		
		Measure measure = new Measure();
		expect(moduleFileSystem.files(isA(FileQuery.class))).andReturn(sourceFiles);
		expect(moduleFileSystem.sourceDirs()).andReturn(sourceDirs);
		expect(metricfuComplexityYamlParser.parseFunctions(isA(String.class),isA(File.class))).andReturn(functions);
		expect(sensorContext.saveMeasure(isA(org.sonar.api.resources.File.class), isA(Metric.class), isA(Double.class))).andReturn(measure).times(2);
		expect(sensorContext.saveMeasure(isA(org.sonar.api.resources.File.class), isA(Measure.class))).andReturn(measure).times(2);

		mocksControl.replay();

		metricfuComplexitySensor.analyse(project, sensorContext);
		mocksControl.verify();
	}
}
