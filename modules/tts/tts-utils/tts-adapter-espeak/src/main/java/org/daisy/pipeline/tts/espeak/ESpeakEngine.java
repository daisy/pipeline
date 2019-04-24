package org.daisy.pipeline.tts.espeak;

import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
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

public class ESpeakEngine extends MarklessTTSEngine {

	private AudioFormat mAudioFormat;
	private String[] mCmd;
	private String mESpeakPath;
	private final static int MIN_CHUNK_SIZE = 2048;
	private int mPriority;
	private final static Logger mLogger = LoggerFactory.getLogger(ESpeakEngine.class);

	public ESpeakEngine(ESpeakService eSpeakService, String eSpeakPath, int priority) {
		super(eSpeakService);
		mESpeakPath = eSpeakPath;
		mPriority = priority;
		mCmd = new String[]{
		        eSpeakPath, "-m", "--stdout", "--stdin"
		};
	}

	@Override
	public Collection<AudioBuffer> synthesize(String sentence, XdmNode xmlSentence,
	        Voice voice, TTSResource threadResources, AudioBufferAllocator bufferAllocator, boolean retry)
	        		throws SynthesisException,InterruptedException, MemoryException {

		Collection<AudioBuffer> result = new ArrayList<AudioBuffer>();
		try {
			new CommandRunner(mCmd)
				.feedInput(sentence.getBytes("utf-8"))
				// read the wave on the standard output
				.consumeOutput(stream -> {
						BufferedInputStream in = new BufferedInputStream(stream);
						AudioInputStream fi = AudioSystem.getAudioInputStream(in);
						if (mAudioFormat == null)
							mAudioFormat = fi.getFormat();
						while (true) {
							AudioBuffer b = bufferAllocator
								.allocateBuffer(MIN_CHUNK_SIZE + fi.available());
							int ret = fi.read(b.data, 0, b.size);
							if (ret == -1) {
								// note: perhaps it would be better to call allocateBuffer()
								// somewhere else in order to avoid this extra call:
								bufferAllocator.releaseBuffer(b);
								break;
							}
							b.size = ret;
							result.add(b);
						}
						fi.close();
				})
				.consumeError(mLogger)
				.run();
		} catch (MemoryException|InterruptedException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			throw e;
		} catch (Throwable e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			throw new SynthesisException(e);
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
		Collection<Voice> result;
		try {
			// First: get the list of all the available languages
			Set<String> languages = new HashSet<String>();
			new CommandRunner(mESpeakPath, "--voices")
				// parse output
				.consumeOutput(stream -> {
						try (Scanner scanner = new Scanner(stream)) {
							Matcher mr = Pattern.compile("\\s*[0-9]+\\s+([-a-z]+)").matcher("");
							scanner.nextLine(); //headers
							while (scanner.hasNextLine()) {
								mr.reset(scanner.nextLine());
								mr.find();
								languages.add(mr.group(1).split("-")[0]);
							}
						}
					}
				)
				.consumeError(mLogger)
				.run();
			
			// Second: get the list of the voices for the found languages.
			// White spaces are not allowed in voice names
			result = new ArrayList<Voice>();
			Matcher mr = Pattern.compile("^\\s*[0-9]+\\s+[-a-z]+\\s+([FM]\\s+)?([^ ]+)").matcher("");
			for (String lang : languages) {
				new CommandRunner(mESpeakPath, "--voices=" + lang)
					.consumeOutput(stream -> {
							try (Scanner scanner = new Scanner(stream)) {
								scanner.nextLine(); // headers
								while (scanner.hasNextLine()) {
									mr.reset(scanner.nextLine());
									mr.find();
									result.add(new Voice(getProvider().getName(), mr.group(2).trim()));
								}
							}
						}
					)
					.consumeError(mLogger)
					.run();
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Throwable e) {
			throw new SynthesisException(e.getMessage(), e.getCause());
		}

		return result;
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
}
