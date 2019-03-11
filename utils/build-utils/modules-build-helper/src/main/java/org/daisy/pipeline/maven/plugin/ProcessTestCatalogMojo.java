package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import org.daisy.maven.xproc.api.XProcEngine;

import static org.daisy.pipeline.maven.plugin.utils.URIs.asURI;

@Mojo(
	name = "process-test-catalog",
	defaultPhase = LifecyclePhase.GENERATE_TEST_RESOURCES,
	requiresDependencyResolution = ResolutionScope.TEST
)
public class ProcessTestCatalogMojo extends AbstractMojo {
	
	@Parameter(
		defaultValue = "${project.basedir}/src/test/resources/META-INF/catalog.xml"
	)
	private File catalogFile;
	
	@Parameter(
		defaultValue = "${project.build.directory}/generated-test-resources/process-catalog/"
	)
	private File generatedResourcesDirectory;
	
	@Parameter(
		defaultValue = "${project.build.directory}/generated-test-sources/process-catalog/"
	)
	private File generatedSourcesDirectory;
	
	@Parameter(
		defaultValue = "true"
	)
	private boolean addResources;
	
	@Parameter(
		defaultValue = "true"
	)
	private boolean addSources;
	
	@Parameter(
		defaultValue = "test-module"
	)
	private String moduleName;
	
	@Parameter(
		defaultValue = "0"
	)
	private String moduleVersion;
	
	@Parameter(
		defaultValue = ""
	)
	private String moduleTitle;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project}"
	)
	private MavenProject mavenProject;
	
	public void execute() throws MojoFailureException {
		try {
			XProcEngine engine = new CalabashWithPipelineModules(mavenProject.getCompileClasspathElements());
			Map<String,String> options = new HashMap<String,String>(); {
				options.put("generatedResourcesDirectory", asURI(generatedResourcesDirectory).toASCIIString());
				options.put("generatedSourcesDirectory", asURI(generatedSourcesDirectory).toASCIIString());
				options.put("moduleName", moduleName);
				options.put("moduleVersion", moduleVersion);
				options.put("moduleTitle", moduleTitle);
			}
			engine.run(asURI(this.getClass().getResource("/process-catalog/process-catalog.xpl")).toASCIIString(),
			           ImmutableMap.of("source", (List<String>)ImmutableList.of(asURI(catalogFile).toASCIIString())),
			           null,
			           options,
			           null);
			if (addResources) {
				Resource generatedResources = new Resource(); {
					generatedResources.setDirectory(generatedResourcesDirectory.getAbsolutePath());
					List<String> excludes = new ArrayList<String>(); {
						excludes.add("bnd.bnd");
					}
					generatedResources.setExcludes(excludes);
				}
				mavenProject.addTestResource(generatedResources);
			}
			if (addSources) {
				mavenProject.addTestCompileSourceRoot(generatedSourcesDirectory.getAbsolutePath());
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
}
