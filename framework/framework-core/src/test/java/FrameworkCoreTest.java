import javax.inject.Inject;

import org.daisy.pipeline.junit.AbstractTest;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class FrameworkCoreTest extends AbstractTest {
	
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
