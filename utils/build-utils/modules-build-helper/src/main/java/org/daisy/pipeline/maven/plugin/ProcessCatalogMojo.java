package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.calabash.Calabash;

/*
 * copied from pipeline-mod-braille/maven/build-helper
 */
@Mojo(
	name = "process-catalog",
	defaultPhase = LifecyclePhase.GENERATE_RESOURCES
)
public class ProcessCatalogMojo extends AbstractMojo {
	
	@Parameter(
		defaultValue = "${project.basedir}/src/main/resources/META-INF/catalog.xml"
	)
	private File catalogFile;
	
	@Parameter(
		defaultValue = "${project.build.directory}/generated-resources/process-catalog/"
	)
	private File outputDirectory;
	
	@Parameter(
		defaultValue = "true"
	)
	private boolean addResources;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project.version}"
	)
	private String projectVersion;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project}"
	)
	private MavenProject mavenProject;
	
	public void execute() throws MojoFailureException {
		try {
			XProcEngine engine = new Calabash();
			engine.run(asURI(this.getClass().getResource("/process-catalog/process-catalog.xpl")).toASCIIString(),
			           ImmutableMap.of("source", (List<String>)ImmutableList.of(asURI(catalogFile).toASCIIString())),
			           null,
			           ImmutableMap.of("outputDir", asURI(outputDirectory).toASCIIString(),
			                           "version", projectVersion),
			           null);
			if (addResources) {
				Resource generatedResources = new Resource(); {
					generatedResources.setDirectory(outputDirectory.getAbsolutePath());
					List<String> includes = new ArrayList<String>(); {
						includes.add("META-INF/catalog.xml");
					}
					generatedResources.setIncludes(includes);
				}
				mavenProject.addResource(generatedResources);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
	
	// copied from org.daisy.pipeline.braille.common.util.URIs.asURI
	private static URI asURI(Object o) {
		if (o == null)
			return null;
		try {
			if (o instanceof String)
				return new URI((String)o);
			if (o instanceof File)
				return ((File)o).toURI();
			if (o instanceof URL) {
				URL url = (URL)o;
				if (url.getProtocol().equals("jar"))
					return new URI("jar:" + new URI(null, url.getAuthority(), url.getPath(), url.getQuery(), url.getRef()).toASCIIString());
				String authority = (url.getPort() != -1) ?
					url.getHost() + ":" + url.getPort() :
					url.getHost();
				return new URI(url.getProtocol(), authority, url.getPath(), url.getQuery(), url.getRef()); }
			if (o instanceof URI)
				return (URI)o; }
		catch (Exception e) {}
		throw new RuntimeException("Object can not be converted to URI: " + o);
	}
}
