package org.daisy.pipeline.pax.exam;

import java.util.ArrayList;
import static java.util.Collections.sort;
import java.util.List;

import static org.daisy.pipeline.pax.exam.Options.logbackClassic;
import static org.daisy.pipeline.pax.exam.Options.mavenBundlesWithDependencies;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.ops4j.pax.exam.Option;

public class OptionsTest {
	
	@Test
	public void testLogbackClassicWithDependencies() {
		List<String> bundles = new ArrayList<String>();
		for (Option b : mavenBundlesWithDependencies(logbackClassic()).getBundles())
			bundles.add(b.toString());
		sort(bundles);
		assertEquals(bundles.get(0).toString(), "mavenBundle(\"ch.qos.logback:logback-classic:1.0.11(start@4)\")");
		assertEquals(bundles.get(1).toString(), "mavenBundle(\"ch.qos.logback:logback-core:1.0.11(start@4)\")");
		assertEquals(bundles.get(2).toString(), "mavenBundle(\"org.slf4j:slf4j-api:1.7.4(start@4)\")");
	}
}
