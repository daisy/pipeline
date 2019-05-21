import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest; 
 
import static org.daisy.pipeline.pax.exam.Options.mavenBundle;
 
import org.ops4j.pax.exam.Configuration; 
import static org.ops4j.pax.exam.CoreOptions.composite; 
import static org.ops4j.pax.exam.CoreOptions.options; 
import org.ops4j.pax.exam.Option; 
 
public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest { 
    
    public XProcSpecTest() {
        XSPEC_TESTS_DIR = "../epub3-dtbook-migrator-rewrite/src/test/xspec";
        XPROCSPEC_TESTS_DIR = "../epub3-dtbook-migrator-rewrite/src/test/xprocspec";
    }
    
    @Override 
    protected String[] testDependencies() { 
        return new String[] { 
            // explicitly including epubcheck 4.2.0 here for v1.12.1 of the framework
            // because 1.12.1 of scripts-parent is not released.
            "org.w3c:epubcheck:4.2.0",
            
            pipelineModule("asciimath-utils"),
            pipelineModule("common-utils"),
            pipelineModule("dtbook-utils"),
            pipelineModule("dtbook-validator"),
            pipelineModule("epub3-nav-utils"),
            pipelineModule("epub3-ocf-utils"),
            pipelineModule("epub3-pub-utils"),
            pipelineModule("epubcheck-adapter"),
            pipelineModule("file-utils"),
            pipelineModule("fileset-utils"),
            pipelineModule("html-utils"),
            pipelineModule("mediatype-utils"),
            pipelineModule("validation-utils")
        }; 
    } 
     
    @Override @Configuration 
    public Option[] config() { 
        return options( 
            // FIXME: epubcheck needs older version of jing
            mavenBundle("org.daisy.libs:jing:20120724.0.0"),
            composite(super.config()));
    } 
} 
