import java.net.URI;

import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcResult;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "mock-xproc-engine",
	service = { XProcEngine.class }
)
public class MockXProcEngine implements XProcEngine {

	public XProcPipeline load(URI uri) { throw new UnsupportedOperationException(); }
	public XProcPipelineInfo getInfo(URI uri) { throw new UnsupportedOperationException(); }
	public XProcResult run(URI uri, XProcInput data) throws XProcErrorException { throw new UnsupportedOperationException(); }

}
