package org.daisy.pipeline.ocr;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.daisy.pipeline.datatypes.DatatypeService;
import org.daisy.pipeline.script.ScriptOption;

/**
 * A text recognition service. Can convert a PDF or image into well-structured
 * document.
 */
public interface OCRService {

	/**
	 * Unique ID of the service
	 */
	String getName();

	/**
	 * Nice name of the service, for displaying in user interfaces.
	 */
	String getDisplayName();

	/**
	 * Description of the service, for displaying in user interfaces.
	 */
	String getDescription();

	/**
	 * List the options that OCR processors from this service accept.
	 */
	default Iterable<ScriptOption> getOptions() {
		return Collections.emptyList();
	}

	/**
	 * Return the available OCR processors that this service provides. For AI
	 * based services, this may correspond to the available models.
	 *
	 * @param properties Must include any information needed by the service, such
	 *                   as authentication credentials for a cloud service, and may
	 *                   include additional settings to configure the behavior of
	 *                   OCR processors.
	 * @throws ServiceDisabledException if the service can not be activated.
	 */
	Collection<OCRProcessor> getAvailableProcessors(Map<String,String> properties)
		throws ServiceDisabledException;

	class ServiceDisabledException extends Exception {

		public ServiceDisabledException() {
			super();
		}

		public ServiceDisabledException(Throwable t) {
			super(t);
		}

		public ServiceDisabledException(String message) {
			super(message);
		}

		public ServiceDisabledException(String message, Throwable cause) {
			super(message, cause);
		}
	}

	class CommonOptions {
		private CommonOptions() {}

		public static final ScriptOption INCLUDE_PAGE_NUMBERS = new ScriptOption() {
				@Override public String getName() { return "include-page-numbers"; }
				@Override public String getNiceName() { return "Include page numbers"; }
				@Override public String getDescription() { return "Whether or not to include print page break indicators"; }
				@Override public boolean isRequired() { return false; }
				@Override public String getDefault() { return "true"; }
				@Override public DatatypeService getType() { return DatatypeService.XS_BOOLEAN; }
				@Override public String getMediaType() { return null; }
				@Override public boolean isSequence() { return false; }
				@Override public boolean isOrdered() { return false; }
				@Override public Role getRole() { return null; }
				@Override public boolean isPrimary() { return false; }};
	}
}
