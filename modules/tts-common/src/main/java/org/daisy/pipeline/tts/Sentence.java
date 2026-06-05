package org.daisy.pipeline.tts;

import java.net.URI;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.pipeline.tts.calabash.impl.FormatSpecifications;

import com.google.common.base.CharMatcher;

public class Sentence implements FormatSpecifications {

	private final String id;
	private int size;
	private final Iterable<Voice> voices;
	private final XdmNode text;
	private final TTSEngine ttsProc;

	/**
	 * @param voices Assumed to contain at least one voice.
	 */
	public Sentence(TTSEngine ttsProc, Iterable<Voice> voices, XdmNode text) {
		id = text.getAttributeValue(Sentence_attr_id);
		if (id == null)
			throw new IllegalArgumentException("id must not be null");
		this.voices = voices;
		this.text = text;
		this.ttsProc = ttsProc;
		size = -1;
	}

	public int getSize() {
		if (size == -1) {
			size = 0;
			size = computeSize(text);
		}
		return size;
	}

	/**
	 * Get the preferred voice
	 */
	public Voice getPreferredVoice() {
		return voices.iterator().next();
	}

	/**
	 * Get the complete ordered set of selected voices
	 */
	public Iterable<Voice> getVoices() {
		return voices;
	}

	public XdmNode getText() {
		return text;
	}

	public TTSEngine getTTSproc() {
		return ttsProc;
	}

	public String getID() {
		return id;
	}

	public URI getBaseURI() {
		return text.getBaseURI();
	}

	public static int computeSize(XdmNode node) {
		int size = 0;
		if (node.getNodeKind() == XdmNodeKind.TEXT) {
			size += CharMatcher.WHITESPACE.removeFrom(node.getStringValue()).length();
		} else {
			XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
			while (iter.hasNext()) {
				size += computeSize((XdmNode) iter.next());
			}
		}
		return size;
	}
}
