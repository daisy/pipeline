package org.daisy.pipeline.webservice.impl;

import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcOptionMetadata.Output;
import org.daisy.pipeline.script.XProcScript;

// TODO: Auto-generated Javadoc
/**
 * The Class XProcScriptFilter.
 */
public final class XProcScriptFilter {

	/** The Constant INSTANCE. */
	public static final XProcScriptFilter INSTANCE = new XProcScriptFilter();

	/** The Constant ANY_URI_TYPE. */
	private static final String ANY_URI_TYPE = "anyURI";

	/** The Constant ANY_FILE_URI_TYPE. */
	private static final String ANY_FILE_URI_TYPE = "anyFileURI";

	/** The Constant ANY_DIR_URI_TYPE. */
	private static final String ANY_DIR_URI_TYPE = "anyDirURI";

	/**
	 * Instantiates a new x proc script filter.
	 */
	private XProcScriptFilter() {
		// singleton
	}

	/* (non-Javadoc)
	 * @see org.daisy.common.base.Filter#filter(java.lang.Object)
	 */
	public XProcScript filter(XProcScript script) {
		XProcPipelineInfo xproc = script.getXProcPipelineInfo();
		// create the script builder
		XProcScript.Builder scriptBuilder = new XProcScript.Builder()
				.withShortName(script.getName()).withDescription(
						script.getDescription()).withHomepage(script.getHomepage()).withDescriptor(script.getDescriptor());
		// create the filtered pipeline info
		XProcPipelineInfo.Builder xprocBuilder = new XProcPipelineInfo.Builder();
		xprocBuilder.withURI(xproc.getURI());
		//copy filesets
		for( String fileset: script.getInputFilesets()){
			scriptBuilder.withInputFileset(fileset);
		}

		for( String fileset: script.getOutputFilesets()){
			scriptBuilder.withOutputFileset(fileset);
		}

		// copy input ports
		for (XProcPortInfo port : xproc.getInputPorts()) {
			xprocBuilder.withPort(port);
			scriptBuilder.withPortMetadata(port.getName(),
					script.getPortMetadata(port.getName()));
		}
		// copy parameter ports
		for (String port : xproc.getParameterPorts()) {
			// FIXME parameter ports should return XProcPortInfo
			xprocBuilder.withPort(XProcPortInfo.newParameterPort(port, false));
			scriptBuilder.withPortMetadata(port, script.getPortMetadata(port));
		}
		// output ports are not copied
		// copy options
		for (XProcOptionInfo option : xproc.getOptions()) {
			XProcOptionMetadata metadata = script.getOptionMetadata(option
					.getName());
			// filter-out options that are both OUTPUT options with type
			// inheriting from anyURI
			if (!(metadata.getOutput() != Output.NA &&
					(ANY_URI_TYPE.equals(metadata.getMediaType())
					 ||
					 ANY_FILE_URI_TYPE.equals(metadata.getType())
					 ||
					 ANY_DIR_URI_TYPE.equals(metadata.getType())))) {
				xprocBuilder.withOption(option);
				scriptBuilder.withOptionMetadata(option.getName(),
						script.getOptionMetadata(option.getName()));
			}

		}
		scriptBuilder.withPipelineInfo(xprocBuilder.build());
		return scriptBuilder.build();
	}
}
