package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.ArtifactHandler;
import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.repository.RepositorySystem;

import org.codehaus.plexus.compiler.Compiler;
import org.codehaus.plexus.compiler.CompilerConfiguration;
import org.codehaus.plexus.compiler.CompilerMessage;
import org.codehaus.plexus.compiler.CompilerResult;
import org.codehaus.plexus.compiler.manager.CompilerManager;
import org.codehaus.plexus.util.DirectoryScanner;

@Mojo(
	name = "generate-spi-classes",
	defaultPhase = LifecyclePhase.GENERATE_SOURCES,
	requiresDependencyResolution = ResolutionScope.COMPILE
)
public class GenerateSpiClassesMojo extends AbstractMojo {
	
	@Parameter(
		defaultValue = "${project.build.directory}/generated-resources/spi/"
	)
	private File generatedResourcesDirectory;
	
	@Parameter(
		defaultValue = "${project.build.directory}/generated-sources/spi/"
	)
	private File generatedSourcesDirectory;
	
	@Parameter(
		defaultValue = "*"
	)
	private String includes;
	
	@Parameter(
		defaultValue = "true"
	)
	private boolean addResources;
	
	@Parameter(
		defaultValue = "true"
	)
	private boolean addSources;
	
	@Parameter(
		defaultValue = "${project.build.outputDirectory}",
		readonly = true,
		required = true
	)
	private File classesDirectory;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project.build.sourceEncoding}"
	)
	private String encoding;
	
	@Parameter(
		readonly = true,
		defaultValue = "${project}"
	)
	private MavenProject mavenProject;
	
	@Parameter(
		defaultValue = "${session}",
		readonly = true,
		required = true
	)
	private MavenSession mavenSession;
	
	@Component
	private CompilerManager compilerManager;
	
	@Component
	private ArtifactHandlerManager artifactHandlerManager;
	
	@Component
	private ResolutionErrorHandler resolutionErrorHandler;
	
	@Component
	private RepositorySystem repositorySystem;
	
	public void execute() throws MojoFailureException {
		try {
			Set<String> compileSourceRoots = new HashSet<String>(); {
				for (Object dir : mavenProject.getCompileSourceRoots())
					if (new File((String)dir).exists())
						compileSourceRoots.add((String)dir);
			}
			if (compileSourceRoots.isEmpty()) {
				getLog().info("No sources to compile");
				return;
			}
			List<String> processorPathEntries = new ArrayList<String>(); {
				Set<Artifact> artifacts = new HashSet<Artifact>();
				ArtifactHandler handler = artifactHandlerManager.getArtifactHandler("jar");
				Properties p = new Properties();
				p.load(GenerateSpiClassesMojo.class.getResourceAsStream(
					       "/org/daisy/pipeline/maven/plugin/dependencies.properties"));
				Artifact artifact = new DefaultArtifact(
						"org.daisy.pipeline.build",
						"ds-to-spi-annotations-processor",
						VersionRange.createFromVersion(p.getProperty("ds-to-spi-annotations-processor.version")),
						Artifact.SCOPE_RUNTIME,
						"jar",
						"",
						handler,
						false);
				artifacts.add(artifact);
				ArtifactResolutionRequest request = new ArtifactResolutionRequest()
					.setArtifact(artifact)
					.setResolveRoot(true)
					.setResolveTransitively(true)
					.setArtifactDependencies(artifacts)
					.setLocalRepository(mavenSession.getLocalRepository())
					.setRemoteRepositories(mavenProject.getRemoteArtifactRepositories());
				ArtifactResolutionResult resolutionResult = repositorySystem.resolve(request);
				resolutionErrorHandler.throwErrors(request, resolutionResult);
				for (Object resolved : resolutionResult.getArtifacts())
					processorPathEntries.add(((Artifact)resolved).getFile().getAbsolutePath());
			}
			Map<String,String> compilerArguments = new HashMap<String,String>(); {
				compilerArguments.put("-AdsToSpi.generatedResourcesDirectory=" + generatedResourcesDirectory.getAbsolutePath(), null);
				compilerArguments.put("-AdsToSpi.generatedSourcesDirectory=" + generatedSourcesDirectory.getAbsolutePath(), null);
				compilerArguments.put("-AdsToSpi.includes=" + includes, null);
			}
			Set<File> sources = new HashSet<File>(); {
				for (String sourceDir : mavenProject.getCompileSourceRoots()) {
					if (new File(sourceDir).exists()) {
						DirectoryScanner scanner = new DirectoryScanner();
						scanner.setBasedir(sourceDir);
						scanner.setIncludes(new String[]{"**/*.java"});
						scanner.scan();
						for (String path : scanner.getIncludedFiles())
							sources.add(new File(sourceDir, path));
					}
				}
			}
			CompilerConfiguration compilerConfiguration = new CompilerConfiguration(); {
				compilerConfiguration.setOutputLocation(classesDirectory.getAbsolutePath());
				compilerConfiguration.setClasspathEntries(mavenProject.getCompileClasspathElements());
				compilerConfiguration.setOptimize(false);
				compilerConfiguration.setDebug(true);
				compilerConfiguration.setVerbose(false);
				compilerConfiguration.setShowWarnings(false);
				compilerConfiguration.setShowDeprecation(false);
				compilerConfiguration.setSourceVersion("1.8");
				compilerConfiguration.setTargetVersion("1.8");
				compilerConfiguration.setProc("only");
				compilerConfiguration.setGeneratedSourcesDirectory(null);
				compilerConfiguration.setSourceLocations(new ArrayList<String>(compileSourceRoots));
				compilerConfiguration.setAnnotationProcessors(null);
				compilerConfiguration.setProcessorPathEntries(processorPathEntries);
				compilerConfiguration.setSourceEncoding(encoding);
				compilerConfiguration.setCustomCompilerArgumentsAsMap(compilerArguments);
				compilerConfiguration.setFork(false);
				compilerConfiguration.setCompilerReuseStrategy(CompilerConfiguration.CompilerReuseStrategy.ReuseCreated);
				compilerConfiguration.setForceJavacCompilerUse(false);
				compilerConfiguration.setSourceFiles(sources);
			}
			Compiler compiler = compilerManager.getCompiler("javac");
			CompilerResult compilerResult = compiler.performCompile(compilerConfiguration);
			if (!compilerResult.isSuccess()) {
				for (CompilerMessage message : compilerResult.getCompilerMessages()) {
					if (message.getKind() == CompilerMessage.Kind.ERROR) {
						getLog().error(message.toString());
					} else if (message.getKind() == CompilerMessage.Kind.WARNING
					           || message.getKind() == CompilerMessage.Kind.MANDATORY_WARNING) {
						getLog().warn(message.toString());
					} else {
						getLog().info(message.toString());
					}
				}
				throw new MojoFailureException("Compilation failed");
			}
			if (addResources) {
				Resource generatedResources = new Resource(); {
					generatedResources.setDirectory(generatedResourcesDirectory.getAbsolutePath());
					List<String> includes = new ArrayList<String>(); {
						includes.add("META-INF/services/*");
					}
					generatedResources.setIncludes(includes);
				}
				mavenProject.addResource(generatedResources);
			}
			if (addSources) {
				mavenProject.addCompileSourceRoot(generatedSourcesDirectory.getAbsolutePath());
			}
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
}
