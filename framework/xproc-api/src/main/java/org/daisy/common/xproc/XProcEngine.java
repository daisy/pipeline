package org.daisy.common.xproc;

import java.net.URI;

/**
 * The Interface XProcEngine allows to load XProcPipeline objects to run a pipeline located at a given URI.
 * Offers some extra functionality to directly load a pipeline information or execute a pipeline without relying on a
 * XProcPipeline object
 */
public interface XProcEngine {


	/**
	 * Loads the pipeline at the given URI.
	 *
	 * @param uri the uri
	 * @return the x proc pipeline
	 */
	XProcPipeline load(URI uri);

	/**
	 * Gets the info from the pipeline located at the URI.
	 *
	 * @param uri the uri
	 * @return the info
	 */
	XProcPipelineInfo getInfo(URI uri);

	/**
	 * Runs a pipeline located in a given URI.
	 *
	 * @param uri the uri
	 * @param data the data
	 * @return the x proc result
	 */
	XProcResult run(URI uri, XProcInput data) throws XProcErrorException;

}
