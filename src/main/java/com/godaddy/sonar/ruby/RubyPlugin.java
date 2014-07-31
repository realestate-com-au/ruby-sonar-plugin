package com.godaddy.sonar.ruby;

import com.godaddy.sonar.ruby.core.Ruby;
import com.godaddy.sonar.ruby.core.RubySourceCodeColorizer;
import com.godaddy.sonar.ruby.core.RubySourceImporter;
import com.godaddy.sonar.ruby.core.profiles.SonarWayProfile;
import com.godaddy.sonar.ruby.metricfu.MetricfuComplexitySensor;
import com.godaddy.sonar.ruby.metricfu.MetricfuComplexityYamlParserImpl;
import com.godaddy.sonar.ruby.metricfu.MetricfuDuplicationSensor;
import com.godaddy.sonar.ruby.metricfu.MetricfuDuplicationYamlParserImpl;
import com.godaddy.sonar.ruby.simplecovrcov.SimpleCovRcovJsonParserImpl;
import com.godaddy.sonar.ruby.simplecovrcov.SimpleCovRcovSensor;

import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.SonarPlugin;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is the entry point for all extensions
 */
@Properties({})
public final class RubyPlugin extends SonarPlugin
{

    public List<Class<? extends Extension>> getExtensions()
    {
        List<Class<? extends Extension>> extensions = new ArrayList<Class<? extends Extension>>();
        extensions.add(Ruby.class);
        extensions.add(SimpleCovRcovSensor.class);
        extensions.add(SimpleCovRcovJsonParserImpl.class);
        extensions.add(MetricfuComplexityYamlParserImpl.class);
        extensions.add(MetricfuDuplicationYamlParserImpl.class);
        extensions.add(RubySourceImporter.class);
        extensions.add(RubySourceCodeColorizer.class);
        extensions.add(RubySensor.class);
        extensions.add(MetricfuComplexitySensor.class);
        extensions.add(MetricfuDuplicationSensor.class);

        // Profiles
        extensions.add(SonarWayProfile.class);

        return extensions;
    }
}
