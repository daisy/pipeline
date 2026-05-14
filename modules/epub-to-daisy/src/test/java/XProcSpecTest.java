import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import java.util.Properties;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

	@Override
	protected Properties systemProperties() {
		Properties p = new Properties();
		p.setProperty("org.daisy.pipeline.lexing.omni.prioritize", "true");
		return p;
	}
}
