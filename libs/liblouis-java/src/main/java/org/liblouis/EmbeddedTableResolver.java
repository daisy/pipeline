package org.liblouis;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.BasicFileAttributeView;
import static java.nio.file.Files.walkFileTree;
import java.nio.file.Files;
import java.nio.file.FileSystem;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.NoSuchFileException;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.liblouis.Louis.asFile;
import static org.liblouis.Louis.asURL;

/**
 * Default table resolver implementation that looks for tables inside this JAR and falls back to the file system
 */
class EmbeddedTableResolver implements TableResolver {
	
	private final Map<String,URL> tables;
	private final Set<String> tablePaths;
	private final Map<String,URL> aggregatorTables = new HashMap<String,URL>();
	
	EmbeddedTableResolver(org.slf4j.Logger logger) {
		tables = new HashMap<String,URL>();
		File resourcesJarFile; {
			URL knownResource = Louis.class.getClassLoader().getResource("darwin-aarch64/liblouis.dylib");
			if (knownResource == null)
				throw new RuntimeException("resources artifact not on classpath");
			else if ("file".equals(knownResource.getProtocol()))
				resourcesJarFile = asFile(knownResource).getParentFile().getParentFile();
			else if ("jar".equals(knownResource.getProtocol()))
				resourcesJarFile = new File(
					URI.create(
						knownResource.toExternalForm().substring(4, knownResource.toExternalForm().indexOf("!/"))));
			else
				throw new RuntimeException(); // don't know what to do with this
		}
		for (String table : listResources("org/liblouis/resource-files/tables", resourcesJarFile))
			tables.put(table, getClass().getClassLoader().getResource("org/liblouis/resource-files/tables/" + table));
		tablePaths = Collections.unmodifiableSet(tables.keySet());
		logger.debug("Using default tables");
		logger.trace("Table files: " + tablePaths);
	}
	
	public URL resolve(String table, URL base) {
		// if we are resolving an include rule from a generated aggregator table, resolve without base
		if (aggregatorTables.containsValue(base))
			base = null;
		if (base == null || tables.containsValue(base)) {
			if (tables.containsKey(table))
				return tables.get(table);
		}
		// if it is a comma separated table list, create a single file that includes all the sub-tables
		if (base == null && table.contains(",")) {
			if (aggregatorTables.containsKey(table))
				return aggregatorTables.get(table);
			StringBuilder b = new StringBuilder();
			for (String s : table.split(","))
				// replace "\" (file separator on Windows) with "\\" and " " (space in file path) with "\s"
				b.append("include ").append(s.replaceAll("\\\\", "\\\\\\\\").replaceAll(" ", "\\\\s")).append('\n');
			InputStream in = new ByteArrayInputStream(b.toString().getBytes(StandardCharsets.UTF_8));
			try {
				File f = File.createTempFile("liblouis-java-", ".tbl");
				f.delete();
				Files.copy(in, f.toPath());
				f.deleteOnExit();
				URL u = asURL(f);
				aggregatorTables.put(table, u);
				return u;
			} catch (IOException e) {
				throw new RuntimeException(e); // should not happen
			}
		}
		// try file system
		if (base != null && base.toString().startsWith("file:")) {
			File f = base.toString().endsWith("/")
				? new File(asFile(base), table)
				: new File(asFile(base).getParentFile(), table);
			if (f.exists())
				return asURL(f);
		} else if (base == null) {
			File f = new File(table);
			if (f.exists())
				return asURL(f);
		}
		return null; // table cannot be resolved
	}
	
	public Set<String> list() {
		return tablePaths;
	}
	
	private static Iterable<String> listResources(final String directory, Class<?> context) {
		return listResources(directory, asFile(context.getProtectionDomain().getCodeSource().getLocation()));
	}
	
	private static Iterable<String> listResources(final String directory, File jarFile) {
		if (!jarFile.exists())
			throw new RuntimeException();
		else if (jarFile.isDirectory()) {
			File d = new File(jarFile, directory);
			if (!d.exists())
				throw new RuntimeException("directory does not exist");
			else if (!d.isDirectory())
				throw new RuntimeException("is not a directory");
			else {
				List<String> resources = new ArrayList<String>();
				for (File f : d.listFiles())
					resources.add(f.getName() + (f.isDirectory() ? "/" : ""));
				return resources; }}
		else {
			FileSystem fs; {
				try {
					fs = FileSystems.newFileSystem(URI.create("jar:" + jarFile.toURI()),
					                               Collections.<String,Object>emptyMap()); }
				catch (IOException e) {
					throw new RuntimeException(e); }}
			try {
				Path d = fs.getPath("/" + directory);
				BasicFileAttributes a; {
					try {
						a = Files.getFileAttributeView(d, BasicFileAttributeView.class).readAttributes(); }
					catch (NoSuchFileException e) {
						throw new RuntimeException("directory does not exist"); }
					catch (FileSystemNotFoundException e) {
						throw new RuntimeException(e); }
					catch (IOException e) {
						throw new RuntimeException(e); }}
				if (!a.isDirectory())
					throw new RuntimeException("is not a directory");
				final List<String> resources = new ArrayList<String>();
				try {
					walkFileTree(d, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
							public FileVisitResult visitFile(Path f, BasicFileAttributes _) throws IOException {
								resources.add(""+f.getFileName());
								return FileVisitResult.CONTINUE; }}); }
				catch (NoSuchFileException e) {
					throw new RuntimeException(e); }
				catch (IOException e) {
					throw new RuntimeException(e); }
				return resources; }
			finally {
				try {
					fs.close(); }
				catch (IOException e) {
					throw new RuntimeException(e); }
			}
		}
	}
}
