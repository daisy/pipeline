package org.daisy.common.xproc;

// TODO: Auto-generated Javadoc
/**
 * The Class XProcPortInfo.
 */
public final class XProcPortInfo {

	/**
	 * The Enum Kind.
	 */
	public static enum Kind {

		/** The INPUT. */
		INPUT,
 /** The OUTPUT. */
 OUTPUT,
 /** The PARAMETER. */
 PARAMETER
	};

	/**
	 * New input port.
	 *
	 * @param name the name
	 * @param isSequence the is sequence
	 * @param isPrimary the is primary
	 * @return the x proc port info
	 */
	public static XProcPortInfo newInputPort(String name, boolean isSequence,
			boolean isPrimary) {
		return new XProcPortInfo(Kind.INPUT, name, isSequence, isPrimary);
	}

	/**
	 * New output port.
	 *
	 * @param name the name
	 * @param isSequence the is sequence
	 * @param isPrimary the is primary
	 * @return the x proc port info
	 */
	public static XProcPortInfo newOutputPort(String name, boolean isSequence,
			boolean isPrimary) {
		return new XProcPortInfo(Kind.OUTPUT, name, isSequence, isPrimary);
	}

	/**
	 * New parameter port.
	 *
	 * @param name the name
	 * @param isPrimary the is primary
	 * @return the x proc port info
	 */
	public static XProcPortInfo newParameterPort(String name, boolean isPrimary) {
		return new XProcPortInfo(Kind.PARAMETER, name, true, isPrimary);
	}

	/** The kind. */
	private final Kind kind;

	/** The name. */
	private final String name;

	/** The is primary. */
	private final boolean isPrimary;

	/** The is sequence. */
	private final boolean isSequence;

	/**
	 * Instantiates a new x proc port info.
	 *
	 * @param kind the kind
	 * @param name the name
	 * @param isSequence the is sequence
	 * @param isPrimary the is primary
	 */
	private XProcPortInfo(Kind kind, String name, boolean isSequence,
			boolean isPrimary) {
		this.kind = kind;
		this.name = name;
		this.isSequence = isSequence;
		this.isPrimary = isPrimary;
	}

	/**
	 * Gets the kind.
	 *
	 * @return the kind
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Checks if is primary.
	 *
	 * @return true, if is primary
	 */
	public boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * Checks if is sequence.
	 *
	 * @return true, if is sequence
	 */
	public boolean isSequence() {
		return isSequence;
	}

}
