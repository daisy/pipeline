package org.daisy.dotify.tasks.impl.validity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.daisy.streamline.api.media.FileDetails;
import org.daisy.streamline.api.validity.Validator;
import org.daisy.streamline.api.validity.ValidatorFactory;
import org.daisy.streamline.api.validity.ValidatorFactoryException;
import org.osgi.service.component.annotations.Component;

/**
 * @author Joel Håkansson
 *
 */
@Component
public class ValidatorFactoryImpl implements ValidatorFactory {
	private static final String MIME_OBFL = "application/x-obfl+xml";
	private static final String MIME_PEF = "application/x-pef+xml";
	Map<String, Class<? extends Validator>> validators;
	
	/**
	 * Creates a new braille validator factory.
	 */
	public ValidatorFactoryImpl() {
		validators = new HashMap<>();
		validators.put(MIME_OBFL, OBFLValidator.class);
		validators.put(MIME_PEF, PEFValidator.class);
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

	@Override
	public Validator newValidator(FileDetails details) throws ValidatorFactoryException {
		String identifier = details.getMediaType();
		Class<? extends Validator> c = validators.get(identifier);
		if (c!=null) {
			try {
				return c.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new ValidatorFactoryException("Cannot instantiate class.", e);
			}
		} else {
			// Try dtd validation
			Object sysId = details.getProperties().get("systemId");
			if (sysId!=null) {
				return new DtdValidator();
			}
			throw new ValidatorFactoryException("Factory for identifier not found: " + identifier);
		}
	}

	@Override
	public Optional<Double> supportsDetails(FileDetails details) {
		if (validators.containsKey(details.getMediaType())) {
			return Optional.of(10d);
		} else {
			Object sysId = details.getProperties().get("systemId");
			if (sysId!=null) {
				return Optional.ofNullable(-10d);
			}
		}
		return Optional.empty();
	}

}
