package org.daisy.braille.api.validator;

import org.daisy.factory.FactoryProperties;
import org.daisy.factory.Provider;

/**
 * <p>
 * Provides an interface for a Validator service. The purpose of this
 * interface is to expose an implementation of Validator as a
 * service.
 * </p>
 * 
 * <p>
 * To comply with this interface, an implementation must be thread safe and
 * address both the possibility that only a single instance is created and used
 * throughout and that new instances are created as desired.
 * </p>
 * 
 * @author Joel Håkansson
 */
public interface ValidatorProvider extends Provider<FactoryProperties> {
	
	public Validator newValidator(String identifier);

}
