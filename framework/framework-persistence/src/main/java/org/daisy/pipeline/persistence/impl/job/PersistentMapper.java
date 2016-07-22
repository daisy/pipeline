package org.daisy.pipeline.persistence.impl.job;

import java.net.URI;

import javax.persistence.Embeddable;

import org.daisy.pipeline.job.URIMapper;

@Embeddable
public class PersistentMapper {

	String inputBase;
	String outputBase;

	/**
	 * Constructs a new instance.
	 */
	public PersistentMapper() {
	}	

	public PersistentMapper(URIMapper mapper){
		this.inputBase=mapper.getInputBase().toString();	
		this.outputBase=mapper.getOutputBase().toString();	
	}

	public URIMapper getMapper(){
		return new URIMapper(URI.create(this.inputBase),URI.create(this.outputBase));
	}

	public void setMapper(URIMapper mapper){
		this.inputBase=mapper.getInputBase().toString();	
		this.outputBase=mapper.getOutputBase().toString();	
	}


}
