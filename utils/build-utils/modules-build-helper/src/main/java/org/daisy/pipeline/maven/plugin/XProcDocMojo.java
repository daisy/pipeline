package org.daisy.pipeline.maven.plugin;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.api.XProcExecutionException;
import org.daisy.maven.xproc.calabash.Calabash;

import static org.daisy.pipeline.maven.plugin.utils.URLs.asURI;

@Mojo(
	name = "xprocdoc"
)
public class XProcDocMojo extends AbstractMojo {
	
	@Parameter(
		defaultValue = "${project.basedir}/src/main/resources/META-INF/catalog.xml"
	)
	private File catalogXmlFile;
	
	@Parameter(
		defaultValue = ""
	)
	private String catalogXmlBaseURI;

	@Parameter(
		defaultValue = "${project.build.directory}/generated-resources/doc"
	)
	private File outputDirectory;
	
	public void execute() throws MojoFailureException {
		if (!catalogXmlFile.exists()) {
			getLog().info("File " + catalogXmlFile + " does not exist. Skipping xprocdoc goal.");
			return; }
		XProcEngine engine = new Calabash();
		outputDirectory.mkdirs();
		try {
			engine.run(asURI(XProcDocMojo.class.getResource("/xprocdoc/catalog-to-xprocdoc.xpl")).toASCIIString(),
			           ImmutableMap.of("source", Collections.singletonList(asURI(catalogXmlFile).toASCIIString())),
			           null,
			           ImmutableMap.of("catalog-base-uri", catalogXmlBaseURI == null | "".equals(catalogXmlBaseURI)
			                               ? ""
			                               : asURI(catalogXmlBaseURI).toASCIIString(),
			                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
			           null); }
		catch (XProcExecutionException e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage()); }
	}
}
