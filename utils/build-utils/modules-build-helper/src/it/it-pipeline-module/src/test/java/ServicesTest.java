import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.script.XProcScriptService;

import org.daisy.pipeline.junit.AbstractTest;
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class ServicesTest extends AbstractTest {
	
	@Inject
	// public DatatypeRegistry datatypes;
	public DatatypeService datatype;
	
	@Test
	public void testDatatype() {
		String id = datatype.getId();
		assertTrue(id.equals("foo:choice") ||
		           id.equals("px:script-option-1") ||
		           id.equals("transform-query"));
		// FIXME: DefaultDatatypeRegistry (framework-core) must support SPI
		// Set<String> ids = new HashSet<>();
		// for (DatatypeService datatype : datatypes.getDatatypes())
		// 	ids.add(datatype.getId());
		// assertTrue(ids.remove("foo:choice"));
		// assertTrue(ids.remove("px:script-option-1"));
		// assertTrue(ids.remove("transform-query")); // because o.d.p.modules.braille:common-utils on class path
		// assertTrue(ids.isEmpty());
	}
	
	@Inject
	public XProcScriptService script;
	
	@Test
	public void testScript() {
		assertEquals("my-script", script.getId());
	}
	
	/* ------------- */
	/* For OSGi only */
	/* ------------- */
	
	@Override
	protected String[] testDependencies() {
		return new String[]{
			"org.daisy.pipeline:framework-core:?",
			"org.daisy.pipeline:calabash-adapter:?",
			"org.daisy.pipeline.modules.braille:liblouis-utils:?"
		};
	}

	@Override @Configuration
	public Option[] config() {
		return options(
			// FIXME: BrailleUtils (dependency of liblouis-utils) needs older version of jing
			mavenBundle("org.daisy.libs:jing:20120724.0.0"),
			composite(super.config()));
	}
}
