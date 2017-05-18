package org.daisy.dotify.impl.validity;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.api.validity.ValidationReport;
import org.daisy.dotify.api.validity.Validator;
import org.daisy.dotify.impl.input.ObflResourceLocator;
import org.daisy.dotify.impl.input.ObflResourceLocator.ObflResourceIdentifier;
import org.daisy.dotify.impl.input.ValidatorException;
import org.daisy.dotify.impl.input.ValidatorTask;

class OBFLValidator implements Validator {
	private static final Logger logger = Logger.getLogger(OBFLValidator.class.getCanonicalName());

	@Override
	public ValidationReport validate(URL input) {
		try {
			return ValidatorTask.validate(input,
					false,
					ObflResourceLocator.getInstance().getResourceByIdentifier(ObflResourceIdentifier.OBFL_RNG_SCHEMA));
		} catch (ValidatorException e) {
			logger.log(Level.WARNING, "Failed to validate.", e);
		}
		return null;
	}

}
