package org.daisy.maven.xproc.plugin;

import java.io.File;

import java.util.ServiceLoader;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

import org.codehaus.plexus.classworlds.realm.ClassRealm;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.xprocspec.XProcSpecRunner;
import org.daisy.maven.xproc.xprocspec.XProcSpecRunner.Reporter;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.slf4j.LoggerFactory;
		
/**
 * Run an XProcSpec test.
 *
 * @goal xprocspec
 * @requiresDependencyResolution test
 */
public class XProcSpecMojo extends AbstractMojo {
	
	/**
	 * Directory containing the XProcSpec tests.
	 *
	 * @parameter expression="${project.basedir}/src/test/xprocspec"
	 * @required
	 */
	private File xprocspecDirectory;
	
	/**
	 * Directory that will contain the generated reports.
	 *
	 * @parameter expression="${project.build.directory}/xprocspec-reports"
	 * @required
	 */
	private File reportsDirectory;
	
	/**
	 * Directory that will contain the generated Surefire reports.
	 *
	 * @parameter expression="${project.build.directory}/surefire-reports"
	 * @required
	 */
	private File surefireReportsDirectory;
	
	/**
	 * Set this to "true" to skip running tests, but still compile them. Its use
	 * is NOT RECOMMENDED, but quite convenient on occasion.
	 *
	 * @parameter property="skipTests" default-value="false"
	 */
	private boolean skipTests;

	/**
	 * Set this to "true" to bypass unit tests entirely. Its use is NOT
	 * RECOMMENDED, especially if you enable it using the "maven.test.skip"
	 * property, because maven.test.skip disables both running the tests and
	 * compiling the tests. Consider using the <code>skipTests</code> parameter
	 * instead.
	 *
	 * @parameter expression="${maven.test.skip}" default-value="false"
	 */
	private boolean skip;
	
	/**
	 * Temporary directory for storing XProcSpec related files.
	 *
	 * @parameter default-value="${project.build.directory}/xprocspec"
	 * @required
	 * @readonly
	 */
	private File tempDir;
	
	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;
	
	public void execute() throws MojoFailureException {
		
		if (skip || skipTests) {
			getLog().info("Tests are skipped.");
			return; }
		
		File logbackXml = new File(new File(project.getBuild().getTestOutputDirectory()), "logback.xml");
		if (logbackXml.exists())
			System.setProperty("logback.configurationFile", logbackXml.toURI().toASCIIString());
		else {
			try {
				ch.qos.logback.classic.Logger root= (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(
					ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
				root.setLevel(ch.qos.logback.classic.Level.WARN); }
			catch (ClassCastException e) {}}
		java.util.logging.LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
		
		XProcSpecRunner runner = new XProcSpecRunner();
		
		File calabashXml = new File(new File(project.getBuild().getTestOutputDirectory()), "calabash.xml");
		if (calabashXml.exists()) {
			ClassRealm realm = (ClassRealm)Thread.currentThread().getContextClassLoader();
			try {
				for (String path : project.getTestClasspathElements())
					realm.addURL(new File(path).toURI().toURL());
				for (Artifact artifact : project.getArtifacts())
					realm.addURL(artifact.getFile().toURI().toURL());
				XProcEngine engine = ServiceLoader.load(XProcEngine.class).iterator().next();
				engine.getClass().getMethod("setConfiguration", File.class).invoke(engine, calabashXml);
				runner.setXProcEngine(engine); }
			catch (Throwable e) {
				e.printStackTrace();
				throw new RuntimeException(e); }}
		
		Reporter.DefaultReporter reporter = new Reporter.DefaultReporter(System.out);
		
		if (!runner.run(xprocspecDirectory,
		                reportsDirectory,
		                surefireReportsDirectory,
		                tempDir,
		                reporter))
			throw new MojoFailureException("There are test failures.");
		
	}
}
