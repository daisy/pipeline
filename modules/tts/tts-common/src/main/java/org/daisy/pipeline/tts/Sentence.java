package org.daisy.pipeline.tts;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.pipeline.tts.calabash.impl.FormatSpecifications;

import com.google.common.base.CharMatcher;

public class Sentence implements FormatSpecifications {

	private int size;
	private final Voice voice;
	private final XdmNode text;
	private final TTSEngine ttsProc;

	public Sentence(TTSEngine ttsProc, Voice voice, XdmNode text) {
		this.voice = voice;
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

	public Voice getVoice() {
		return voice;
	}

	public XdmNode getText() {
		return text;
	}

	public TTSEngine getTTSproc() {
		return ttsProc;
	}

	public String getID() {
		return text.getAttributeValue(Sentence_attr_id);
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
