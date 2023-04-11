package org.daisy.common.xproc;

/**
 * Info about an XProc port
 */
public final class XProcPortInfo {

	public static enum Kind {
		INPUT,
		OUTPUT,
		PARAMETER
	};

	/**
	 * Instantiates a new {@link XProcPortInfo} for an input port.
	 */
	public static XProcPortInfo newInputPort(String name, boolean isSequence, boolean isRequired, boolean isPrimary) {
		return new XProcPortInfo(Kind.INPUT, name, isSequence, isRequired, isPrimary);
	}

	/**
	 * Instantiates a new {@link XProcPortInfo} for an output port.
	 */
	public static XProcPortInfo newOutputPort(String name, boolean isSequence, boolean isPrimary) {
		return new XProcPortInfo(Kind.OUTPUT, name, isSequence, false, isPrimary);
	}

	/**
	 * Instantiates a new {@link XProcPortInfo} for a parameter input port.
	 */
	public static XProcPortInfo newParameterPort(String name, boolean isPrimary) {
		return new XProcPortInfo(Kind.PARAMETER, name, true, true, isPrimary);
	}

	private final Kind kind;
	private final String name;
	private final boolean isPrimary;
	private final boolean isSequence;
	private final boolean isRequired;

	private XProcPortInfo(Kind kind, String name, boolean isSequence, boolean isRequired, boolean isPrimary) {
		this.kind = kind;
		this.name = name;
		this.isSequence = isSequence;
		this.isRequired = isRequired;
		this.isPrimary = isPrimary;
	}

	/**
	 * The kind of port.
	 */
	public Kind getKind() {
		return kind;
	}

	/**
	 * The port name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Whether it is a primary port.
	 */
	public boolean isPrimary() {
		return isPrimary;
	}

	/**
	 * Whether the port can accept/produce a sequence of documents.
	 *
	 * A sequence of documents is always allowed on a parameter input port.
	 */
	public boolean isSequence() {
		return isSequence;
	}

	/**
	 * Whether a connection is required on the port.
	 *
	 * A connection is required if the port does not have a default connection. Output ports never
	 * require a connection. Paramter input ports always required a connection.
	 */
	public boolean isRequired() {
		return isRequired;
	}
}
