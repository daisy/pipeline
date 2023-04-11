import javax.inject.Inject;

import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import org.ops4j.pax.exam.ProbeBuilder;
import org.ops4j.pax.exam.TestProbeBuilder;

public class FrameworkCoreTest extends AbstractTest {
	
	@Override
	public String[] testDependencies() {
		return new String[]{
			"com.google.guava:guava:?",
			"org.slf4j:slf4j-api:?",
			"org.daisy.libs:jing:?",
			"org.daisy.pipeline:common-utils:?",
			"org.daisy.pipeline:xproc-api:?",
			"org.daisy.pipeline:woodstox-osgi-adapter:?",
		};
	}
	
	@ProbeBuilder
	public TestProbeBuilder probeConfiguration(TestProbeBuilder probe) {
		// FIXME: can not delete this yet because it can not be generated with maven-bundle-plugin
		probe.setHeader("Service-Component", "OSGI-INF/script.xml,"
		                                   + "OSGI-INF/datatype.xml");
		return probe;
	}
	
	@Inject
	public ScriptRegistry registry;
	
	@Test
	public void testScript() {
		ScriptService<?> script = registry.getScript("unit-test-script");
		assertEquals("unit-test-script", script.getId());
		ScriptOption meta = script.load().getOption("option1");
		assertEquals("dtbook:mydatatype", meta.getType().getId());
	}
}
