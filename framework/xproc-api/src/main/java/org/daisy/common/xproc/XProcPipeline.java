package org.daisy.common.xproc;

import java.util.Map;

/**
 * XProcPipeline gives access to the pipeline info and allows to run a pipeline.
 */
public interface XProcPipeline {

	/**
	 * Gets the pipeline info object associated to this pipeline.
	 */
	XProcPipelineInfo getInfo();

	/**
	 * Runs the pipeline on the specified input
	 */
	XProcResult run(XProcInput data) throws XProcErrorException;

	/**
	 * Runs the pipeline on the specified input
	 *
	 * @param monitor observer object to monitor the execution
	 */
	XProcResult run(XProcInput data, XProcMonitor monitor, Map<String,String> props) throws XProcErrorException;

}
