import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import javax.inject.Inject;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamResult;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.pipeline.junit.AbstractTest;

import org.ops4j.pax.exam.util.PathUtils;

import org.junit.Assert;
import org.junit.Test;

public class XProcEngineTest extends AbstractTest {

	@Inject
	public XProcEngine xprocEngine;

	@Test
	public void testStepWithNonStringOptions() throws XProcErrorException {
		XProcPipeline pipeline = xprocEngine.load(new File(new File(PathUtils.getBaseDir()), "src/test/resources/step.xpl").toURI());
		Writer result = new StringWriter();
		pipeline.run(
			new XProcInput.Builder().withOption(new QName("option-1"), ImmutableList.of("1", "2", "3"))
			                        .withOption(new QName("option-2"), ImmutableMap.of("1", "foo",
			                                                                           "2", "bar",
			                                                                           "3", "baz"))
			                        .build()
		).writeTo(
			new XProcOutput.Builder().withOutput("result", () -> new StreamResult(result)).build()
		);
		Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" +
		                    "<result>foo///bar///baz</result>",
		                    result.toString());
	}

	@Override
	public String[] testDependencies() {
		return new String[]{
			"com.google.guava:guava:?",
			"org.daisy.libs:com.xmlcalabash:?",
			"org.daisy.libs:saxon-he:?",
			"org.slf4j:slf4j-api:?",
			"org.daisy.pipeline:common-utils:?",
			"org.daisy.pipeline:saxon-adapter:?",
			"org.daisy.pipeline:xproc-api:?",
			"org.daisy.pipeline:logging-appender:?",
		};
	}
}
