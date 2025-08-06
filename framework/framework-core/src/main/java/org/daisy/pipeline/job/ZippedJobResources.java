package org.daisy.pipeline.job;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.daisy.common.file.Resource;

import com.google.common.collect.ImmutableMap;

/**
 * Wraps a zip file into a {@link JobResources}.
 */
public final class ZippedJobResources implements JobResources {

	private final Map<URI,Resource> resources;

	public ZippedJobResources(final ZipFile zip) {
		ImmutableMap.Builder<URI,Resource> mapBuilder = ImmutableMap.builder();
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			final ZipEntry entry = entries.nextElement();
			try {
				URI name = new URI(null, null, entry.getName().replace("\\", "/"), null, null).normalize();
				mapBuilder.put(
					name,
					Resource.load(
						() -> {
							try {
								return zip.getInputStream(entry); }
							catch (IOException e) {
								throw new RuntimeException(e); }},
						name,
						Resource.MEDIA_TYPE_UNKNOWN));
			} catch (URISyntaxException e) {
				throw new RuntimeException("Resource path could not be converted to URI: " + entry.getName(), e);
			}
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
}
