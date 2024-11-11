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

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof UseXSLTPackage))
			return false;
		UseXSLTPackage that = (UseXSLTPackage)o;
		if (!name.equals(that.name))
			return false;
		if (!version.equals(that.version))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + name.hashCode();
		result = prime * result + version.hashCode();
		return result;
	}
}
