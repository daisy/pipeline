package org.daisy.pipeline.pipeline1.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

// Pipeline 1
import org.daisy.pipeline.exception.DMFCConfigurationException;
import org.daisy.pipeline.exception.TransformerDisabledException;
import org.daisy.pipeline.core.DirClassLoader;
import org.daisy.pipeline.core.PipelineCore;
import org.daisy.pipeline.core.script.Creator;
import org.daisy.pipeline.core.script.ScriptValidationException;
import org.daisy.pipeline.core.transformer.AbstractTransformerLoader;
import org.daisy.pipeline.core.transformer.TransformerHandler;

// Pipeline 2
import org.daisy.common.file.URLs;
import org.daisy.common.properties.Properties;
import org.daisy.common.spi.annotations.LoadWith;
import org.daisy.pipeline.datatypes.DatatypeRegistry;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.script.ScriptServiceProvider;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "Pipeline1ScriptProvider",
	service = {
		ScriptServiceProvider.class
	}
)
@LoadWith(Pipeline1ClassLoader.class)
public class Pipeline1ScriptProvider implements ScriptServiceProvider {

	private Path transformersDir = null;
	private File tempDir = null;
	DatatypeRegistry datatypeRegistry = null;
	PipelineCore core = null;
	private List<ScriptService<?>> scripts;
	boolean closed = false;

	private static final Map<String,String> taskScripts = ImmutableMap.of(
		"dtbook-to-latex", "scripts/create_distribute/latex/DTBookToLaTeX.taskScript"
	);

	@Override
	public Iterable<ScriptService<?>> getScripts() {
		if (closed)
			throw new IllegalStateException("script provider is closed");
		return scripts;
	}

