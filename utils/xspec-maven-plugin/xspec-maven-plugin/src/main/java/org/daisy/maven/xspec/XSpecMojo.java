/**
 * Copyright (C) 2013 The DAISY Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.daisy.maven.xspec;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.DirectoryScanner;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

/**
 * Run XSpec tests and produce various reports.
 * 
 */
@Mojo(
	name = "test",
	defaultPhase = LifecyclePhase.TEST,
	requiresDependencyResolution = ResolutionScope.TEST

)
public class XSpecMojo extends AbstractMojo {

	private static final String pluginName = "XSpec";
	private static final String[] defaultIncludes = new String[] { "**/*.xspec" };
	private static final Predicate<String> notNullOrEmpty = new Predicate<String>() {
		public boolean apply(String string) {
			return !Strings.isNullOrEmpty(string);
		}
	};

	/**
	 * The base directory containing test sources. The default is
	 * <code>${basedir}/src/test/xspec</code>
	 */
	@Parameter(defaultValue = "${basedir}/src/test/xspec")
	private File testSourceDirectory;

	/**
	 * Directory where all Surefire reports are written to.
	 */
	@Parameter(defaultValue = "${project.build.directory}/surefire-reports")
	private File reportsDirectory;

	/**
	 * Set this to "true" to skip running tests, but still compile them. Its use
	 * is NOT RECOMMENDED, but quite convenient on occasion.
	 */
	@Parameter(property = "skipTests", defaultValue = "false")
	private boolean skipTests;

	/**
	 * Set this to "true" to bypass unit tests entirely. Its use is NOT
	 * RECOMMENDED, especially if you enable it using the "maven.test.skip"
	 * property, because maven.test.skip disables both running the tests and
	 * compiling the tests. Consider using the <code>skipTests</code> parameter
	 * instead.
	 */
	@Parameter(property = "maven.test.skip", defaultValue = "false")
	private boolean skip;

	/**
	 * Specify this parameter to run individual tests by file name, overriding
	 * the <code>includes/excludes</code> parameters. Each pattern you specify
	 * here will be used to create an include pattern formatted like
	 * <code>**&#47;${test}.xspec</code>, so you can just type "-Dxspec=MyTest"
	 * to run a single test called "xspec/MyTest.xspec". The test patterns
	 * prefixed with a <code>!</code> will be excluded.<br/>
	 * This parameter overrides the <code>includes/excludes</code> parameters
	 * <p/>
	 */
	@Parameter(property = "xspec")
	private String test;

	/**
	 * A list of &lt;include> elements specifying the tests (by pattern) that
	 * should be included in testing. When not specified and when the
	 * <code>test</code> parameter is not specified, the default includes will
	 * be <code><br/>
	 * &lt;includes><br/>
	 * &nbsp;&lt;include>**&#47;*.xspec&lt;/include><br/>
	 * &lt;/includes><br/>
	 * </code>
	 * <p>
	 * Each include item may also contain a comma-separated sublist of items,
	 * which will be treated as multiple &nbsp;&lt;include> entries.
	 * </p>
	 */
	@Parameter
	private List<String> includes;

	/**
	 * A list of &lt;exclude> elements specifying the tests (by pattern) that
	 * should be excluded in testing. When not specified and when the
	 * <code>test</code> parameter is not specified, the default excludes will
	 * be empty.
	 * <p>
	 * Each exclude item may also contain a comma-separated sublist of items,
	 * which will be treated as multiple &nbsp;&lt;exclude> entries.
	 * </p>
	 */
	@Parameter
	private List<String> excludes;

	@Parameter(readonly = true, defaultValue = "${project}")
	private MavenProject project;

	private XSpecRunner xspecRunner = null;

	public void execute() throws MojoExecutionException, MojoFailureException {

		if (verifyParameters()) {

			getLog().info(pluginName + " report directory: " + reportsDirectory);

			logHeader();
			try {
				if (xspecRunner == null)
					xspecRunner = newXSpecRunner();
				TestResults testResults = xspecRunner.run(scanTests(),
						reportsDirectory);
				System.out.println(testResults.toDetailedString());
				if ((testResults.getFailures() > 0 || testResults.getErrors() > 0)) {
					StringBuilder sb = new StringBuilder();
					sb.append("There are test failures.").append(
							TestResults.NEWLINE);
					sb.append("Please refer to ")
							.append(reportsDirectory.getPath())
							.append(" for the individual test results.");
					throw new MojoFailureException(sb.toString());
				}
			} catch (MojoFailureException me) {
				throw me;
			} catch (Exception e) {
				e.printStackTrace();
				getLog().error(e.getMessage());
				throw new MojoExecutionException(e.getMessage());
			}
		}

	}

