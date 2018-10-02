package org.daisy.pipeline.tts.espeak;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.MarklessTTSEngine;
import org.daisy.pipeline.tts.SoundUtil;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.Mark;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;

public class ESpeakEngine extends MarklessTTSEngine {

	private AudioFormat mAudioFormat;
	private String[] mCmd;
	private String mESpeakPath;
	private final static int MIN_CHUNK_SIZE = 2048;
	private int mPriority;

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
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(mCmd);

			//write the SSML
			BufferedOutputStream out = new BufferedOutputStream((p.getOutputStream()));
			out.write(sentence.getBytes("utf-8"));
			out.close();

			//read the wave on the standard output
			BufferedInputStream in = new BufferedInputStream(p.getInputStream());
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
			waitFor(p, mCmd);
		} catch (MemoryException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			p.destroy();
			throw e;
		} catch (InterruptedException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			if (p != null)
				p.destroy();
			throw e;
		} catch (Exception e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			if (p != null)
				p.destroy();
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
		InputStream is;
		Process proc = null;
		Scanner scanner = null;
		Matcher mr;
		try {
			//First: get the list of all the available languages
			Set<String> languages = new HashSet<String>();
			String[] cmd = new String[]{
				mESpeakPath, "--voices"
			};
			proc = Runtime.getRuntime().exec(cmd);
			is = proc.getInputStream();
			mr = Pattern.compile("\\s*[0-9]+\\s+([-a-z]+)").matcher("");
			scanner = new Scanner(is);
			scanner.nextLine(); //headers
			while (scanner.hasNextLine()) {
				mr.reset(scanner.nextLine());
				mr.find();
				languages.add(mr.group(1).split("-")[0]);
			}
			is.close();
			waitFor(proc, cmd);
			proc = null;

			//Second: get the list of the voices for the found languages.
			//White spaces are not allowed in voice names
			result = new ArrayList<Voice>();
			mr = Pattern.compile("^\\s*[0-9]+\\s+[-a-z]+\\s+([FM]\\s+)?([^ ]+)").matcher("");
			for (String lang : languages) {
				cmd = new String[]{
					mESpeakPath, "--voices=" + lang
				};
				proc = Runtime.getRuntime().exec(cmd);
				is = proc.getInputStream();
				scanner = new Scanner(is);
				scanner.nextLine(); //headers
				while (scanner.hasNextLine()) {
					mr.reset(scanner.nextLine());
					mr.find();
					result.add(new Voice(getProvider().getName(), mr.group(2).trim()));
				}
				is.close();
				waitFor(proc, cmd);
			}

		} catch (InterruptedException e) {
			if (proc != null) {
				proc.destroy();
			}
			throw e;
		} catch (Exception e) {
			if (proc != null) {
				proc.destroy();
			}
			throw new SynthesisException(e.getMessage(), e.getCause());
		} finally {
			if (scanner != null)
				scanner.close();
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

	private void waitFor(Process proc, String[] command) throws InterruptedException {
		try {
			proc.waitFor();
		} catch (InterruptedException e) {
			throw new InterruptedException("Interrupted while running command `" + String.join(" ", command) + "`");
		}
	}
}
