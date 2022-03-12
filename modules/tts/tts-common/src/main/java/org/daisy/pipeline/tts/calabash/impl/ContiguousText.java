package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * List of sentences adjacent to each other in the original document. Positions
 * in the document are kept so as to adequately name the sound files.
 */
class ContiguousText implements Comparable<ContiguousText> {

	ContiguousText(int documentPosition, File audioOutputDir) {
		mDocumentPosition = documentPosition;
		mAudioOutputDir = audioOutputDir;
		mDocumentSplitPosition = 0;
		sentences = new ArrayList<Sentence>();
	}

	void computeSize() {
		mSize = 0;
		for (Sentence speakable : sentences) {
			mSize += speakable.getSize();
		}
	}

	@Override
	public int compareTo(ContiguousText other) {
		return (other.mSize - mSize);
	}

	public void setDocumentSplitPosition(int pos) {
		mDocumentSplitPosition = pos;
	}

	public int getDocumentSplitPosition() {
		return mDocumentSplitPosition;
	}

	public int getDocumentPosition() {
		return mDocumentPosition;
	}

	public int getStringSize() {
		return mSize;
	}

	public void setStringsize(int size) {
		mSize = size;
	}

	public File getAudioOutputDir() {
		return mAudioOutputDir;
	}

	private File mAudioOutputDir;
	private int mDocumentSplitPosition;
	private int mDocumentPosition;
	private int mSize; //used for sorting
	List<Sentence> sentences;
}
