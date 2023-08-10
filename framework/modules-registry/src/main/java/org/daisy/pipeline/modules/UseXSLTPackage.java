package org.daisy.pipeline.modules;

/**
 * A {@code xsl:use-package} dependency.
 */
public class UseXSLTPackage implements Dependency {

	private final String name;
	private final String version;

	public UseXSLTPackage(String name) {
		this(name, "*");
	}

	public UseXSLTPackage(String name, String version) {
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}
}
