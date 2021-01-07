package org.daisy.pipeline.tts.cereproc.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.sound.sampled.AudioFormat;

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
import org.daisy.pipeline.tts.VoiceInfo.Gender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CereProcEngine extends MarklessTTSEngine {

	private final static Logger logger = LoggerFactory.getLogger(CereProcEngine.class);
	private static File tmpDirectory = null;

	private AudioFormat audioFormat;
	private final int priority;
	private final String[] cmd;
	private final int expectedMillisecPerWord;

	enum Variant {
		STANDARD,
		DNN
	}

	public CereProcEngine(Variant variant, CereProcService service, String server, int port, File client, int priority)
			throws SynthesisException {
		super(service);
		this.priority = priority;
		if (!client.exists())
			throw new SynthesisException("No CereProc client installed at " + client);
		if (tmpDirectory == null) {
			try {
				tmpDirectory = Files.createTempDirectory("cereproc-").toFile();
				tmpDirectory.deleteOnExit();
				tmpDirectory = tmpDirectory.toPath().toRealPath().normalize().toFile();
			} catch (IOException e) {
				throw new SynthesisException("Could not initialize CereProc engine", e);
			}
		}
		this.cmd = new String[]{client.getAbsolutePath(),
		                        "-H", server,
		                        "-p", ""+port,
		                        "-o", tmpDirectory.getAbsolutePath()};
		int sampleRate; // sample rate in Hz
		int sampleBits = 16; // sample size in bits
		switch (variant) {
		case DNN:
			sampleRate = 16000;
			this.expectedMillisecPerWord = 500;
			break;
		case STANDARD:
		default:
			sampleRate = 48000;
			this.expectedMillisecPerWord = 200;
		}
		// FIXME: don't hard code
		this.audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
		                                   sampleRate,
		                                   sampleBits,
		                                   1,                                 // mono
		                                   2,                                 // frame size in bytes
		                                   sampleRate * sampleBits / (2 * 8), // frame rate
		                                   false                              // little endian
		                                   );
	}

	@Override
	public int getOverallPriority() {
		return priority;
	}

	@Override
	public AudioFormat getAudioOutputFormat() {
		return audioFormat;
	}

	@Override
	public int expectedMillisecPerWord() {
		return expectedMillisecPerWord;
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
		// FIXME: don't hard code
		List<Voice> voices = new ArrayList<>();
		voices.add(new Voice(getProvider().getName(), "William", Locale.forLanguageTag("en-GB"), Gender.MALE_ADULT));
		voices.add(new Voice(getProvider().getName(), "Ylva", Locale.forLanguageTag("sv"), Gender.FEMALE_ADULT));
		return voices;
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException, InterruptedException {
		return new TTSResource();
	}

	@Override
	public Collection<AudioBuffer> synthesize(String sentence,
	                                          XdmNode xmlSentence,
	                                          Voice voice,
	                                          TTSResource threadResources,
	                                          AudioBufferAllocator bufferAllocator,
	                                          boolean retry)
			throws SynthesisException, InterruptedException, MemoryException {
		Collection<AudioBuffer> result = new ArrayList<>();
		StringWriter out = new StringWriter();
		StringWriter err = new StringWriter();
		File txtFile;
		try {
			txtFile = File.createTempFile("tmp", ".txt", tmpDirectory);
		} catch (IOException e) {
			throw new SynthesisException(e);
		}
		File audioFile = new File(tmpDirectory, txtFile.getName().replaceAll(".txt$", ".raw"));
		try {
			String[] cmd = new String[this.cmd.length + 3];
			System.arraycopy(this.cmd, 0, cmd, 0, this.cmd.length);
			cmd[cmd.length - 3] = "-V";
			cmd[cmd.length - 2] = voice.name;
			cmd[cmd.length - 1] = txtFile.getAbsolutePath();
			try (OutputStream os = new FileOutputStream(txtFile)) {
				Writer w = new OutputStreamWriter(os, UTF_8);
				w.write(sentence.replace('\n', ' '));
				w.write("\n");
				try {
					w.flush();
				} catch (IOException e) {
				}
			}
			int ret = new CommandRunner(cmd)
				.consumeOutput(
					stream -> {
						try (Reader r = new InputStreamReader(stream)) {
							for (int c = r.read(); c != -1; c = r.read()) out.write((char)c);
						}
					}
				)
				.consumeError(
					stream -> {
						try (Reader r = new InputStreamReader(stream)) {
							for (int c = r.read(); c != -1; c = r.read()) err.write((char)c);
						}
					}
				)
				.run();
			if (ret != 0)
				throw new RuntimeException("Return value was " + ret);
			if (!audioFile.exists())
				throw new RuntimeException("No audio file was produced");
			if (out.getBuffer().length() > 0)
				logger.trace(out.toString());
			if (err.getBuffer().length() > 0)
				logger.trace(err.toString());
			try (InputStream is = new FileInputStream(audioFile)) {
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				byte[] buf = new byte[8192];
				int len;
				while ((len = is.read(buf)) > 0)
					bytes.write(buf, 0, len);
				AudioBuffer b = bufferAllocator.allocateBuffer(bytes.size());
				System.arraycopy(bytes.toByteArray(), 0, b.data, 0, b.data.length);
				result.add(b);
			}
		} catch (MemoryException|InterruptedException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			throw e;
		} catch (Throwable e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			logger.error(out.toString());
			logger.error(err.toString());
			throw new SynthesisException(e);
		}
		return result;
	}
}
