package org.daisy.pipeline.script;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import com.google.common.io.CharStreams;

import org.daisy.common.transform.LazySaxSourceProvider;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.JobResourcesDir;
import org.daisy.pipeline.job.impl.IOHelper;

import org.xml.sax.InputSource;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ScriptInput {

	public static class Builder {

		final Map<String,SourceSequence> inputs = Maps.newHashMap();
		final Map<String,List<String>> options = Maps.newHashMap();
		private final JobResources resources;
		private final Set<URI> resourcePaths;

		/**
		 * Don't handle relative inputs.
		 */
		public Builder() {
			this((JobResources)null);
		}

		/**
		 * Resolve relative inputs within a resource collection.
		 */
		public Builder(JobResources resources) {
			this.resources = resources;
			if (resources != null) {
				resourcePaths = new HashSet<>();
				for (String path : resources.getNames())
					try {
						resourcePaths.add(new URI(null, null, path.replace("\\", "/"), null, null).normalize());
					} catch (URISyntaxException e) {
						throw new RuntimeException("Resource path could not be converted to URI: " + path, e);
					}
			} else
				resourcePaths = null;
		}

		/**
		 * Resolve relative inputs against a base directory.
		 */
		public Builder(File baseDir) {
			this(new JobResourcesDir(baseDir));
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws IllegalArgumentException if no {@link InputStream} or {@link Reader} can be
		 *         obtained from <code>source</code> and <code>source</code> has an empty system ID.
		 * @throws FileNotFoundException if no {@link InputStream} or {@link Reader} can be obtained
		 *         from <code>source</code> and the system ID can not be resolved to a document.
		 */
		public Builder withInput(String port, Source source) throws IllegalArgumentException, FileNotFoundException {
			InputSource is = SAXSource.sourceToInputSource(source);
			if (is == null || (is.getByteStream() == null && is.getCharacterStream() == null)) {
				String sysId = source.getSystemId();
				if (sysId == null || "".equals(sysId)) {
					throw new IllegalArgumentException(
						"Input is expected to either be a stream or have non empty system ID");
				}
				checkInputURI(sysId);
			}
			getSequence(port).add(source);
			return this;
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws FileNotFoundException if the URI can not be resolved to a document.
		 */
		public Builder withInput(String port, URI source) throws FileNotFoundException {
			checkInputURI(source);
			return withInput(port, new LazySaxSourceProvider(source.toASCIIString()));
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws FileNotFoundException if <code>source</code> does not exist.
		 */
		public Builder withInput(String port, File source) throws FileNotFoundException {
			checkFile(source);
			return withInput(port, new LazySaxSourceProvider(source.toURI().toASCIIString()));
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws FileNotFoundException if the URL can not be resolved to a document.
		 */
		public Builder withInput(String port, URL source) throws FileNotFoundException {
			InputSource is = new InputSource(checkInputURI(source));
			is.setSystemId(source.toString());
			getSequence(port).add(new SAXSource(is));
			return this;
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 */
		public Builder withInput(String port, InputStream source) {
			getSequence(port).add(new SAXSource(new InputSource(source)));
			return this;
		}

		/**
		 * Put a single document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * The {@link Supplier} serves as a proxy and must always return the same object.
		 */
		private Builder withInput(String port, Supplier<Source> source) {
			getSequence(port).add(source);
			return this;
		}

		private SourceSequence getSequence(String port) {
			SourceSequence sources = inputs.get(port);
			if (sources == null) {
				sources = new SourceSequence();
				inputs.put(port, sources);
			}
			return sources;
		}

		/**
		 * Set a single value for an option. All values that are set on an option form a sequence.
		 */
		public Builder withOption(String name, String value) {
			if (options.containsKey(name)) {
				options.get(name).add(value);
			} else {
				List<String> values = Lists.newLinkedList();
				values.add(value);
				options.put(name, values);
			}
			return this;
		}

		private void checkInputURI(String uri) throws FileNotFoundException {
			try {
				checkInputURI(new URI(uri));
			} catch (URISyntaxException e) {
				throw new FileNotFoundException(
					"Input not found: not a valid URI: " + uri);
			}
		}

		private InputStream checkInputURI(URL uri) throws FileNotFoundException {
			try {
				return uri.openStream();
			} catch (IOException e) {
				throw new FileNotFoundException("Input not found: " + uri);
			}
		}

		private void checkInputURI(URI uri) throws FileNotFoundException {
			File absoluteFile = null;
			if ("file".equals(uri.getScheme())) {
				if (uri.isOpaque())
					throw new FileNotFoundException(
						"Input not found: expected an absolute file or a relative path, but got: " + uri);
				try {
					absoluteFile = new File(uri);
				} catch (IllegalArgumentException e) {
					throw new FileNotFoundException(
						"Input not found: not a valid file URI: " + uri);
				}
			}
			if (absoluteFile != null) {
				checkFile(absoluteFile);
			} else {
				if (uri.isAbsolute() || uri.getSchemeSpecificPart().startsWith("/"))
					throw new FileNotFoundException(
						"Input not found: expected an absolute file or a relative path, but got: " + uri);
				if (resources == null)
					throw new FileNotFoundException(
						"Input not found: a relative path was specified but no context provided: " + uri);
				if (!resourcePaths.contains(uri.normalize()))
					throw new FileNotFoundException(
						"Input not found within provided context: " + uri);
			}
		}

		private void checkFile(File file) throws FileNotFoundException {
			if (!file.isAbsolute())
				throw new FileNotFoundException(
					"Input not found: not an absolute file: " + file);
			if (!file.exists())
				throw new FileNotFoundException(
					"Input not found: file does not exist: " + file);
		}

		/**
		 * Build the {@link ScriptInput}
		 */
		public ScriptInput build() {
			return new ScriptInput(resources, inputs, options);
		}
	}

	private final JobResources resources;
	private final Map<String,SourceSequence> inputs;
	private final Map<String,List<String>> options;
	private boolean storedToDisk = false;
	private final static List<Source> emptySources = ImmutableList.of();
	private final static List<String> emptyValues = ImmutableList.of();

	private ScriptInput(JobResources resources, Map<String,SourceSequence> inputs, Map<String,List<String>> options) {
		this.resources = resources;
		this.inputs = inputs;
		this.options = options;
	}

	/**
	 * Get all documents on an input port.
	 *
	 * The returned {@link Source} should only be consumed once.
	 */
	public Iterable<Source> getInput(String port) {
		return inputs.containsKey(port)
			? ImmutableList.copyOf(inputs.get(port))
			: emptySources;
	}

	/**
	 * Get the sequence of values for an option.
	 */
	public Iterable<String> getOption(String name) {
		return options.containsKey(name)
			? ImmutableList.copyOf(options.get(name))
			: emptyValues;
	}

	/**
	 * Get the resource collection.
	 */
	public JobResources getResources() {
		return resources;
	}

	/**
	 * Ensure all documents on input ports are stored to disk
	 *
	 * @param baseDir The directory does not have to exist yet, and may be
	 *                {@code null}, in which case a temporary directory will be
	 *                created.
	 */
	public ScriptInput storeToDisk(File baseDir) throws IOException {
		if (storedToDisk)
			return this;
		boolean everythingStored = true;
		if (this.resources != null)
			everythingStored = false;
		else
			ports: for (String port : this.inputs.keySet())
				for (Source src : this.inputs.get(port))
					if (!isStoredOnDisk(src)) {
						everythingStored = false;
						break ports; }
		if (everythingStored) {
			storedToDisk = true;
			return this;
		}
		JobResources resources = this.resources;
		Map<String,SourceSequence> inputs = Maps.newHashMap();
		if (this.resources != null) {
			if (baseDir == null)
				baseDir = Files.createTempDirectory(null).toFile();
			baseDir.mkdirs();
			IOHelper.dump(this.resources, baseDir.toURI());
			resources = new JobResourcesDir(baseDir);
			// just to be sure we don't use the same directory for files that come from the ZIP context
			// and inline documents from the job request XML
			baseDir = null;
		}
		inputs = Maps.newHashMap();
		for (String port : this.inputs.keySet()) {
			SourceSequence sources = new SourceSequence();
			inputs.put(port, sources);
			int inputCnt = 0; // number of inputs for this port
			for (Source src : this.inputs.get(port)) {
				InputSource is = SAXSource.sourceToInputSource(src);
				if (is != null && (is.getByteStream() != null || is.getCharacterStream() != null)) {
					// this is the case when the document comes from the job request XML for instance
					if (baseDir == null)
						baseDir = Files.createTempDirectory(null).toFile();
					// give the file a name that resembles the original name if possible
					String fileName; {
						try {
							fileName = new File(is.getSystemId()).getName();
						} catch (Throwable e) {
							fileName = "";
						}
						if ("".equals(fileName))
							fileName = port + '-' + inputCnt + ".xml";
					}
					File f = new File(baseDir, fileName);
					InputStream s = is.getByteStream();
					if (s == null)  {
						Reader reader = is.getCharacterStream();
						String encoding = is.getEncoding();
						if (encoding == null)
							encoding = "UTF-8";
						s = new ByteArrayInputStream(CharStreams.toString(reader).getBytes(encoding));
					}
					f.getParentFile().mkdirs();
					f.deleteOnExit();
					IOHelper.dump(s, new FileOutputStream(f));
					sources.add(new LazySaxSourceProvider(f.toURI().toASCIIString()));
				} else
					sources.add(src);
				inputCnt++;
			}
		}
		ScriptInput i = new ScriptInput(resources, inputs, options);
		i.storedToDisk = true;
		return i;
	}

	private static boolean isStoredOnDisk(Source src) {
		InputSource is = SAXSource.sourceToInputSource(src);
		if (is != null && (is.getByteStream() != null || is.getCharacterStream() != null))
			return false;
		else
			return true;
	}

	public ScriptInput storeToDisk() throws IOException {
		return storeToDisk(null);
	}

	private static class SourceSequence implements Iterable<Source> {

		private Iterable<Source> iterable;
		private List<Source> sources;
		private List<Supplier<Source>> suppliers;
		private boolean locked = false;

		public void add(Source source) {
			if (locked) throw new UnsupportedOperationException("already iterated");
			if (suppliers != null) {
				iterable = concat(iterable, Iterables.transform(suppliers, Supplier::get));
				suppliers = null;
			}
			if (sources == null)
				sources = new ArrayList<>();
			sources.add(source);
		}

		public void add(Supplier<Source> source) {
			if (locked) throw new UnsupportedOperationException("already iterated");
			if (sources != null) {
				iterable = concat(iterable, sources);
				sources = null;
			}
			if (suppliers == null)
				suppliers = new ArrayList<>();
			suppliers.add(source);
		}

		@Override
		public Iterator<Source> iterator() {
			if (sources != null) {
				iterable = concat(iterable, sources);
				sources = null;
			} else if (suppliers != null) {
				iterable = concat(iterable, Iterables.transform(suppliers, Supplier::get));
				suppliers = null;
			}
			if (iterable == null)
				iterable = Collections.emptyList();
			locked = true;
			return iterable.iterator();
		}

		private static <T> Iterable<T> concat(Iterable<T> head, Iterable<T> tail) {
			if (head == null)
				return tail;
			else if (tail == null)
				return head;
			else
				return Iterables.concat(head, tail);
		}
	}
}
