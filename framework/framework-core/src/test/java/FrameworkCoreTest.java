import javax.inject.Inject;
import javax.xml.namespace.QName;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScriptParser;
import org.daisy.pipeline.script.XProcScriptService;

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
	public XProcScriptParser scriptParser;
	
	@Inject
	public XProcScriptService script;
	
	@Test
	public void testScript() {
		assertEquals("unit-test-script", script.getId());
		script.setParser(scriptParser);
		XProcOptionMetadata meta = script.load().getOptionMetadata(new QName("option1"));
		assertEquals("dtbook:mydatatype", meta.getType());
	}

	@Inject
	public DatatypeService datatype;
	
	@Test
	public void testDatatype() {
		assertEquals("dtbook:mydatatype", datatype.getId());
	}
}
