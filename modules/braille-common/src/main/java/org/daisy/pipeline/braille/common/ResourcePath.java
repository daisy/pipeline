package org.daisy.pipeline.braille.common;

import java.net.URI;

public interface ResourcePath extends ResourceResolver {
	
	public URI getIdentifier();
	
	public URI canonicalize(URI resource);
	
}
