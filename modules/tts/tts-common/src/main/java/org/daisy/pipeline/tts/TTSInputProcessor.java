package org.daisy.pipeline.tts;

import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.daisy.common.transform.InputValue;
import org.daisy.common.transform.SingleInSingleOutXMLTransformer;
import org.daisy.common.transform.XMLInputValue;
import org.daisy.common.transform.XMLOutputValue;
import org.daisy.common.transform.XMLTransformer;
import org.daisy.common.xproc.calabash.XProcStepProvider;

/**
 * Prepares an input XML stream for TTS processing. The output is an SSML document that can be
 * passed to a {@link TTSEngine}.
 *
 * Objects that implement this interface are expected to also implement {@link
 * SingleInSingleOutXMLTransformer}, or a {@link XProcStepProvider} that provides {@link
 * SingleInSingleOutXMLTransformer}.
 *
 * The language of the input can be specified through a "language" parameter. It should not be
 * assumed that a {@code xml:lang} attribute is present when the input is an element.
 */
public interface TTSInputProcessor {

	public boolean supportsInputMediaType(String mediaType);

	public TTSInputProcessor forInputMediaType(String mediaType) throws UnsupportedOperationException;

	/**
	 * Priority of this processor compared to other processors (that supports a given media type).
	 */
	public int getPriority();

	public static Optional<Locale> getLanguage(Map<QName,InputValue<?>> params) throws IllegalArgumentException {
		InputValue<?> v = params.get(new QName("language"));
		if (v != null)
			try {
				Object o = v.asObject();
				if (o instanceof Locale)
					return Optional.of((Locale)o);
				else if (o instanceof String)
					try {
						return Optional.of(new Locale.Builder().setLanguageTag((String)o).build());
					} catch (IllformedLocaleException e) {
						throw new IllegalArgumentException("language can not be parsed: " + v, e);
					}
				else
					throw new IllegalArgumentException();
			} catch (UnsupportedOperationException e) {
				throw new IllegalArgumentException();
			}
		return Optional.empty();
	}
}
