package org.daisy.pipeline.script;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import com.google.common.io.CharStreams;

import org.daisy.common.file.Resource;
import org.daisy.common.file.URLs;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.JobResourcesDir;
import org.daisy.pipeline.job.impl.IOHelper;

import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class ScriptInput {

	public static class Builder {

		final Map<String,InputSequence> inputs = Maps.newHashMap();
		final Map<String,List<String>> options = Maps.newHashMap();
		private final JobResources resources;
		private final Map<URI,AtomicReference<Resource>> resourceRefs = Maps.newHashMap();
		private final Set<AtomicReference<Resource>> extraResources = Sets.newHashSet();

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
			if (resources != null)
				for (URI name : resources.getNames())
					resourceRefs.put(name, new AtomicReference<>(resources.getResource(name).copy(name)));
		}

		/**
		 * Resolve relative inputs against a base directory.
		 */
		public Builder(File baseDir) {
			this(new JobResourcesDir(baseDir));
		}

		/**
		 * Put a single XML document on the specified input port. All documents that are put on a port
		 * form a sequence.
		 *
		 * @throws IllegalArgumentException if no {@link InputStream} or {@link Reader} can be
		 *         obtained from <code>input</code> and <code>input</code> has an empty system ID.
		 *         (Note that there is currently no special support for
		 *         {@link javax.xml.transform.dom.DOMSource} and {@link javax.xml.transform.stax.StAXSource}.)
		 * @throws FileNotFoundException if no {@link InputStream} or {@link Reader} can be obtained
		 *         from <code>input</code> and the system ID can not be resolved to a document.
		 */
		public Builder withInput(String port, Source input) throws IllegalArgumentException, FileNotFoundException {
			InputSource is = SAXSource.sourceToInputSource(input);
			if (is == null || (is.getByteStream() == null && is.getCharacterStream() == null)) {
				String sysId = input.getSystemId();
				if (sysId == null || "".equals(sysId)) {
					throw new IllegalArgumentException(
						"Input is expected to either be a stream or have non empty system ID");
				}
				try {
					return withInput(port, new URI(sysId));
				} catch (URISyntaxException e) {
					throw new FileNotFoundException(
						"Input not found: not a valid URI: " + sysId);
				}
			} else {
				// this is the case when the document comes from the job request XML for instance
				Resource res; {
					URI path = URI.create(generateFileName(is.getSystemId(), port));
					InputStream s = is.getByteStream();
					if (s != null)
						res = Resource.load(s, path, Resource.MEDIA_TYPE_UNKNOWN);
					else {
						Reader reader = is.getCharacterStream();
						res = Resource.load(() -> {
								try {
									String encoding = is.getEncoding();
									if (encoding == null)
										encoding = "UTF-8";
									return new ByteArrayInputStream(CharStreams.toString(reader).getBytes(encoding));
								} catch (IOException e) {
									throw new UncheckedIOException(e);
								}
							},
							path,
							Resource.MEDIA_TYPE_UNKNOWN);
					}
				}
				getSequence(port).add(addExtraResource(res));
				return this;
			}
		}

		/**
		 * Put a single file on the specified input port. All files that are put on a port
		 * form a sequence.
		 *
		 * @throws FileNotFoundException if the URI can not be resolved to a file.
		 */
		public Builder withInput(String port, URI input) throws FileNotFoundException {
			File absoluteFile = null;
			if ("file".equals(input.getScheme())) {
				if (input.isOpaque())
					throw new FileNotFoundException(
						"Input not found: expected an absolute file or a relative path, but got: " + input);
				try {
					absoluteFile = new File(input);
				} catch (IllegalArgumentException e) {
					throw new FileNotFoundException(
						"Input not found: not a valid file URI: " + input);
				}
			}
			if (absoluteFile != null) {
				return withInput(port, absoluteFile);
			} else {
				if (input.isAbsolute() || input.getSchemeSpecificPart().startsWith("/"))
					throw new FileNotFoundException(
						"Input not found: expected an absolute file or a relative path, but got: " + input);
				if (resources == null)
					throw new FileNotFoundException(
						"Input not found: a relative path was specified but no context provided: " + input);
				AtomicReference<Resource> res = resourceRefs.get(input.normalize());
				if (res == null)
					throw new FileNotFoundException(
						"Input not found within provided context: " + input);
				getSequence(port).add(res);
				return this;
			}
		}

		/**
		 * Put a single file on the specified input port. All files that are put on a port
		 * form a sequence.
		 *
		 * @throws FileNotFoundException if <code>input</code> does not exist.
		 */
		public Builder withInput(String port, File input) throws FileNotFoundException {
			if (!input.isAbsolute())
				throw new FileNotFoundException(
					"Input not found: not an absolute file: " + input);
			if (!input.exists())
				throw new FileNotFoundException(
					"Input not found: file does not exist: " + input);
			getSequence(port).add(Resource.load(input, Resource.MEDIA_TYPE_UNKNOWN));
			return this;
		}

		/**
		 * Put a single file on the specified input port. All files that are put on a port
		 * form a sequence.
		 *
		 * @throws FileNotFoundException if the URL can not be resolved to a file.
		 */
		public Builder withInput(String port, URL input) throws FileNotFoundException {
			try {
				if ("file".equals(input.getProtocol()))
					return withInput(port, URLs.asURI(input));
				Resource res = Resource.load(
					input.openStream(), URLs.asURI(input), Resource.MEDIA_TYPE_UNKNOWN);
				res = res.copy(URI.create(generateFileName(res.getPath().toString(), port)));
				getSequence(port).add(addExtraResource(res));
				return this;

			} catch (IOException e) {
				throw new FileNotFoundException("Input not found: " + input);
			}
		}

		/**
		 * Put a single file on the specified input port. All files that are put on a port
		 * form a sequence.
		 */
		public Builder withInput(String port, InputStream source) {
			Resource res = Resource.load(source, URI.create(generateFileName(port)), Resource.MEDIA_TYPE_UNKNOWN);
			getSequence(port).add(addExtraResource(res));
			return this;
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

		private AtomicReference<Resource> addExtraResource(Resource resource) {
			AtomicReference<Resource> ref = new AtomicReference<>(resource);
			extraResources.add(ref);
			return ref;
		}

		private InputSequence getSequence(String port) {
			InputSequence seq = inputs.get(port);
			if (seq == null) {
				seq = new InputSequence();
				inputs.put(port, seq);
			}
			return seq;
		}

		private String generateFileName(String port) {
			return generateFileName(null, port);
		}

		private String generateFileName(String uri, String port) {
			String fileName = "";
			if (uri != null)
				try {
					// give the file a name that resembles the original name if possible
					fileName = new File(uri).getName();
				} catch (Throwable e) {
				}
			if ("".equals(fileName))
				fileName = port + '-' + getSequence(port).size() + ".xml";
			return fileName;
		}

		/**
		 * Build the {@link ScriptInput}
		 */
		public ScriptInput build() {
			JobResources resources = this.resources; {
				if (!extraResources.isEmpty()) {
					List<Resource> allResources = new ArrayList<>(); {
						if (resources == null || resources.getNames().isEmpty())
							for (AtomicReference<Resource> res : extraResources)
								allResources.add(res.get());
						else {
							URI subdir1 = URI.create("zip");
							for (AtomicReference<Resource> res : resourceRefs.values())
								allResources.add(res.updateAndGet(x -> x.copy(URLs.resolve(subdir1, x.getPath()))));
							URI subdir2 = URI.create("inline");
							for (AtomicReference<Resource> res : extraResources)
								allResources.add(res.updateAndGet(x -> x.copy(URLs.resolve(subdir2, x.getPath()))));
						}
					}
					Map<URI,Resource> resourcesMap; {
						ImmutableMap.Builder<URI, Resource> b = ImmutableMap.builder();
						for (Resource res : allResources)
							b.put(res.getPath(), res);
						resourcesMap = b.build();
					}
					resources = new JobResources() {
							@Override
							public Set<URI> getNames() {
								return resourcesMap.keySet();
							}
							@Override
							public Resource getResource(URI name) {
								return resourcesMap.get(name);
							}
						};
				}
			}
			return new ScriptInput(
				resources,
				inputs,
				options);
		}
	}

	private final JobResources resources;
	private final Map<String,InputSequence> inputs;
	private final Map<String,List<String>> options;
	private boolean storedToDisk = false;
	private final static List<URI> emptyInput = ImmutableList.of();
	private final static List<String> emptyOption = ImmutableList.of();

	private ScriptInput(JobResources resources, Map<String,InputSequence> inputs, Map<String,List<String>> options) {
		this.resources = resources;
		this.inputs = inputs;
		this.options = options;
	}

	/**
	 * Get all files on an input port.
	 *
	 * The returned {@link Source} should only be consumed once.
	 */
	public Iterable<URI> getInput(String port) {
		return inputs.containsKey(port)
			? ImmutableList.copyOf(inputs.get(port))
			: emptyInput;
	}

	/**
	 * Get the sequence of values for an option.
	 */
	public Iterable<String> getOption(String name) {
		return options.containsKey(name)
			? ImmutableList.copyOf(options.get(name))
			: emptyOption;
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
			for (InputSequence port : inputs.values())
				if (!port.isStoredOnDisk()){
					everythingStored = false;
					break; }
		if (everythingStored) {
			storedToDisk = true;
			return this;
		}
		JobResources resources = null; {
			if (this.resources != null) {
				if (baseDir == null)
					baseDir = Files.createTempDirectory(null).toFile();
				baseDir.mkdirs();
				IOHelper.dump(this.resources, baseDir.toURI());
				resources = new JobResourcesDir(baseDir);
			}
		}
		ScriptInput i = new ScriptInput(resources, inputs, options);
		i.storedToDisk = true;
		return i;
	}

	public ScriptInput storeToDisk() throws IOException {
		return storeToDisk(null);
	}

	private static class InputSequence implements Iterable<URI> {

		private List<AtomicReference<Resource>> seq;
		private boolean locked = false;

		public int size() {
			return seq != null ?  seq.size() : 0;
		}

		public void add(Resource input) {
			add(new AtomicReference<>(input));
		}

		public void add(AtomicReference<Resource> input) {
			if (locked) throw new UnsupportedOperationException("already iterated");
			if (seq == null)
				seq = new ArrayList<>();
			seq.add(input);
		}

		@Override
		public Iterator<URI> iterator() {
			if (seq == null)
				seq = Collections.emptyList();
			locked = true;
			return Iterators.transform(seq.iterator(), r -> r.get().getPath());
		}

		public boolean isStoredOnDisk() {
			if (seq != null)
				try {
					for (AtomicReference<Resource> r : seq)
						r.get().readAsFile();
				} catch (UnsupportedOperationException e) {
					return false;
				}
			return true;
		}
	}
}
