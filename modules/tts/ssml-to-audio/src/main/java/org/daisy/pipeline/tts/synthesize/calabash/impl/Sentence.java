package org.daisy.pipeline.tts.synthesize.calabash.impl;

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
			mSize = computeSize(mText);
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

	static int computeSize(XdmNode node) {
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

	private int mSize;
	private Voice mVoice;
	private XdmNode mText;
	private TTSEngine mTTSproc;
}
