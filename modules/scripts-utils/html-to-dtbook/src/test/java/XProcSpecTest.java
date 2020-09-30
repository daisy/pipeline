import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

import static org.daisy.pipeline.pax.exam.Options.mavenBundle;

import org.ops4j.pax.exam.Configuration;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.options;
import org.ops4j.pax.exam.Option;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {

    @Override
    protected String[] testDependencies() {
        return new String[] {
            pipelineModule("epub-utils"),
            pipelineModule("common-utils"),
            pipelineModule("file-utils"),
            pipelineModule("fileset-utils"),
        };
    }
}
