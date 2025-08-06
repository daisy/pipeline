package org.daisy.pipeline.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.daisy.common.file.Resource;
import org.daisy.pipeline.job.impl.IOHelper;

import com.google.common.collect.ImmutableMap;

/**
 * Collection of job resource files within a base directory.
 */
public final class JobResourcesDir implements JobResources {

	private final File baseDir;
	private final Map<URI,Resource> resources;

	public JobResourcesDir(File baseDir) {
		this.baseDir = baseDir;
		ImmutableMap.Builder<URI,Resource> mapBuilder = ImmutableMap.builder();
		try {
			int baselen = baseDir.getCanonicalPath().length();
			for (File f : IOHelper.treeFileList(baseDir)) {
				String relativePath = f.getCanonicalPath().substring(baselen + 1);
				try {
					URI name = new URI(null, null, relativePath.replace("\\", "/"), null, null).normalize();
					mapBuilder.put(
						name,
						Resource.load(f, name, Resource.MEDIA_TYPE_UNKNOWN));
				} catch (URISyntaxException e) {
					throw new RuntimeException("Resource path could not be converted to URI: " + relativePath, e);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		resources = mapBuilder.build();
	}

	@Override
	public Resource getResource(URI name) {
		return resources.get(name);
	}

	@Override
	public Set<URI> getNames() {
		return resources.keySet();
	}

	public File getBaseDir() {
		return baseDir;
	}
}
