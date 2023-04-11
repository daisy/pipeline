package org.daisy.pipeline.script;

import org.daisy.pipeline.datatypes.DatatypeService;

/**
 * Script option description.
 */
public interface ScriptOption {

	/**
	 * The name.
	 */
	public String getName();

	/**
	 * Whether the option is required.
	 */
	public boolean isRequired();

	/**
	 * The default value.
	 */
	public String getDefault();

	/**
	 * The nice name.
	 */
	public String getNiceName();

	/**
	 * The description.
	 */
	public String getDescription();

	/**
	 * The type.
	 */
	public DatatypeService getType();

	/**
	 * The media type.
	 */
	public String getMediaType();

	/**
	 * Whether the option is a primary output.
	 */
	public boolean isPrimary();

	/**
	 * Whether this option takes a sequence of values.
	 */
	public boolean isSequence();

	/**
	 * Whether the order in a sequence matters.
	 */
	public boolean isOrdered();

}
