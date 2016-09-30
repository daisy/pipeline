package org.daisy.dotify.api.tasks;

import java.io.File;

import org.daisy.dotify.api.tasks.AnnotatedFile;

/**
 * Provides a default implementation of AnnotatedFile
 * 
 * @author Joel Håkansson
 */
public class DefaultAnnotatedFile implements AnnotatedFile {
	private final File f;
	private final String extension;
	private final String mediaType;
	
	/**
	 * Provides a builder for an annotated file
	 * @author Joel Håkansson
	 *
	 */
	public static class Builder {
		private final File f;
		private String extension = null;
		private String mediaType = null;
		
		/**
		 * Creates a new builder with the specified file
		 * @param f the file
		 */
		public Builder(File f) {
			this.f = f;
		}

		/**
		 * Sets the file extension
		 * @param value the extension
		 * @return this builder
		 */
		public Builder extension(String value) {
			this.extension = value;
			return this;
		}
		
		/**
		 * Sets the file extension using the specified file.
		 * @param value
		 * @return this builder
		 */
		public Builder extension(File value) {
			String inp = value.getName();
			int inx = inp.lastIndexOf('.');
			this.extension = (inx>-1 && inx<inp.length()-1)?inp.substring(inx + 1):null;
			return this;
		}
		
		/**
		 * Sets the media type
		 * @param value the media type
		 * @return this builder
		 */
		public Builder mediaType(String value) {
			this.mediaType = value;
			return this;
		}

		/**
		 * Build the annotated file
		 * @return a new Annotated File
		 */
		public DefaultAnnotatedFile build() {
			return new DefaultAnnotatedFile(this);
		}
	}
	
	/**
	 * Creates a new builder with the specified file.
	 * @param f the file
	 * @return returns a new builder
	 */
	public static Builder with(File f) {
		return new Builder(f);
	}

	private DefaultAnnotatedFile(Builder builder) {
		this.f = builder.f;
		this.extension = builder.extension;
		this.mediaType = builder.mediaType;
	}

	@Override
	public File getFile() {
		return f;
	}

	@Override
	public String getExtension() {
		return extension;
	}

	@Override
	public String getMediaType() {
		return mediaType;
	}

}