	@Reference(
		name = "datatype-registry",
		unbind = "-",
		service = DatatypeRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setDatatypeRegistry(DatatypeRegistry registry) {
		datatypeRegistry = registry;
	}

	@Activate
	protected void init() {
		try {
			transformersDir = URLs.getResourceFromJAR("/transformers.jar",
			                                          URLs.getCurrentJAR(Pipeline1ScriptProvider.class).toPath());
			// In order to support TDF files with a ${transformer_dir} parameter, the JAR must be exploded.
			try (FileSystem fs = FileSystems.newFileSystem(transformersDir, (ClassLoader)null)) {
				transformersDir = copyFolder(fs.getPath("/"), Files.createTempDirectory(null));
			}
			// (Note that without this requirement, we would still have to extract the JAR, because URL
			// and ClassLoader do not support nested JARs, however we wouldn't have to explode it.)
			//transformersDir = copyFile(transformersDir, Files.createTempFile(null, ".jar"));
			//transformersDir.toFile().deleteOnExit();
			//transformersDir = FileSystems.newFileSystem(transformersDir, (ClassLoader)null).getPath("/");
			tempDir = Files.createTempDirectory(null).toFile();
			scripts = new ArrayList<>();
			try (ThreadLocalEnvironment _env = new ThreadLocalEnvironment(getClass().getClassLoader(),
			                                                              System.getProperties())) {
				System.setProperty("pipeline.tempDir", tempDir.getAbsolutePath());
				core = new MyPipelineCore();
			}
			String version; {
				java.util.Properties mavenProps = new java.util.Properties();
				mavenProps.load(getClass().getResourceAsStream("/maven.properties"));
				version = mavenProps.getProperty("pipeline1.version");
			}
			for (String id : taskScripts.keySet()) {
				scripts.add(
					new ScriptService<Pipeline1Script>() {
						private Pipeline1Script script = null;
						public String getId() {
							return id;
						}
						public String getVersion() {
							return version;
						}
						public Pipeline1Script load() {
							if (closed)
								throw new IllegalStateException("script provider is closed");
							if (script == null) {
								ClassLoader cl = getClass().getClassLoader();
								try (ThreadLocalEnvironment _env = new ThreadLocalEnvironment(cl)) {
									script = new Pipeline1Script.Builder(
										Pipeline1ScriptProvider.this,
										this,
										core.newScript(cl.getResource(taskScripts.get(id)))
									).build();
								} catch (ScriptValidationException e) {
									throw new IllegalStateException(e);
								}
							}
							return script;
						}
					}
				);
			}
			scripts = Collections.unmodifiableList(scripts);
		} catch (DMFCConfigurationException|IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Deactivate
	protected void close() {
		if (!closed) {
			if (tempDir != null)
				deleteDir(tempDir);
			try {
				deleteDir(transformersDir.toFile());
			} catch (UnsupportedOperationException e) {
			}
			closed = true;
		}
	}

	private static class Pipeline1ScriptService implements ScriptService<Pipeline1Script> {

		private final String id;
		private final String version;
		private Pipeline1Script script;

		Pipeline1ScriptService(String id, String version) {
			this.id = id;
			this.version = version;
			this.script = null;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getVersion() {
			return version;
		}

		@Override
		public Pipeline1Script load() {
			if (script == null) {
				throw new UnsupportedOperationException("FIXME");
			}
			return script;
		}
	}

	private class MyPipelineCore extends PipelineCore {

		MyPipelineCore() throws DMFCConfigurationException {
			super(null, new File("/irrelevant"), new java.util.Properties(), new java.util.Properties());
			mCreator = new MyCreator();
		}
	}

	private class MyCreator extends Creator {

		MyCreator() {
			super();
		}

		@Override
		protected TransformerHandler getTransformerHandler(String transformerName) throws TransformerDisabledException {
			try {
				Path transformerDir = transformersDir.resolve(transformerName.replace('.', '/'));
				if (Files.exists(transformerDir)
				    && Files.getFileAttributeView(transformerDir, BasicFileAttributeView.class).readAttributes().isDirectory()) {
					for (Iterator<String> files = URLs.listResourcesFromJAR("/", transformerDir); files.hasNext();) {
						String f = files.next();
						f = f.substring(1); // trim leading '/'
						if (f.endsWith(".tdf"))
							return new TransformerHandler(new MyTransformerLoader(transformerDir.resolve(f)));
					}
				}
			} catch (IOException e) {
				throw new IllegalStateException(); // should not happen
			}
			return null;
		}
	}

	private class MyTransformerLoader extends AbstractTransformerLoader {

		private final Path tdfFile;

		MyTransformerLoader(Path tdfFile) {
			super(null);
			this.tdfFile = tdfFile;
		}

		@Override
		public URL getTdfUrl() throws MalformedURLException {
			return tdfFile.toUri().toURL();
		}

		@Override
		public File getTransformerDir() {
			try {
				return tdfFile.toFile().getParentFile();
			} catch (UnsupportedOperationException e) {
				return null;
			}
		}

		@Override
		protected ClassLoader getClassLoader(Collection<String> jars) throws IOException {
			File dir = getTransformerDir();
			if (dir != null) {
				File transformersDir = Pipeline1ScriptProvider.this.transformersDir.toFile();
				DirClassLoader classLoader = new DirClassLoader(transformersDir, transformersDir);
				for (String jar : jars)
					classLoader.addJar(new File(dir, jar));
				return classLoader;
			} else {
				URLClassLoader classLoader = new URLClassLoader(new URL[]{}, getClass().getClassLoader()) {{
					try {
						addURL(transformersDir.toUri().toURL());
						for (String jar : jars) {
							Path jarFile = tdfFile.getParent().resolve(jar);
							// copy to temporary file if needed, because URL and ClassLoader do not support nested JARs
							if (jarFile.toUri().toString().matches("^jar:.+\\.jar$")) {
								jarFile = copyFile(jarFile, Files.createTempFile(null, ".jar"));
								jarFile.toFile().deleteOnExit();
							}
							addURL(jarFile.toUri().toURL());
						}
					} catch (MalformedURLException e) {
						throw new IOException(e);
					}
				}};
				return classLoader;
			}
		}

		@Override
		protected boolean isLoadedFromJar() {
			return tdfFile.toUri().toString().startsWith("jar:");
		}
	}

	private static Path copyFile(Path source, Path target) throws IOException {
		Files.copy(source, Files.newOutputStream(target));
		return target;
	}

	private static Path copyFolder(Path source, Path target) throws IOException {
		Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
					Files.createDirectories(target.resolve(source.relativize(dir).toString()));
					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.copy(file, target.resolve(source.relativize(file).toString()));
					return FileVisitResult.CONTINUE;
				}
			});
		return target;
	}

	private static boolean deleteDir(File dir) {
		File[] files = dir.listFiles();
		if (files != null)
			for (File f : files) {
				if (f.isDirectory()) {
					deleteDir(f);
				}
				f.delete();
			}
		return dir.delete();
	}
}
