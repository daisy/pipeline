package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.List;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

@Mojo(
	name = "dependencies-package-list",
	defaultPhase = LifecyclePhase.PREPARE_PACKAGE,
	requiresDependencyResolution = ResolutionScope.COMPILE
)
public class DependenciesPackageListMojo extends AbstractMojo {
	
	@Parameter(
		defaultValue = "${project.build.directory}/dependencies-package-list/"
	)
	private File outputDirectory;
	
	@Parameter(
		required = true
	)
	private String includes;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project}"
	)
	private MavenProject mavenProject;
	
	public void execute() throws MojoFailureException {
		try {
			Set<String> packages = new HashSet<String>(); {
				for (String p : mavenProject.getCompileClasspathElements()) {
					File f = new File(p);
					if (!f.exists())
						continue;
					if (f.isDirectory()) {
						// this is probably the target/classes directory, which is not a dependency
					} else {
						JarFile jar = new JarFile(f);
						for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();) {
							JarEntry e = entries.nextElement();
							if (!e.isDirectory()) {
								String name = e.getName();
								if (name.endsWith(".class")) {
									int i = name.lastIndexOf('/');
									if (i > 0)
										packages.add(name.substring(0, i).replace("/", "."));
								}
							}
						}
					}
				}
			}
			List<String> sortedPackages = new ArrayList(packages);
			Collections.sort(sortedPackages);
			String[] includesArray = includes.replaceAll("\\s", "").split(",(?![^{]*})");
			outputDirectory.mkdirs();
			PrintWriter writer = new PrintWriter(new File(outputDirectory, "package-list"), "UTF-8");
			for (String p : sortedPackages)
				for (String i : includesArray)
					if (i.equals(p) || i.endsWith(".*") && p.startsWith(i.substring(0, i.length() - 1)))
						writer.println(p);
			writer.close();
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
}
