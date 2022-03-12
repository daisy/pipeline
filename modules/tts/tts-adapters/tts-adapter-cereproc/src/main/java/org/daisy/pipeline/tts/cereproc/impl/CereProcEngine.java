package org.daisy.pipeline.tts.cereproc.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import javax.sound.sampled.AudioFormat;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.common.shell.CommandRunner;
import org.daisy.common.stax.XMLStreamWriterHelper;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.Gender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CereProcEngine extends TTSEngine {

	private final static Logger logger = LoggerFactory.getLogger(CereProcEngine.class);
	private final static URL ssmLxslTransformerURL = URLs.getResourceFromJAR("/transform-ssml.xsl", CereProcEngine.class);

	private static File tmpDirectory = null;
	private AudioFormat audioFormat;
	private final int priority;
	private static final Map<String,CereprocTTSUtil> ttsUtils = new HashMap<String,CereprocTTSUtil>() {
		{
			put("sv", new CereprocTTSUtil(Optional.of(new Locale("sv"))));
			put("no", new CereprocTTSUtil(Optional.of(new Locale("no"))));
			put("en", new CereprocTTSUtil(Optional.of(new Locale("en"))));
		}
	};
	private final String[] cmd;
	private final int expectedMillisecPerWord;

	enum Variant {
		STANDARD,
		DNN
	}

	public CereProcEngine(Variant variant,
	                      CereProcService service,
	                      String server,
	                      int port,
	                      File client,
	                      int priority)
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
	public int expectedMillisecPerWord() {
		return expectedMillisecPerWord;
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException, InterruptedException {
		// FIXME: don't hard code
		List<Voice> voices = new ArrayList<>();
		voices.add(new Voice(getProvider().getName(), "William", Locale.forLanguageTag("en-GB"), Gender.MALE_ADULT));
		voices.add(new Voice(getProvider().getName(), "Ylva", Locale.forLanguageTag("sv"), Gender.FEMALE_ADULT));
		voices.add(new Voice(getProvider().getName(), "Hulda", Locale.forLanguageTag("no"), Gender.FEMALE_ADULT));
		voices.add(new Voice(getProvider().getName(), "Clara", Locale.forLanguageTag("no"), Gender.FEMALE_ADULT));
		return voices;
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException, InterruptedException {
		return new TTSResource();
	}

	@Override
	public SynthesisResult synthesize(XdmNode sentence,
	                                  Voice voice,
	                                  TTSResource threadResources)
			throws SynthesisException, InterruptedException {
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
			String filteredSentence = transformSSML(sentence, voice);
			try (OutputStream os = new FileOutputStream(txtFile)) {
				Writer w = new OutputStreamWriter(os, UTF_8);
				w.write(filteredSentence.replace('\n', ' '));
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
				return new SynthesisResult(createAudioStream(audioFormat, bytes.toByteArray()));
			}
		} catch (InterruptedException e) {
			throw e;
		} catch (Throwable e) {
			logger.error(out.toString());
			logger.error(err.toString());
			throw new SynthesisException(e);
		}
	}

	/**
	 * Function to transform SSML to String and also perform regex rules and char substitutions.
	 */
	String transformSSML(XdmNode ssmlIn, Voice v) throws SynthesisException, SaxonApiException, XMLStreamException {
		List<XdmItem> ssmlProcessed = new ArrayList<>();
		Configuration conf = ssmlIn.getUnderlyingNode().getConfiguration();
		XMLStreamReader reader = new SaxonInputValue(ssmlIn).asXMLStreamReader();
		XMLStreamWriter writer = new SaxonOutputValue(item -> {
				if (item instanceof XdmNode) {
					ssmlProcessed.add(item);
				} else {
					throw new RuntimeException(); // should not happen
				}
			}, conf).asXMLStreamWriter();
		performSubstitutionRules(reader, writer, v.getLocale().get().getLanguage());
		if (ssmlProcessed.size() != 1) {
			throw new RuntimeException("Something went wrong");
		}
		if (!(ssmlProcessed.get(0) instanceof XdmNode)) {
			throw new RuntimeException("Incorrect type");
		}
		XdmNode ssmlOut = (XdmNode)ssmlProcessed.get(0);
		Map<String,Object> params = new TreeMap<>(); {
			params.put("voice", v.name);
		}
		try {
			return transformSsmlNodeToString(ssmlOut, ssmLxslTransformerURL, params);
		} catch (SaxonApiException | IOException e) {
			throw new SynthesisException(e);
		}
	}

	private void performSubstitutionRules(XMLStreamReader reader, XMLStreamWriter writer, String lang) throws XMLStreamException {
		while(reader.hasNext()) {
			reader.next();
			if (reader.getEventType() == XMLEvent.CHARACTERS) {
				CereprocTTSUtil utils = ttsUtils.get(lang);
				writer.writeCharacters(utils.applyAll(reader.getText()));
			} else {
				XMLStreamWriterHelper.writeEvent(writer, reader);
			}
		}
	}
}
