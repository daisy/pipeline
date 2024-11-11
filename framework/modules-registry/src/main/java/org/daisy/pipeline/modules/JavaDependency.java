package org.daisy.pipeline.modules;

/**
 * Java dependency of an XSLT resource. The Java resource (class) is assumed to live in the same
 * module as the XSLT resource.
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
}
