import org.daisy.pipeline.junit.AbstractXSpecAndXProcSpecTest;

public class XProcSpecTest extends AbstractXSpecAndXProcSpecTest {
	
	@Override
	protected String[] testDependencies() {
		return new String[] {
			pipelineModule("common-utils"),
			pipelineModule("css-speech"),
			pipelineModule("epub3-nav-utils"),
			pipelineModule("epub3-ocf-utils"),
			pipelineModule("epub3-pub-utils"),
			pipelineModule("epub3-tts"),
			pipelineModule("fileset-utils"),
			pipelineModule("file-utils"),
			pipelineModule("html-utils"),
			pipelineModule("mediaoverlay-utils"),
			pipelineModule("metadata-utils"),
			pipelineModule("tts-helpers"),
			pipelineModule("zedai-to-html"),
			pipelineModule("zedai-utils")
		};
	}
}
