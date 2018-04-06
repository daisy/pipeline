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

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.api.XProcExecutionException;
import org.daisy.maven.xproc.calabash.Calabash;

import static org.daisy.pipeline.maven.plugin.utils.URIs.asURI;

/**
 * @goal xprocdoc
 */
public class XProcDocMojo extends AbstractMojo {
	
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
	 * @parameter expression="${project.build.directory}/generated-resources/doc"
	 * @required
	 */
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
			           ImmutableMap.of("input-base-uri", asURI(sourceDirectory).toASCIIString(),
			                           "output-base-uri", asURI(outputDirectory).toASCIIString()),
			           null); }
		catch (XProcExecutionException e) {
			throw new MojoFailureException(e.getMessage()); }
	}
}
