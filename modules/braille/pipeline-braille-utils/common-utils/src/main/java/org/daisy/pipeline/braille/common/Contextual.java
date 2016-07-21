package org.daisy.pipeline.braille.common;

public interface Contextual<C,T> {
	
	public T withContext(C context);
	
}
