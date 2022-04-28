package org.daisy.common.xproc;

import javax.xml.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Class XProcOptionInfo contains the information of a given option.
 */
public class XProcOptionInfo {

	/**
	 * This method creates a new option hiding the instantation process.
	 *
	 * @param name the name
	 * @param type the value type of the option
	 * @param isRequired the is required
	 * @param select the select
	 * @return the x proc option info
	 */
	public static XProcOptionInfo newOption(QName name, String type, boolean isRequired, String select) {
		return new XProcOptionInfo(name, type, isRequired, select);
	}

	public static XProcOptionInfo newOption(QName name, boolean isRequired, String select) {
		return new XProcOptionInfo(name, "xs:string", isRequired, select);
	}

	/** The name. */
	private final QName name;

	/** The type. */
	private final String type;

	/** ifthe option is required. */
	private final boolean isRequired;

	/** The select statement */
	private final String select;

	public XProcOptionInfo(QName name, String type, boolean isRequired, String select) {
		this.name = name;
		this.type = type;
		this.isRequired = isRequired;
		this.select = select;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public QName getName() {
		return name;
	}

	/**
	 * Gets the value type of the option
	 *
	 * This corresponds with the "as" attribute in XProc 3.0 (or the
	 * XMLCalabash specific "cx:as" attribute for XProc 1.0). The "xs"
	 * namespace prefix is bound to "http://www.w3.org/2001/XMLSchema".
	 */
	public String getType() {
		return type;
	}

	/**
	 * Checks if is required.
	 *
	 * @return true, if is required
	 */
	public boolean isRequired() {
		return isRequired;
	}

	/**
	 * Gets the select statement.
	 *
	 * @return the select
	 */
	public String getSelect() {
		return select;
	}

	@Override
	public String toString() {
		return String.format("XProcOptionInfo (%s) [select=%s,required=%s]",this.name,this.select,this.isRequired);
	}
}
