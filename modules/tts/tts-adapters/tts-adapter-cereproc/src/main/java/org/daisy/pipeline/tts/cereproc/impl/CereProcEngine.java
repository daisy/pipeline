package org.daisy.pipeline.tts.cereproc.impl;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

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

	private AudioFormat audioFormat;
	private final int priority;
	private final String[] cmd;

	public CereProcEngine(CereProcService service, String server, int port, File clientDir, int priority)
			throws SynthesisException {
		super(service);
		this.priority = priority;
		File libDir = new File(clientDir, "lib");
		if (!libDir.isDirectory())
			throw new SynthesisException("No CereProc client installed in " + clientDir);
		this.cmd = new String[]{"java",
		                        "-Djava.library.path=" + libDir,
		                        "-jar",
		                        ""+new File(clientDir, "cserver-client-java.jar"),
		                        server,
		                        ""+port};
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
		StringWriter err = new StringWriter();
		try {
			SubprocessIO io = new SubprocessIO();
			String[] cmd = new String[this.cmd.length + 2];
			System.arraycopy(this.cmd, 0, cmd, 0, this.cmd.length);
			cmd[cmd.length - 2] = "FILIBUSTER";
			cmd[cmd.length - 1] = voice.name;
			int ret = new CommandRunner(cmd)
				.feedInput(
					stream -> io.writeSentence(stream, sentence)
				)
				.consumeOutput(
					stream -> {
						result.add(io.readAudio(stream, bufferAllocator));
						stream.close();
						io.quit();
					}
				)
				.consumeError(
					stream -> {
						try (Reader r = new InputStreamReader(stream)) {
							for (int c = r.read(); c != -1; c = r.read()) err.write((char)c);
						}
					}
				)
				.run(io::getTimeout);
			if (ret != 0)
				throw new RuntimeException("Return value was " + ret);
			if (err.getBuffer().length() > 0)
			  logger.trace(err.toString());
		} catch (MemoryException|InterruptedException e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			throw e;
		} catch (Throwable e) {
			SoundUtil.cancelFootPrint(result, bufferAllocator);
			logger.error(err.toString());
			throw new SynthesisException(e);
		}
		return result;
	}

	private class SubprocessIO {

		Writer writer;
		long quitTime = -1;

		void writeSentence(OutputStream stream, String sentence) throws IOException {
			sentence = sentence.replace('\n', ' ');
			writer = new OutputStreamWriter(stream, UTF_8);
			writer.write(sentence);
			writer.write("\n");
			try {
				writer.flush();
			} catch (IOException e) {
			}
		}

		AudioBuffer readAudio(InputStream stream, AudioBufferAllocator bufferAllocator) throws IOException, MemoryException {
			// first read length of audio data
			int len; {
				String s = "";
				int c;
				waitForStreamAvailable(stream, 10000);
				while ((c = stream.read()) != '\n') {
					if (c < 0) throw new RuntimeException("coding error");
					s += (char)c;
					waitForStreamAvailable(stream, 10000);
				}
				len = Integer.parseInt(s);
			}
			// then read the audio
			try {
				// this assumes stream supports mark/reset (wrap in a BufferedInputStream to be sure)
				stream = AudioSystem.getAudioInputStream(stream); }
			catch (UnsupportedAudioFileException e) {
				throw new IOException(e); }
			if (CereProcEngine.this.audioFormat == null)
				CereProcEngine.this.audioFormat = ((AudioInputStream)stream).getFormat();
			len -= 44; // because of the header that AudioInputStream strips
			           // FIXME: very brittle: move AudioSystem.getAudioInputStream(...) after this
			byte[] audio = new byte[len];
			do {
				waitForStreamAvailable(stream, 10000);
				int read = stream.read(audio, audio.length - len, len);
				if (read < 0) throw new RuntimeException("coding error");
				len -= read;
			} while (len > 0);
			AudioBuffer b = bufferAllocator.allocateBuffer(audio.length);
			System.arraycopy(audio, 0, b.data, 0, b.data.length);
			return b;
		}

		// make the subprocess quit
		void quit() throws IOException {
			// assuming that writer has been initialized
			writer.write(System.getProperty("line.separator"));
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
			}
			quitTime = System.currentTimeMillis();
		}

		// whether to wait longer for the process to return
		Long getTimeout() {
			return (quitTime < 0 || System.currentTimeMillis() - quitTime < 10000)
				? 200L
				: 0L;
		}
	}

	/**
	 * @param timeout in msec
	 */
	private static long waitForStreamAvailable(InputStream stream, long timeout) throws IOException {
		long poll = 200; // msec
		long startTime = System.currentTimeMillis();
		do {
			if (stream.available() > 0)
				return System.currentTimeMillis() - startTime;
			try {
				Thread.sleep(poll);
			} catch (InterruptedException e) {
				throw new IOException("Wait for IO interrupted", e);
			}
		} while (timeout > (System.currentTimeMillis() - startTime));
		throw new IOException("Wait for IO timed out after: " + (System.currentTimeMillis() - startTime) + "ms");
	}
}
