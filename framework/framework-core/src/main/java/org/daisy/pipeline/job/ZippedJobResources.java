package org.daisy.pipeline.job;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

// TODO: Auto-generated Javadoc
/**
 * The Class ZipResourceContext wrapps a zip file into a resource collection, it is used as context to execute pipelines.
 */
public final class ZippedJobResources implements JobResources {

	/** The resources. */
	private final Map<String, Supplier<InputStream>> resources;

	/**
	 * Instantiates a new zip resource context.
	 *
	 * @param zip the zip
	 */
	public ZippedJobResources(final ZipFile zip) {
		ImmutableMap.Builder<String, Supplier<InputStream>> mapBuilder = ImmutableMap
				.builder();
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			final ZipEntry entry = entries.nextElement();
			mapBuilder.put(entry.getName(), new Supplier<InputStream>() {

				@Override
				public InputStream get() {
					try {
						return zip.getInputStream(entry);
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}

			});
		}
		resources = mapBuilder.build();
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.ResourceCollection#getResource(java.lang.String)
	 */
	@Override
	public Supplier<InputStream> getResource(String name) {
		return resources.get(name);
	}

	/* (non-Javadoc)
	 * @see org.daisy.pipeline.job.ResourceCollection#getNames()
	 */
	@Override
	public Iterable<String> getNames() {
		return resources.keySet();
	}

}
