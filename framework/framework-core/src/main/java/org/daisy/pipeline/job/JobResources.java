package org.daisy.pipeline.job;

import java.net.URI;
import java.util.Set;

import org.daisy.common.file.Resource;

/**
 * Collection of job resource files
 */
public interface JobResources {

	Set<URI> getNames();

	Resource getResource(URI name);

}
