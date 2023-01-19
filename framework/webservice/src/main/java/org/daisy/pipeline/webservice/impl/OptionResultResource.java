package org.daisy.pipeline.webservice.impl;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobResult;

/**
 * The Class ResultResource.
 */
public class OptionResultResource extends NamedResultResource {

	@Override
	protected Collection<JobResult> gatherResults(Job job, String name) {
		return job.getResults().getResults(new QName(name));
	}
}
