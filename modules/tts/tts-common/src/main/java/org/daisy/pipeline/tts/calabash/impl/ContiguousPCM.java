package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;

import javax.sound.sampled.AudioInputStream;

import org.daisy.pipeline.tts.AudioFootprintMonitor;

/**
 * ContiguousPCMs are the message objects sent from TTS processors to the
 * encoders. Every ContiguousPCM contains a AudioInputStream and attributes
 * related to the URI of the sound file to produce.
 */
class ContiguousPCM implements Comparable<ContiguousPCM> {

	static ContiguousPCM EndOfQueue = new ContiguousPCM();

	ContiguousPCM() {
		mEncodingTimeApprox = -1;
		mSizeInBytes = minByteSize();
	}

	boolean isEndOfQueue() {
		return (mEncodingTimeApprox == -1);
	}

	ContiguousPCM(AudioInputStream audio, File destdir, String destFilePrefix) {
		mDestURI = new StringBuilder();
		mAudio = audio;
		mDestDir = destdir;
		mDestFilePrefix = destFilePrefix;
		int size = AudioFootprintMonitor.getFootprint(audio);
		mSizeInBytes = minByteSize() + size;
		mEncodingTimeApprox = size / audio.getFormat().getSampleSizeInBits();
	}

	@Override
	public int compareTo(ContiguousPCM o) {
		return (o.mEncodingTimeApprox - mEncodingTimeApprox); //descending order => biggest chunks will be polled first
	}

	String getDestinationFilePrefix() {
		return mDestFilePrefix;
	}

	File getDestinationDirectory() {
		return mDestDir;
	}

	AudioInputStream getAudio() {
		return mAudio;
	}

	StringBuilder getURIholder() {
		return mDestURI;
	}

	public int sizeInBytes() {
		return mSizeInBytes;
	}

	private int minByteSize() {
		return 500;//rough approximation of an empty ContiguousPCM's memory footprint
	}

	private AudioInputStream mAudio;
	private int mEncodingTimeApprox; //used for sorting
	private int mSizeInBytes; //used for monitoring the memory footprint
	private File mDestDir;
	private String mDestFilePrefix;
	private StringBuilder mDestURI; //simple way to hold a string

}
