package org.daisy.pipeline.webservice.restlet.impl;

import java.util.Collection;
import java.util.Collections;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobResult;

/**
 * Kept for backward compatibility. Always returns an empty list.
 */
public class OptionResultResource extends NamedResultResource {

	@Override
	protected Collection<JobResult> gatherResults(Job job, String name) {
		return Collections.emptyList();
	}
}
