package org.daisy.common.xproc;

import javax.xml.namespace.QName;

// TODO: Auto-generated Javadoc
/**
 * The Class XProcOptionInfo contains the information of a given option.
 */
public final class XProcOptionInfo {

	/**
	 * This method creates a new option hiding the instantation process.
	 *
	 * @param name the name
	 * @param isRequired the is required
	 * @param select the select
	 * @return the x proc option info
	 */
	public static XProcOptionInfo newOption(QName name, boolean isRequired,
			String select) {
		return new XProcOptionInfo(name, isRequired, select);
	}

	/** The name. */
	private final QName name;

	/** ifthe option is required. */
	private final boolean isRequired;

	/** The select statement */
	private final String select;

	/**
	 * Instantiates a new x proc option info.
	 *
	 * @param name the name
	 * @param isRequired the is required
	 * @param select the select
	 */
	public XProcOptionInfo(QName name, boolean isRequired, String select) {
		this.name = name;
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
