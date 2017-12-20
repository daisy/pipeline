package org.daisy.dotify.tasks.impl.validity;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.daisy.dotify.tasks.impl.input.ValidatorException;
import org.daisy.dotify.tasks.impl.input.ValidatorTask;
import org.daisy.streamline.api.option.UserOption;
import org.daisy.streamline.api.option.UserOptionValue;
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
	private static final String FEATURE_MODE = "validator-mode";
	/**
	 * Defines the modes available to the validator.
	 */
	public enum Mode {
		/**
		 * Light mode validation only validates the document against the Relax NG schema
		 */
		LIGHT("resource-files/pef-2008-1.rng"), 
		/**
		 * In addition to schema validation, performs other tests required by the PEF-specification.
		 */
		FULL("resource-files/pef-2008-1.rng", "resource-files/pef-schematron.sch");
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
	private List<UserOption> options;
	
	/**
	 * Creates a new PEFValidator
	 */
	PEFValidator() {
	}

	@Override
	public ValidationReport validate(URL input, Map<String, Object> params) {
		Mode mode = Optional.ofNullable(params.get(FEATURE_MODE))
				.map(v-> {
					try {
						return Mode.valueOf(v.toString().toUpperCase());
					} catch (IllegalArgumentException e) {
						logger.warning(String.format("'%s' is not a recognized value for key %s", v.toString(), FEATURE_MODE));
						return null;
					}})
				.orElse(Mode.FULL);
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

	private List<UserOption> buildOptions() {
		List<UserOption> ret = new ArrayList<>();
		ret.add(new UserOption.Builder(FEATURE_MODE)
				.defaultValue(Mode.FULL.name())
				.addValue(new UserOptionValue.Builder(Mode.FULL.name().toLowerCase()).description("Runs all tests").build())
				.addValue(new UserOptionValue.Builder(Mode.LIGHT.name().toLowerCase()).description("Runs basic tests").build())
				.build());
		return ret;
	}

	@Override
	public List<UserOption> listOptions() {
		if (options==null) {
			options = Collections.unmodifiableList(buildOptions());
		}
		return options;
	}

}
