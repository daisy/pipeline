package org.daisy.pipeline.job;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

import org.daisy.common.file.Resource;

/**
 * A job result
 */
public class JobResult extends Resource {

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !(obj instanceof JobResult))
			return false;
		JobResult other = (JobResult)obj;
		return getPath().toString().equals(other.getPath().toString())
			&& getFile().equals(other.getFile());
	}

	@Override
	public int hashCode() {
		return getPath().toString().hashCode() + getFile().hashCode();
	}

	@Override
	public String toString() {
		return String.format("JobResult [#=%s file='%s']", getPath(), getFile());
	}

	/**
	 * @param path The short (relative) path
	 * @param file The file path
	 * @param mediaType The media type
	 */
	protected JobResult(URI path, File file, String mediaType) {
		super(file, path, Optional.ofNullable(mediaType));
	}

	public File getFile() {
		try {
			return readAsFile();
		} catch (UnsupportedOperationException e) {
			throw new IllegalStateException("coding error");
		}
	}

	/**
	 * Strip the first path level
	 */
	public JobResult strip() {
		return new JobResult(stripPrefix(getPath()), getFile(), getMediaType().orElse(null));
	}

	private static URI stripPrefix(URI path) {
		int idx = path.toString().indexOf('/');
		if (idx != 0)
			return URI.create(path.toString().substring(idx + 1));
		else
			return path;
	}

	/**
	 * The size of the file in bytes
	 */
	public long getSize() {
		try {
			File file = getFile();
			if (!file.exists())
				throw new IOException(String.format("File not found: ", file.getAbsolutePath()));
			return file.length();
		} catch (Exception e){
			throw new RuntimeException("Error calculating result size", e);
		}
	}
}
