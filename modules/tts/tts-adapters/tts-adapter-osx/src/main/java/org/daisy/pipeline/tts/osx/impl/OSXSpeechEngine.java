package org.daisy.pipeline.tts.osx.impl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.shell.CommandRunner;
import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.MarklessTTSEngine;
import org.daisy.pipeline.tts.SoundUtil;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OSXSpeechEngine extends MarklessTTSEngine {

	private AudioFormat mAudioFormat;
	private String mSayPath;
	private int mPriority;
	private final static int MIN_CHUNK_SIZE = 2048;
	private final static Logger mLogger = LoggerFactory.getLogger(OSXSpeechEngine.class);

	public OSXSpeechEngine(OSXSpeechService service, String osxPath, int priority) {
		super(service);
		mPriority = priority;
		mSayPath = osxPath;
	}

	@Override
	public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
	        Voice voice, TTSResource threadResources,
	        AudioBufferAllocator bufferAllocator, boolean retry) throws SynthesisException,
	        InterruptedException, MemoryException {

		Collection<AudioBuffer> result = new ArrayList<AudioBuffer>();
		File waveOut = null;
		try {
			waveOut = File.createTempFile("pipeline", ".wav");
			new CommandRunner(mSayPath, "--data-format=LEI16@22050", "-o",
			                  waveOut.getAbsolutePath(), "-v", voice.name)
				.feedInput(sentence.getBytes("utf-8"))
				.consumeError(mLogger)
				.run();
			
			// read the wave on the standard output
			BufferedInputStream in = new BufferedInputStream(new FileInputStream(waveOut));
			AudioInputStream fi = AudioSystem.getAudioInputStream(in);

			if (mAudioFormat == null)
				mAudioFormat = fi.getFormat();

			while (true) {
				AudioBuffer b = bufferAllocator
				        .allocateBuffer(MIN_CHUNK_SIZE + fi.available());
				int ret = fi.read(b.data, 0, b.size);
				if (ret == -1) {
					//note: perhaps it would be better to call allocateBuffer()
					//somewhere else in order to avoid this extra call:
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
		} finally {
			if (waveOut != null)
				waveOut.delete();
		}
		return result;
	}

	@Override
	public AudioFormat getAudioOutputFormat() {
		return mAudioFormat;
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
	        InterruptedException {

		Collection<Voice> result = new ArrayList<Voice>();
		try {
			new CommandRunner(mSayPath, "-v", "?")
				.consumeOutput(stream -> {
						Matcher mr = Pattern.compile("(.*?)\\s+\\w{2}_\\w{2}").matcher("");
						try (Scanner scanner = new Scanner(stream)) {
							while (scanner.hasNextLine()) {
								mr.reset(scanner.nextLine());
								if (mr.find()) {
									result.add(new Voice(getProvider().getName(), mr.group(1).trim()));
								}
							}
						}
					}
				)
				.consumeError(mLogger)
				.run();
		} catch (Throwable e) {
			throw new SynthesisException(e.getMessage(), e.getCause());
		}

		return result;
	}

	@Override
	public int getOverallPriority() {
		return mPriority;
	}

}
