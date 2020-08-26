package org.daisy.pipeline.tts;

import net.sf.saxon.s9api.XdmNode;

/**
 * Transform the SSML according to the TTS engine's features and bugs
 */
public interface SSMLTransformer {
	
	public XdmNode transform(XdmNode ssml);
	
}