	private boolean isSkipExecution() {
		return skip || skipTests;
	}

	/** Find XSpecRunner service or create one. */
	private XSpecRunner newXSpecRunner() {
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
			XSpecRunner runner; {
				try {
					runner = ServiceLoader.load(XSpecRunner.class).iterator().next();
				} catch (NoSuchElementException e) {
					runner = new XSpecRunner();
					runner.init();
				}
			}
			return runner;
		} catch (DependencyResolutionRequiredException e) {
			throw new RuntimeException(e);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		} finally {
			Thread.currentThread().setContextClassLoader(restoreClassLoader);
		}
	}

	private void logHeader() {
		System.out.println();
		System.out
				.println("-------------------------------------------------------");
		System.out.println(" T E S T S  ::  X S L T ");
		System.out
				.println("-------------------------------------------------------");
		System.out.println();
	}

	private boolean verifyParameters() throws MojoFailureException,
			MojoExecutionException {
		if (isSkipExecution()) {
			getLog().info("Tests are skipped.");
			return false;
		}
		Preconditions.checkNotNull(reportsDirectory);
		if (!reportsDirectory.exists() || !reportsDirectory.isDirectory()) {
			if (!reportsDirectory.mkdirs()) {
				getLog().error(
						"Can't create reports directory: "
								+ reportsDirectory.getPath());
				return false;
			}
		}
		Preconditions.checkNotNull(testSourceDirectory);
		if (!testSourceDirectory.exists() || !testSourceDirectory.isDirectory()) {
			getLog().warn(
					"Test source directory does not exist: "
							+ testSourceDirectory);
			return false;
		}
		return true;
	}

	private List<String> getIncludeList() {
		if (!getSingleTest().isEmpty()) {
			return getSingleTest();
		} else if (includes == null || includes.isEmpty()) {
			return Arrays.asList(defaultIncludes);
		} else {
			return Lists.newArrayList(Collections2.filter(includes,
					notNullOrEmpty));
		}
	}

	private List<String> getExcludeList() {
		if (excludes == null || excludes.isEmpty()
				|| !getSingleTest().isEmpty()) {
			return Collections.emptyList();
		} else {
			return Lists.newArrayList(Collections2.filter(excludes,
					notNullOrEmpty));
		}
	}

	private List<String> getSingleTest() {
		if (Strings.isNullOrEmpty(test)) {

			return Collections.emptyList();
		} else {
			return ImmutableList.of("**/"
					+ (test.endsWith(".xspec") ? test : test + ".xspec"));
		}
	}

	private Map<String, File> scanTests() {
		DirectoryScanner scanner = new DirectoryScanner();
		scanner.setBasedir(testSourceDirectory);
		scanner.setCaseSensitive(true);
		scanner.setExcludes(getExcludeList().toArray(new String[] {}));
		scanner.setIncludes(getIncludeList().toArray(new String[] {}));
		scanner.addDefaultExcludes();
		scanner.scan();
		String[] testPaths = scanner.getIncludedFiles();
		Map<String, File> tests = new HashMap<String, File>();
		for (int i = 0; i < testPaths.length; i++) {
			String testPath = testPaths[i];
			String testName = testPath.replaceFirst(
					"\\." + Files.getFileExtension(testPath) + "$", "")
					.replace(File.separatorChar, '.');
			tests.put(testName, new File(testSourceDirectory, testPath));
		}
		return Collections.unmodifiableMap(tests);
	}

	protected void setTestSourceDirectory(File testSourceDirectory) {
		this.testSourceDirectory = testSourceDirectory;
	}

	protected void setReportsDirectory(File reportsDirectory) {
		this.reportsDirectory = reportsDirectory;
	}

	protected void setSkipTests(boolean skipTests) {
		this.skipTests = skipTests;
	}

	protected void setSkip(boolean skip) {
		this.skip = skip;
	}

	protected void setTest(String test) {
		this.test = test;
	}

	protected void setIncludes(List<String> includes) {
		this.includes = includes;
	}

	protected void setExcludes(List<String> excludes) {
		this.excludes = excludes;
	}

	void setXSpecRunner(XSpecRunner xspecRunner) {
		this.xspecRunner = xspecRunner;
	}
}
