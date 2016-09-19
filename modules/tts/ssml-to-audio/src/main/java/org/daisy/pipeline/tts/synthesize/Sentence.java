package org.daisy.pipeline.tts.synthesize;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.Voice;

import com.google.common.base.CharMatcher;

class Sentence implements FormatSpecifications {
	Sentence(TTSEngine ttsProc, Voice voice, XdmNode text) {
		mVoice = voice;
		mText = text;
		mTTSproc = ttsProc;
		mSize = -1;
	}

	int getSize() {
		if (mSize == -1) {
			mSize = 0;
			computeSize(mText);
		}
		return mSize;
	}

	Voice getVoice() {
		return mVoice;
	}

	XdmNode getText() {
		return mText;
	}

	TTSEngine getTTSproc() {
		return mTTSproc;
	}

	String getID() {
		return mText.getAttributeValue(Sentence_attr_id);
	}

	private void computeSize(XdmNode node) {
		if (node.getNodeKind() == XdmNodeKind.TEXT) {
			mSize += CharMatcher.WHITESPACE.removeFrom(node.getStringValue()).length();
		} else {
			XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
			while (iter.hasNext()) {
				computeSize((XdmNode) iter.next());
			}
		}
	}

	private int mSize;
	private Voice mVoice;
	private XdmNode mText;
	private TTSEngine mTTSproc;
}
