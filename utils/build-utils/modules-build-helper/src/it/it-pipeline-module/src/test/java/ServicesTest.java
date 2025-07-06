import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;

import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.script.XProcScriptService;

import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class ServicesTest extends AbstractXSpecAndXProcSpecTest {
	
	@Inject
	public DatatypeRegistry datatypes;

	@Inject
	public DatatypeService datatype;
	
	@Test
	public void testDatatype() {
		String id = datatype.getId();
		assertTrue(id.equals("foo:choice") ||
		           id.equals("px:bar-2.params-option-2") ||
		           id.equals("px:script-option-1") ||
		           id.equals("transform-query"));
		Set<String> ids = new HashSet<>();
		for (DatatypeService datatype : datatypes.getDatatypes())
			ids.add(datatype.getId());
		assertTrue(ids.remove("foo:choice"));
		assertTrue(ids.remove("px:bar-2.params-option-2"));
		assertTrue(ids.remove("px:script-option-1"));
		assertTrue(ids.remove("transform-query"));       // because braille-common on class path
		assertTrue(ids.remove("stylesheet-parameters")); // because css-utils on class path
		assertTrue(ids.remove("preview-table"));         // because pef-utils on class path
		//assertTrue(ids.remove("liblouis-table-query"));  // because liblouis-utils on class path
		assertTrue("ids not empty: " + ids, ids.isEmpty());
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
}
