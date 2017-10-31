package org.daisy.dotify.tasks.impl.validity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.daisy.streamline.api.validity.Validator;
import org.daisy.streamline.api.validity.ValidatorFactory;
import org.daisy.streamline.api.validity.ValidatorFactoryException;

import aQute.bnd.annotation.component.Component;

/**
 * @author Joel Håkansson
 *
 */
@Component
public class BrailleValidatorFactory implements ValidatorFactory {
	Map<String, Class<? extends Validator>> validators;
	
	/**
	 * Creates a new braille validator factory.
	 */
	public BrailleValidatorFactory() {
		validators = new HashMap<>();
		validators.put("application/x-obfl+xml", OBFLValidator.class);
		validators.put("application/x-pef+xml", PEFValidator.class);
	}

	@Override
	public Collection<String> listIdentifiers() {
		return validators.keySet();
	}

	@Override
	public Validator newValidator(String identifier) throws ValidatorFactoryException {
		Class<? extends Validator> c = validators.get(identifier);
		if (c==null) {
			throw new ValidatorFactoryException("Factory for identifier not found: " + identifier);
		}
		try {
			return c.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ValidatorFactoryException("Cannot instantiate class.", e);
		}
	}

}
