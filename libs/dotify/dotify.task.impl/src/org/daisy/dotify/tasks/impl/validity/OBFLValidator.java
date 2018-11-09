package org.daisy.dotify.tasks.impl.validity;

import java.net.URL;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.tasks.impl.input.ObflResourceLocator;
import org.daisy.dotify.tasks.impl.input.ObflResourceLocator.ObflResourceIdentifier;
import org.daisy.dotify.tasks.impl.input.ValidatorException;
import org.daisy.dotify.tasks.impl.input.ValidatorTask;
import org.daisy.streamline.api.media.InputStreamSupplier;
import org.daisy.streamline.api.validity.ValidationReport;
import org.daisy.streamline.api.validity.Validator;

class OBFLValidator implements Validator {
	private static final Logger logger = Logger.getLogger(OBFLValidator.class.getCanonicalName());

	@Override
	public ValidationReport validate(URL input, Map<String, Object> params) {
		try {
			return ValidatorTask.validate(input,
					false,
					ObflResourceLocator.getInstance().getResourceByIdentifier(ObflResourceIdentifier.OBFL_RNG_SCHEMA));
		} catch (ValidatorException e) {
			logger.log(Level.WARNING, "Failed to validate.", e);
		}
		return null;
	}

	@Override
	public ValidationReport validate(InputStreamSupplier input, Map<String, Object> options) {
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
