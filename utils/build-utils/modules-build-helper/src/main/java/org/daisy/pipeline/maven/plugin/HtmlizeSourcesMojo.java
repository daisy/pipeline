package org.daisy.pipeline.maven.plugin;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.util.DirectoryScanner;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.api.XProcExecutionException;

import static org.daisy.pipeline.maven.plugin.utils.URLs.asURI;
import static org.daisy.pipeline.maven.plugin.utils.URLs.relativize;
import static org.daisy.pipeline.maven.plugin.utils.XML.evaluateXPath;
import static org.daisy.pipeline.maven.plugin.utils.XML.transform;
import org.daisy.pipeline.modules.impl.ModuleUriResolver;

/**
 * @goal htmlize-sources
 * @requiresDependencyResolution compile
 */
public class HtmlizeSourcesMojo extends AbstractMojo {
	
	/**
	 * @parameter expression="${project.basedir}/src/main/resources"
	 * @required
	 */
	private File sourceDirectory;
	
	/**
	 * @parameter expression="${project.basedir}/src/main/resources/META-INF/catalog.xml"
	 * @required
	 */
	private File catalogXmlFile;
	
	/**
	 * @parameter
	 */
	private String includes;
	private final String defaultIncludes = "**/*.xpl,**/*.xsl,**/*.css";
	
	/**
	 * @parameter expression="${project.build.directory}/generated-resources/htmlize-sources"
	 * @required
	 */
	private File outputDirectory;
	
	/**
	 * @parameter expression="${project}"
	 */
	private MavenProject mavenProject;
	
	private final static Pattern PSEUDO_ATTR_RE = Pattern.compile("(href|type|title|media|charset|alternate)=(\"([^\"]+)\"|'([^']+)')");
	private final static Pattern STYLESHEET_RE = Pattern.compile("^\\s*" + PSEUDO_ATTR_RE + "(\\s+" + PSEUDO_ATTR_RE + ")*\\s*$");
	
