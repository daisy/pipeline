package org.daisy.pipeline.tts.synthesize.calabash.impl;

import java.io.File;

import javax.sound.sampled.AudioFormat;

import org.daisy.pipeline.audio.AudioBuffer;

/**
 * ContiguousPCMs are the message objects sent from TTS processors to the
 * encoders. Every ContiguousPCM contains a list of audio buffers and attributes
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

	ContiguousPCM(AudioFormat audioformat, Iterable<AudioBuffer> buffers, File destdir,
	        String destFilePrefix) {
		mDestURI = new StringBuilder();
		mAudioFormat = audioformat;
		mBuffers = buffers;
		mDestDir = destdir;
		mDestFilePrefix = destFilePrefix;
		mEncodingTimeApprox = 0;
		for (AudioBuffer buffer : buffers)
			mEncodingTimeApprox += buffer.size;
		mEncodingTimeApprox /= mAudioFormat.getSampleSizeInBits();
		mSizeInBytes = minByteSize();
		for (AudioBuffer buffer : buffers)
			mSizeInBytes += buffer.data.length;
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

	AudioFormat getAudioFormat() {
		return mAudioFormat;
	}

	Iterable<AudioBuffer> getBuffers() {
		return mBuffers;
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

	private Iterable<AudioBuffer> mBuffers;
	private int mEncodingTimeApprox; //used for sorting
	private int mSizeInBytes; //used for monitoring the memory footprint
	private AudioFormat mAudioFormat;
	private File mDestDir;
	private String mDestFilePrefix;
	private StringBuilder mDestURI; //simple way to hold a string

}
