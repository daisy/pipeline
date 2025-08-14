import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import org.daisy.common.shell.BinaryFinder;
import org.junit.Assume;
import org.junit.Test;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

	@Override
	@Test
	public void runXSpecAndXProcSpec() throws Exception {
		Assume.assumeTrue("Test can not be run because espeak not present",
		                  BinaryFinder.find("espeak").isPresent());
		super.runXSpecAndXProcSpec();
	}

	@Override
	protected String[] testDependencies() {
		return new String[] {
		};
	}
}