	public void execute() throws MojoFailureException {
		try {
			final List<String> compileClassPath = mavenProject.getCompileClasspathElements();
			final XProcEngine engine = new CalabashWithPipelineModules(compileClassPath);
			List<File> sources = new ArrayList<File>();
			if (includes == null)
				includes = defaultIncludes;
			String[] includesArray = includes.replaceAll("\\s", "").split(",(?![^{]*})");
			DirectoryScanner scanner = new DirectoryScanner();
			scanner.setBasedir(sourceDirectory);
			scanner.setIncludes(includesArray);
			scanner.setExcludes(new String[]{"**/.DS_Store"});
			scanner.scan();
			for (String f : scanner.getIncludedFiles())
				sources.add(new File(sourceDirectory, f));
			final Map<FilenameFilter,Htmlizer> htmlizers = new HashMap<FilenameFilter,Htmlizer>(); {
				final Map<String,Map<String,String>> params
					= ImmutableMap.of("parameters",
					                  (Map<String,String>)ImmutableMap.of("catalog-xml-uri", asURI(catalogXmlFile).toASCIIString()));
				htmlizers.put(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.equals("catalog.xml") && dir.getName().equals("META-INF"); }},
					new Htmlizer() {
						public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) throws XProcExecutionException {
							List<String> sourcesAsURIs = new ArrayList<String>();
							for (File f : sources)
								sourcesAsURIs.add(asURI(f).toASCIIString());
								engine.run(asURI(HtmlizeSourcesMojo.class.getResource("/htmlize-sources/htmlize-catalog.xpl")).toASCIIString(),
								           ImmutableMap.of("sources", sourcesAsURIs),
								           null,
								           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
								                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
								           params);
						}
					}
				);
				htmlizers.put(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".xpl") || name.endsWith(".params"); }},
					new Htmlizer() {
						public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) throws XProcExecutionException {
							List<String> sourcesAsURIs = new ArrayList<String>();
							for (File f : sources)
								sourcesAsURIs.add(asURI(f).toASCIIString());
								engine.run(asURI(HtmlizeSourcesMojo.class.getResource("/htmlize-sources/htmlize-xproc.xpl")).toASCIIString(),
								           ImmutableMap.of("sources", sourcesAsURIs),
								           null,
								           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
								                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
								           params);
						}
					}
				);
				htmlizers.put(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".xsl"); }},
					new Htmlizer() {
						public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) throws XProcExecutionException {
							List<String> sourcesAsURIs = new ArrayList<String>();
							for (File f : sources)
								sourcesAsURIs.add(asURI(f).toASCIIString());
								engine.run(asURI(HtmlizeSourcesMojo.class.getResource("/htmlize-sources/htmlize-xslt.xpl")).toASCIIString(),
								           ImmutableMap.of("sources", sourcesAsURIs),
								           null,
								           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
								                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
								           params);
						}
					}
				);
				htmlizers.put(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							return name.endsWith(".css") || name.endsWith(".scss"); }},
					new Htmlizer() {
						public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) {
							for (File f : sources) {
								try {
									File outputFile = new File(outputDirectory, relativize(asURI(sourceDirectory), asURI(f)) + "/index.md");
									outputFile.getParentFile().mkdirs();
									BufferedReader reader = new BufferedReader(new FileReader(f));
									BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
									writer.write("<link rev=\"doc\" href=\"../" + f.getName() + "\"/>");
									writer.newLine();
									writer.newLine();
									if (f.getName().endsWith(".scss"))
										writer.write("~~~sass");
									else
										writer.write("~~~css");
									writer.newLine();
									String line;
									while ((line = reader.readLine()) != null) {
										writer.write(line);
										writer.newLine(); }
									writer.write("~~~");
									writer.newLine();
									writer.close();
								} catch (Exception e) {
									throw new RuntimeException("Error processing file " + f, e);
								}
							}
						}
					}
				);
				htmlizers.put(
					new FilenameFilter() {
						public boolean accept(File dir, String name) {
							if (name.endsWith(".xpl") ||
							    name.endsWith(".params") ||
							    name.endsWith(".xsl") ||
							    (name.equals("catalog.xml") && dir.getName().equals("META-INF")))
								return false;
							File f = new File(dir, name);
							boolean isXml; {
								try {
									isXml = (Boolean)evaluateXPath(f, "/*", null, Boolean.class); }
								catch (RuntimeException e) {
									isXml = false; }}
							return isXml;
						}},
					new Htmlizer() {
						public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) {
							for (File f : sources) {
								File outputFile = new File(outputDirectory, relativize(asURI(sourceDirectory), asURI(f)) + "/index.html");
								URI xslt; {
									xslt = null;
									String instruction = (String)evaluateXPath(
										f, "/processing-instruction('xml-stylesheet')[1]", null, String.class);
									if (instruction != null) {
										Matcher m = STYLESHEET_RE.matcher(instruction);
										if (m.matches()) {
											m = PSEUDO_ATTR_RE.matcher(instruction);
											while (m.find())
												if ("href".equals(m.group(1))) {
													String href = m.group(3);
													if (href == null) href = m.group(4);
													xslt = asURI(f).resolve(href);
													try {
														if (!(Boolean)evaluateXPath(asURI(f).resolve(href),
														                            "/xsl:stylesheet",
														                            ImmutableMap.of("xsl", "http://www.w3.org/1999/XSL/Transform"),
														                            Boolean.class))
															xslt = null; }
													catch (RuntimeException e) {
														xslt = null; }
													break; }}
									}
								}
								if (xslt != null) {
									transform(f,
									          outputFile,
									          xslt,
									          null,
									          CalabashWithPipelineModules.getModuleUriResolver(compileClassPath));
								} else {
									List<String> sourceAsURI = new ArrayList<String>();
									engine.run(asURI(HtmlizeSourcesMojo.class.getResource("/htmlize-sources/htmlize-xml.xpl")).toASCIIString(),
									           ImmutableMap.of("sources", (List<String>)ImmutableList.of(asURI(f).toASCIIString())),
									           null,
									           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
									                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
									           params);
								}
							}
						}
					}
				);
			}
			final Htmlizer fallbackHtmlizer = new Htmlizer() {
				public void run(Iterable<File> sources, File sourceDirectory, File outputDirectory) throws XProcExecutionException {
					for (File f : sources) {
						try {
							File outputFile = new File(outputDirectory, relativize(asURI(sourceDirectory), asURI(f)) + "/index.md");
							outputFile.getParentFile().mkdirs();
							BufferedReader reader = new BufferedReader(new FileReader(f));
							BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
							writer.write("<link rev=\"doc\" href=\"../" + f.getName() + "\"/>");
							writer.newLine();
							writer.newLine();
							writer.write("~~~");
							writer.newLine();
							String line;
							while ((line = reader.readLine()) != null) {
								writer.write(line);
								writer.newLine(); }
							writer.write("~~~");
							writer.newLine();
							writer.close();
						} catch (Exception e) {
							throw new RuntimeException("Error processing file " + f, e);
						}
					}
				}
			};
			Multimap<Htmlizer,File> index = Multimaps.index(
				sources,
				new Function<File,Htmlizer>() {
					public Htmlizer apply(File f) {
						for (Map.Entry<FilenameFilter,Htmlizer> kv : htmlizers.entrySet())
							if (kv.getKey().accept(f.getParentFile(), f.getName()))
								return kv.getValue();
						return fallbackHtmlizer; }});
			outputDirectory.mkdirs();
			for (Map.Entry<Htmlizer,Collection<File>> kv : index.asMap().entrySet())
				kv.getKey().run(kv.getValue(), sourceDirectory, outputDirectory);
			
			// Generate directory index files
			List<String> includedFiles = new ArrayList<String>();
			Collections.addAll(includedFiles, scanner.getIncludedFiles());
			Collections.sort(includedFiles);
			List<String> includedDirectories = new ArrayList<String>();
			Collections.addAll(includedDirectories, scanner.getIncludedDirectories());
			for (String s : includesArray)
				if (".".equals(s) || "./".equals(s))
					includedDirectories.add("");
			for (String dir : includedDirectories) {
				File outputFile = new File(outputDirectory, ("".equals(dir) ? "" : (dir + "/")) + "index.md");
				outputFile.getParentFile().mkdirs();
				BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
				writer.write("<link rev=\"doc\" href=\"../" + outputFile.getParentFile().getName() + "/\"/>");
				writer.newLine();
				writer.newLine();
				int slash = dir.lastIndexOf('/');
				if (slash > 0) {
					String parent = dir.substring(0, slash);
					if (includedDirectories.contains(parent)) {
						writer.write("- [../](../)");
						writer.newLine();
					}
				}
				for (String d : includedDirectories) {
					if (!"".equals(d)) {
						slash = d.lastIndexOf('/');
						if (dir.equals(slash < 0 ? "" : d.substring(0, slash))) {
							d = d.substring(slash + 1);
							writer.write("- [" + d + "/](" + d + ")");
							writer.newLine();
						}
					}
				}
				for (Iterator<String> i = includedFiles.iterator(); i.hasNext();) {
					String f = i.next();
					slash = f.lastIndexOf('/');
					if (dir.equals(slash < 0 ? "" : f.substring(0, slash))) {
						f = f.substring(slash + 1);
						writer.write("- [" + f + "](" + f + ")");
						writer.newLine();
						i.remove();
					}
				}
				writer.close();
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
	
	private static interface Htmlizer {
		public void run(Iterable<File> files, File sourceDirectory, File outputDirectory);
	}
}
