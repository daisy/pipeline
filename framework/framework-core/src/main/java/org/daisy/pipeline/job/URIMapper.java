package org.daisy.pipeline.job;

import java.net.URI;

public final class URIMapper{
	private final URI inputBase;
	private final URI outputBase;

	public URIMapper(URI inputBase,URI outputBase){
		this.inputBase=inputBase;
		this.outputBase=outputBase;
	}
	public URI mapInput(URI relative){
		return inputBase.resolve(relative);
	}
	public URI mapOutput(URI relative){
		return outputBase.resolve(relative);
	}
	public URI unmapInput(URI absolute){
		return inputBase.relativize(absolute);
	}
	public URI unmapOutput(URI absolute){
		return outputBase.relativize(absolute);
	}

	/**
	 * Gets the inputBase for this instance.
	 *
	 * @return The inputBase.
	 */
	public URI getInputBase() {
		return this.inputBase;
	}

	/**
	 * Gets the outputBase for this instance.
	 *
	 * @return The outputBase.
	 */
	public URI getOutputBase() {
		return this.outputBase;
	}

	@Override
	public int hashCode() {
		return this.inputBase.hashCode()+this.outputBase.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj==null || ! (obj instanceof URIMapper))
			return false;
		URIMapper other=(URIMapper)obj;
		return this.inputBase.equals(other.inputBase)&&this.outputBase.equals(other.outputBase);

	}

}
