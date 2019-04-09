package org.daisy.maven.xproc.plugin;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

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
	
	private static final String XPROCSPEC_LOGGER_NAME = "org.daisy.xprocspec";
	
	public void execute() throws MojoFailureException {
		
		if (skip || skipTests) {
			getLog().info("Tests are skipped.");
			return; }
		
		try {
		
		// configure logging
		File logbackXml = new File(new File(project.getBuild().getTestOutputDirectory()), "logback.xml");
		org.slf4j.Logger rootLogger = LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		if (!(rootLogger instanceof ch.qos.logback.classic.Logger)) {
			System.out.println("WARNING: There is another SLF4J implementation on your class path that is chosen over logback.");
			try {
				System.out.println(" -> " + rootLogger.getClass().getProtectionDomain().getCodeSource().getLocation()); }
			catch (SecurityException se) {}
			if (logbackXml.exists())
				System.out.println("" + logbackXml + " ignored");
			if (rootLogger.getClass().getName().equals("org.slf4j.impl.SimpleLogger")) {
				
				// default log settings with slf4j-simple
				if (System.getProperty("org.slf4j.simpleLogger.log." + XPROCSPEC_LOGGER_NAME) == null)
					System.setProperty("org.slf4j.simpleLogger.log." + XPROCSPEC_LOGGER_NAME, "warn");
				System.out.println("See http://www.slf4j.org/api/org/slf4j/impl/SimpleLogger.html for how to configure logging."); }}
		else if (logbackXml.exists())
			System.setProperty("logback.configurationFile", logbackXml.toURI().toASCIIString());
		else
			
			// default log settings with logback
			((ch.qos.logback.classic.Logger)LoggerFactory.getLogger(XPROCSPEC_LOGGER_NAME)).setLevel(ch.qos.logback.classic.Level.WARN);
		java.util.logging.LogManager.getLogManager().reset();
		SLF4JBridgeHandler.install();
		java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.FINEST);
		
		ClassLoader restoreClassLoader = Thread.currentThread().getContextClassLoader();
		
		try {
		
		URLClassLoader classLoader; {
			List<URL> classPathURLs = new ArrayList<URL>(); {
				for (String path : project.getTestClasspathElements())
					classPathURLs.add(new File(path).toURI().toURL());
				for (Artifact artifact : project.getArtifacts())
					classPathURLs.add(artifact.getFile().toURI().toURL());
			}
			classLoader = new URLClassLoader(classPathURLs.toArray(new URL[classPathURLs.size()]),
			                                 Thread.currentThread().getContextClassLoader());
		}
		Thread.currentThread().setContextClassLoader(classLoader);
		XProcSpecRunner runner; {
			try {
				runner = ServiceLoader.load(XProcSpecRunner.class).iterator().next();
			} catch (NoSuchElementException e) {
				throw new RuntimeException("No XProcSpecRunner found", e);
			}
		}
		File calabashXml = new File(new File(project.getBuild().getTestOutputDirectory()), "calabash.xml");
		if (!calabashXml.exists()) calabashXml = null;
		Reporter.DefaultReporter reporter = new Reporter.DefaultReporter(System.out);
		
		if (!runner.run(xprocspecDirectory,
		                reportsDirectory,
		                surefireReportsDirectory,
		                tempDir,
		                calabashXml,
		                reporter))
			throw new MojoFailureException("There are test failures.\n"
			                               + "Please refer to " + reportsDirectory.getPath()
			                               + " for the individual test results.");
		
		} finally {
			Thread.currentThread().setContextClassLoader(restoreClassLoader);
		}
		
		} catch (MojoFailureException e) {
			throw e;
		} catch (Throwable e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage(), e);
		}
	}
}
