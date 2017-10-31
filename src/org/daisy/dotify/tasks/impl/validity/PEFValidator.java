package org.daisy.dotify.tasks.impl.validity;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.tasks.impl.input.ValidatorException;
import org.daisy.dotify.tasks.impl.input.ValidatorTask;
import org.daisy.streamline.api.validity.ValidationReport;
import org.daisy.streamline.api.validity.Validator;

/**
 * Validates PEF-documents against the official Relax NG schema. Optionally performs additional
 * checks, see the different modes. 
 * @author Joel Håkansson
 */
class PEFValidator implements Validator {

	/**
	 * Key for getFeature/setFeature,
	 * corresponding value should be a {@link Mode} value
	 */
	public static final String FEATURE_MODE = "validator mode";
	/**
	 * Defines the modes available to the validator.
	 */
	public enum Mode {
		/**
		 * Light mode validation only validates the document against the Relax NG schema
		 */
		LIGHT_MODE("resource-files/pef-2008-1.rng"), 
		/**
		 * In addition to schema validation, performs other tests required by the PEF-specification.
		 */
		FULL_MODE("resource-files/pef-2008-1.rng", "resource-files/pef-schematron.sch");
		private final URL[] schemas;
		Mode(String ... schemaPaths) {
			List<URL> schemas = new ArrayList<>();
			for (String path : schemaPaths) {
				schemas.add(this.getClass().getResource(path));
			}
			this.schemas = schemas.toArray(new URL[schemas.size()]);
		}
	}
	private static final Logger logger = Logger.getLogger(PEFValidator.class.getCanonicalName());
	private Mode mode;
	
	/**
	 * Creates a new PEFValidator
	 */
	PEFValidator() {
		this(PEFValidator.class.getCanonicalName());
	}

	PEFValidator(String id) {
		this.mode = Mode.FULL_MODE;
	}

	@Override
	public ValidationReport validate(URL input) {
		return validate(input, mode);
	}
	
	private ValidationReport validate(URL input, Mode modeLocal) {
		
		try {
			return ValidatorTask.validate(input, false, modeLocal.schemas);
		} catch (ValidatorException e) {
			logger.log(Level.WARNING, "Failed to validate.", e);
		}
		return null;
	}
	
	Object getFeature(String key) {
		if (FEATURE_MODE.equals(key)) {
			return mode;
		} else {
			throw new IllegalArgumentException("Unknown feature: '" + key +"'");
		}
	}

	void setFeature(String key, Object value) {
		if (FEATURE_MODE.equals(key)) {
			try {
				mode = (Mode)value;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Unsupported value for " + FEATURE_MODE + " '" + value + "'", e);
			}
		} else {
			throw new IllegalArgumentException("Unknown feature: '" + key +"'");
		}
		
	}

}
