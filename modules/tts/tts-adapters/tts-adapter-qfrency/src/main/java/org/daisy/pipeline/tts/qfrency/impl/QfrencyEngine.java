package org.daisy.pipeline.tts.qfrency.impl;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.shell.CommandRunner;
import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.SoundUtil;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QfrencyEngine extends TTSEngine {

	private AudioFormat mAudioFormat;
	private String mHostAddress;
	private String mQfrencyPath;
	private final static int MIN_CHUNK_SIZE = 2048;
	private int mPriority;
	private final static Logger mLogger = LoggerFactory.getLogger(QfrencyEngine.class);

	public QfrencyEngine(QfrencyService qfrencyService, String qfrencyPath, String address, int priority) {
		super(qfrencyService);
		mQfrencyPath = qfrencyPath;
		mPriority = priority;
		mHostAddress = address;
	}

	@Override
	public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
											  Voice voice, TTSResource threadResources, List<Mark> marks, List<String> oldMarks,
											  AudioBufferAllocator bufferAllocator, boolean retry) throws SynthesisException,
																										  InterruptedException, MemoryException {

		Collection<AudioBuffer> result = new ArrayList<AudioBuffer>();
		File outFile = null;
		String outPath = null;
		sentence = stripSSML(sentence);
		try {
					outFile = File.createTempFile("dp2_qfrency_", ".wav");
		outFile.deleteOnExit();
		outPath = outFile.getPath();
			String [] lCmd = new String[7];
			lCmd[0]=mQfrencyPath;
			lCmd[1]="-a";
			lCmd[2]=mHostAddress;
			lCmd[3]="-s";
			lCmd[4]=outPath;
			lCmd[5]=voice.name;
			lCmd[6]="\'"+sentence+"\'";

			new CommandRunner(lCmd)
				.consumeError(mLogger)
				.run();

			BufferedInputStream in = new BufferedInputStream(new FileInputStream(outFile));
			AudioInputStream fi = AudioSystem.getAudioInputStream(in);

			if (mAudioFormat == null)
				mAudioFormat = fi.getFormat();

			while (true) {
				AudioBuffer b = bufferAllocator
					.allocateBuffer(MIN_CHUNK_SIZE + fi.available());
				int ret = fi.read(b.data, 0, b.size);
				if (ret == -1) {
					bufferAllocator.releaseBuffer(b);
					break;
				}
				b.size = ret;
				result.add(b);
			}
			fi.close();


		} catch (MemoryException|InterruptedException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			throw e;
		} catch (Throwable e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new SynthesisException(e);
		}
		outFile.delete();
		new File(outPath+".sutt").delete();
		new File(outPath+".TextGrid").delete();
		return result;
	}

	public int reservedThreadNum() {
		return 1;
	}

	
	@Override
	public AudioFormat getAudioOutputFormat() {
		return mAudioFormat;
	}


	/**
	 * Need not be thread-safe. This method is called from the main thread.
	 */
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
														 InterruptedException {
		ArrayList<Voice> list = new ArrayList<Voice>();
		try {
			new CommandRunner(mQfrencyPath, "-a", mHostAddress, "-l")
				.consumeOutput(stream -> {
						try (Scanner scanner = new Scanner(stream)) {
							while (scanner.hasNext())
								list.add(new Voice(getProvider().getName(), scanner.next()));
						}
					}
				)
				.consumeError(mLogger)
				.run();
			return list;
		} catch (Throwable x) {
			System.out.println("Error listing voices");
			x.printStackTrace();
		}
		return new ArrayList<Voice>();
	}

	
	@Override
	public int getOverallPriority() {
		return mPriority;
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException,
														InterruptedException {
		return new TTSResource();
	}

	
    private String stripSSML(String in) {
		String ret = "";
		boolean inTag=false;
		for (int i = 0; i < in.length(); i++) {
			if (in.charAt(i)=='<')
				inTag=true;
			else if (in.charAt(i)=='>')
				inTag=false;
			if (!inTag && in.charAt(i)!='>')
				ret+=in.charAt(i);
		}
		return ret;
    }
}
