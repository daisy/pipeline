package org.daisy.pipeline.modules;

/**
 * Java dependency of an XSLT resource.
 */
public class JavaDependency implements Dependency {

	private final Module module;
	private final String className;

	/**
	 * @param className The fully qualified name of the class.
	 */
	public JavaDependency(Module module, String className) {
		this.module = module;
		this.className = className;
	}

	public String getClassName() {
		return className;
	}

	@Override
	public String toString() {
		return className;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null)
			return false;
		if (!(o instanceof JavaDependency))
			return false;
		JavaDependency that = (JavaDependency)o;
		//if (!module.equals(that.module))
		//	return false;
		if (!className.equals(that.className))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		//result = prime * result + module.hashCode();
		result = prime * result + className.hashCode();
		return result;
	}
}
