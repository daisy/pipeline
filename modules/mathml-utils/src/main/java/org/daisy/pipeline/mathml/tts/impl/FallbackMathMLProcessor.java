package org.daisy.pipeline.mathml.tts.impl;

import java.net.URI;

import org.daisy.common.file.URLs;
import org.daisy.common.xproc.calabash.XProcBasedTransformer;
import org.daisy.pipeline.tts.TTSInputProcessor;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "FallbackMathMLProcessor",
	service = { TTSInputProcessor.class }
)
public class FallbackMathMLProcessor extends XProcBasedTransformer implements TTSInputProcessor {

	private final static String MIME_MATHML = "application/mathml+xml";
	private final static URI xproc = URLs.asURI(URLs.getResourceFromJAR("xml/xproc/mathml-to-ssml.impl/mathml-to-ssml.xpl",
	                                                                    FallbackMathMLProcessor.class));

	public FallbackMathMLProcessor() {
		super(xproc, null);
	}

	@Override
	public int getPriority() {
		return 1;
	}

	@Override
	public boolean supportsInputMediaType(String mediaType) {
		return MIME_MATHML.equals(mediaType);
	}

	@Override
	public FallbackMathMLProcessor forInputMediaType(String mediaType) {
		if (!MIME_MATHML.equals(mediaType))
			throw new UnsupportedOperationException("This processor does not support the input media type " + mediaType);
		return this;
	}
}
