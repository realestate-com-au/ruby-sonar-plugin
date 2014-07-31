package com.godaddy.sonar.ruby;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.sonar.api.Extension;

import com.godaddy.sonar.ruby.core.*;
import com.godaddy.sonar.ruby.core.profiles.SonarWayProfile;
import com.godaddy.sonar.ruby.metricfu.*;
import com.godaddy.sonar.ruby.simplecovrcov.*;

public class RubyPluginTest {

	@Test
	public void testGetExtensions() {
		RubyPlugin plugin = new RubyPlugin();
		List<Class<? extends Extension>> extensions = plugin.getExtensions();
		assertTrue(extensions.size() > 0);
		assertTrue(extensions.contains(Ruby.class));
		assertTrue(extensions.contains(SimpleCovRcovSensor.class));
		assertTrue(extensions.contains(SimpleCovRcovJsonParserImpl.class));
        assertTrue(extensions.contains(MetricfuComplexityYamlParserImpl.class));
        assertTrue(extensions.contains(MetricfuDuplicationYamlParserImpl.class));
		assertTrue(extensions.contains(RubySourceImporter.class));
		assertTrue(extensions.contains(RubySourceCodeColorizer.class));
		assertTrue(extensions.contains(RubySensor.class));
        assertTrue(extensions.contains(MetricfuComplexitySensor.class));
        assertTrue(extensions.contains(MetricfuDuplicationSensor.class));
		assertTrue(extensions.contains(SonarWayProfile.class));
	}
}
