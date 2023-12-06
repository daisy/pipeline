package org.daisy.common.xproc;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

/**
 * Contains the information of a given option.
 */
public class XProcOptionInfo {

	public static XProcOptionInfo newOption(QName name, NamespaceContext nsContext, String type, boolean isRequired, String select) {
		return new XProcOptionInfo(name, nsContext, type, isRequired, select);
	}

	public static XProcOptionInfo newOption(QName name, String type, boolean isRequired, String select) {
		return new XProcOptionInfo(name, null, type, isRequired, select);
	}

	public static XProcOptionInfo newOption(QName name, boolean isRequired, String select) {
		return new XProcOptionInfo(name, null, null, isRequired, select);
	}

	private final QName name;
	private final NamespaceContext nsContext;
	private final String type;
	private final boolean isRequired;
	private final String select;

	protected XProcOptionInfo(QName name, String type, boolean isRequired, String select) {
		this(name, null, type, isRequired, select);
	}

	protected XProcOptionInfo(QName name, NamespaceContext nsContext, String type, boolean isRequired, String select) {
		if (isRequired && select != null)
			throw new IllegalArgumentException("A required option can not have a select statement");
		this.name = name;
		this.nsContext = nsContext;
		this.type = type;
		this.isRequired = isRequired;
		this.select = select;
	}

	/**
	 * Gets the name.
	 */
	public QName getName() {
		return name;
	}

	/**
	 * Namespace context for the type and select statements.
	 */
	public NamespaceContext getNamespaceContext() {
		return nsContext;
	}

	/**
	 * Gets the value type of the option
	 *
	 * This corresponds with the "as" attribute in XProc 3.0 (or the XMLCalabash specific "cx:as"
	 * attribute for XProc 1.0).
	 */
	public String getType() {
		return type;
	}

	/**
	 * Whether the option is required.
	 */
	public boolean isRequired() {
		return isRequired;
	}

	/**
	 * Gets the select statement.
	 */
	public String getSelect() {
		return select;
	}

	@Override
	public String toString() {
		return String.format("XProcOptionInfo (%s) [select=%s,required=%s]",this.name,this.select,this.isRequired);
	}
}
