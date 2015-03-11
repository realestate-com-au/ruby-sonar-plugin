package com.godaddy.sonar.ruby;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.Metric;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.scan.filesystem.FileQuery;
import org.sonar.api.scan.filesystem.ModuleFileSystem;

import com.godaddy.sonar.ruby.core.RubyPackage;

@SuppressWarnings("deprecation")
public class RubySensorTest {
	public static String INPUT_SOURCE_DIR = "src/test/resources/test-data";
	public static String INPUT_SOURCE_FILE = "src/test/resources/test-data/hello_world.rb";

	private IMocksControl mocksControl;
	private ModuleFileSystem moduleFileSystem;
	private ProjectFileSystem projectFileSystem;
	private SensorContext sensorContext;
	private Configuration config;
	private Project project;
	private List<File> sourceDirs;
	private List<File> files;
	
	@Before
	public void setUp() throws Exception {
		mocksControl = EasyMock.createControl();
		moduleFileSystem = mocksControl.createMock(ModuleFileSystem.class);
		projectFileSystem = mocksControl.createMock(ProjectFileSystem.class);

		config = mocksControl.createMock(Configuration.class);
		expect(config.getString("sonar.language", "java")).andStubReturn("ruby");

		project = new Project("test project");
		project.setConfiguration(config);
		project.setFileSystem(projectFileSystem);

		sensorContext = mocksControl.createMock(SensorContext.class);

		sourceDirs = new ArrayList<File>();
		sourceDirs.add(new File(INPUT_SOURCE_DIR));
		files = new ArrayList<File>();
		files.add(new File(INPUT_SOURCE_FILE));
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testRubySensor() {
		RubySensor sensor = new RubySensor(moduleFileSystem);
		assertNotNull(sensor);
	}

	@Test
	public void testShouldExecuteOnProject() {
		RubySensor sensor = new RubySensor(moduleFileSystem);

		mocksControl.replay();
		
		sensor.shouldExecuteOnProject(project);
		
		mocksControl.verify();	
	}

	@Test
	public void testAnalyse() {
		RubySensor sensor = new RubySensor(moduleFileSystem);

		Measure measure = new Measure();
		
		expect(moduleFileSystem.files(isA(FileQuery.class))).andReturn(files).once();
		expect(projectFileSystem.getSourceDirs()).andReturn(sourceDirs).once();
		expect(moduleFileSystem.sourceCharset()).andReturn(Charset.defaultCharset()).once();
		expect(sensorContext.saveMeasure(isA(org.sonar.api.resources.File.class), isA(Metric.class), isA(Double.class))).andReturn(measure).times(5);
		expect(sensorContext.saveMeasure(isA(RubyPackage.class), isA(Metric.class), isA(Double.class))).andReturn(measure).times(1);

		mocksControl.replay();

		sensor.analyse(project, sensorContext);
		
		mocksControl.verify();	
	}
	
	@Test
	public void testToString() {
		RubySensor sensor = new RubySensor(moduleFileSystem);
		String result = sensor.toString();
		assertEquals("RubySensor", result);
	}
}
