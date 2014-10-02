package org.daisy.cli;
/**
 * Provides exit codes to be used by implementing classes.
 */
public enum ExitCode {
	/**
	 * Normal application termination
	 */
	OK,
	/**
	 * Missing a required argument
	 */
	MISSING_ARGUMENT,
	/**
	 * Argument is unknown to the application
	 */
	UNKNOWN_ARGUMENT,
	/**
	 * 
	 */
	FAILED_TO_READ,
	/**
	 * 
	 */
	MISSING_RESOURCE,
	/**
	 * Argument value is illegal
	 */
	ILLEGAL_ARGUMENT_VALUE
};