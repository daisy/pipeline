package org.daisy.common.xproc;

import java.util.Properties;

/**
 * The Interface XProcPipeline gives access to the pipeline info and allows to run a pipeline.
 */
public interface XProcPipeline {

	/**
	 * Gets the pipeline info object associated to this pipeline.
	 *
	 * @return the info
	 */
	XProcPipelineInfo getInfo();

	/**
	 * Runs the pipline plugging the input descriptions into the engine
	 *
	 * @param data the data
	 * @return the x proc result
	 */
	XProcResult run(XProcInput data);


	/**
	 * Runs the pipline plugging the input descriptions into the engine
	 *
	 * @param data the data
	 * @param monitor observer object to monitorise the execution
	 * @return the x proc result
	 */
	XProcResult run(XProcInput data,XProcMonitor monitor,Properties props);



}
