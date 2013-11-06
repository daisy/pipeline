package org.daisy.dotify.api.translator;

/**
 * <p>
 * Provides an interface for a TextBorderFactory service. The purpose of this
 * interface is to expose an implementation of a TextBorderFactory as a service.
 * </p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 * 
 */
public interface TextBorderFactoryService {

	public TextBorderFactory newFactory();

}
