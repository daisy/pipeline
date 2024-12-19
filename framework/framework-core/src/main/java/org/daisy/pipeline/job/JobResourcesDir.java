package org.daisy.pipeline.job;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.daisy.pipeline.job.impl.IOHelper;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;

/**
 * Collection of job resource files within a base directory.
 */
public final class JobResourcesDir implements JobResources {

	private final File baseDir;
	private final Map<String,Supplier<InputStream>> resources;

	public JobResourcesDir(File baseDir) {
		this.baseDir = baseDir;
		ImmutableMap.Builder<String,Supplier<InputStream>> mapBuilder = ImmutableMap.builder();
		try {
			int baselen = baseDir.getCanonicalPath().length();
			for (File f : IOHelper.treeFileList(baseDir)) {
				mapBuilder.put(
					f.getCanonicalPath().substring(baselen + 1),
					() -> {
						try {
							return new FileInputStream(f);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
				);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		resources = mapBuilder.build();
	}

	@Override
	public Supplier<InputStream> getResource(String name) {
		return resources.get(name);
	}

	@Override
	public Iterable<String> getNames() {
		return resources.keySet();
	}

	public File getBaseDir() {
		return baseDir;
	}
}
