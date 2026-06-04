package org.daisy.pipeline.junit;

import java.io.File;
import java.util.Properties;

import org.junit.runner.RunWith;

import org.ops4j.pax.exam.util.PathUtils;

@RunWith(TestRunner.class)
public abstract class AbstractTest {
	
	protected Properties systemProperties() {
		return null;
	}
	
	protected Properties calabashConfiguration() {
		File baseDir = new File(PathUtils.getBaseDir());
		Properties p = new Properties();
		p.setProperty("com.xmlcalabash.config.user", "");
		File file = new File(baseDir, "/src/test/resources/config-calabash.xml");
		if (file.exists())
			p.setProperty("org.daisy.pipeline.xproc.configuration", file.getAbsolutePath());
		return p;
	}
	
	protected Properties logbackConfiguration() {
		File baseDir = new File(PathUtils.getBaseDir());
		File file = new File(baseDir, "/src/test/resources/logback.xml");
		if (!file.exists())
			return null;
		Properties p = new Properties();
		p.setProperty("logback.configurationFile", file.toURI().toString());
		return p;
	}
	
	protected Properties allSystemProperties() {
		return mergeProperties(
			systemProperties(),
			logbackConfiguration());
	}
	
	protected Properties mergeProperties(Properties... properties) {
		Properties merged = new Properties();
		for (Properties props : properties)
			if (props != null)
				for (String key : props.stringPropertyNames())
					merged.setProperty(key, props.getProperty(key));
		return merged;
	}
	
	@TestConfiguration
	public void testConfiguration() {
		Properties props = allSystemProperties();
		for (String key : props.stringPropertyNames())
			System.setProperty(key, props.getProperty(key));
	}
}
