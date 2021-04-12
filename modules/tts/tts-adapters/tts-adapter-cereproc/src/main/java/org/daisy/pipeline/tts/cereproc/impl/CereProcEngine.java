package org.daisy.pipeline.tts.cereproc.impl;


import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.common.shell.CommandRunner;
import org.daisy.common.xslt.CompiledStylesheet;
import org.daisy.common.xslt.XslTransformCompiler;
import org.daisy.pipeline.audio.AudioBuffer;
import org.daisy.pipeline.tts.AudioBufferAllocator;
import org.daisy.pipeline.tts.AudioBufferAllocator.MemoryException;
import org.daisy.pipeline.tts.MarklessTTSEngine;
import org.daisy.pipeline.tts.SoundUtil;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.cereproc.impl.util.CereprocTTSUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;

public class CereProcEngine extends MarklessTTSEngine {

	private final static Logger logger = LoggerFactory.getLogger(CereProcEngine.class);
	private static File tmpDirectory = null;

	private AudioFormat audioFormat;
	private final int priority;
	private Map<String, CereprocTTSUtil> mTtsUtils;
	private final String[] cmd;
	private final int expectedMillisecPerWord;
	private Map<Configuration, CompiledStylesheet> mStyleSheets;

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

		this.mTtsUtils = new HashMap<>();
		this.mTtsUtils.put("sv", new CereprocTTSUtil(Optional.of(new Locale("sv"))));
		this.mTtsUtils.put("en", new CereprocTTSUtil(Optional.of(new Locale("en"))));

		this.mStyleSheets = new HashMap<>();
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
			String filteredSentence = this.transformSSML(xmlSentence, voice);
			try (OutputStream os = new FileOutputStream(txtFile)) {
				Writer w = new OutputStreamWriter(os, UTF_8);
			 	w.write(filteredSentence.replace('\n', ' '));
				w.write("\n");
				logger.warn("TEXTALK (SENTENCE): " + sentence);
				logger.warn("TEXTALK (FILTERED): " + filteredSentence);

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

	/**
	 * Function to transform ssml to String and also perform Regex rules and char substitutions.
	 *
	 * @param
	 * @return
	 * @throws SaxonApiException
	 */
		public String transformSSML(XdmNode ssmlIn, Voice v) throws SynthesisException, SaxonApiException, XMLStreamException {

		    List<XdmItem> ssmlProcessed = new ArrayList<>();

			Configuration conf = ssmlIn.getUnderlyingNode().getConfiguration();

			XMLStreamReader reader = new SaxonInputValue(ssmlIn, conf).asXMLStreamReader();
			XMLStreamWriter writer = new SaxonOutputValue(item -> {
				if (item instanceof XdmNode) {
					ssmlProcessed.add(item);
				} else {
					throw new RuntimeException(); // should not happen
				}
			}, conf).asXMLStreamWriter();

			performSubstitutionRules(reader, writer, v.getLocale().get().getLanguage());

			if ( ssmlProcessed.size() != 1) {
				throw new RuntimeException("Something went wrong");
			}
			if (!(ssmlProcessed.get(0) instanceof XdmNode)) {
				throw new RuntimeException("Incorrect type");
			}

			XdmNode ssmlOut = (XdmNode) ssmlProcessed.get(0);

			CompiledStylesheet styleSheet = this.getStyleSheetFromConfig(conf);
			return styleSheet.newTransformer().transformToString(ssmlOut);
		}

	/**
	 * Cache stylesheets so they dont need to be compiled every time.
	 * @param conf
	 * @return
	 * @throws SynthesisException
	 */
	private CompiledStylesheet getStyleSheetFromConfig(Configuration conf) throws SynthesisException {
		XslTransformCompiler xslCompiler = new XslTransformCompiler(conf);
		CompiledStylesheet styleSheet;
		styleSheet = this.mStyleSheets.get(conf);
		if (styleSheet == null) {
			try {
				styleSheet = xslCompiler.compileStylesheet(CereProcEngine.class.getResourceAsStream("/transform-ssml.xsl"));
				mStyleSheets.put(conf, styleSheet);
			} catch (SaxonApiException e) {
				logger.error(e.toString());
				throw new SynthesisException(e);
			}
		}
		return styleSheet;
	}

	private void performSubstitutionRules(XMLStreamReader reader, XMLStreamWriter writer, String lang) throws XMLStreamException {
		int i ,len;
		while(reader.hasNext()) {
			reader.next();
			int et = reader.getEventType();
			switch (et) {
				case XMLEvent.START_ELEMENT:
					final String localName = reader.getLocalName();
					final String namespaceURI = reader.getNamespaceURI();
					if (namespaceURI != null && namespaceURI.length() > 0) {
						final String prefix = reader.getPrefix();
						if (prefix != null)
							writer.writeStartElement(prefix, localName, namespaceURI);
						else
							writer.writeStartElement(namespaceURI, localName);
					} else {
						writer.writeStartElement(localName);
					}

					for ( i = 0, len = reader.getNamespaceCount(); i < len; i++) {
						writer.writeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
					}

					for ( i = 0, len = reader.getAttributeCount(); i < len; i++) {
						String attUri = reader.getAttributeNamespace(i);
						if (attUri != null)
							writer.writeAttribute(attUri, reader.getAttributeLocalName(i), reader.getAttributeValue(i));
						else
							writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
					}
					break;
				case XMLEvent.END_ELEMENT:
					writer.writeEndElement();
					break;
				case XMLEvent.SPACE:
				case XMLEvent.CHARACTERS:
					CereprocTTSUtil utils = this.mTtsUtils.get(lang);
					writer.writeCharacters(utils.applyAll(reader.getText()));
					break;
				case XMLEvent.PROCESSING_INSTRUCTION:
					writer.writeProcessingInstruction(reader.getPITarget(), reader.getPIData());
					break;
				case XMLEvent.CDATA:
					writer.writeCData(reader.getText());
					break;
				case XMLEvent.COMMENT:
					writer.writeComment(reader.getText());
					break;
				case XMLEvent.ENTITY_REFERENCE:
					writer.writeEntityRef(reader.getLocalName());
					break;
				case XMLEvent.START_DOCUMENT:
					String encoding = reader.getCharacterEncodingScheme();
					String version = reader.getVersion();

					if (encoding != null && version != null)
						writer.writeStartDocument(encoding, version);
					else if (version != null)
						writer.writeStartDocument(reader.getVersion());
					break;
				case XMLEvent.END_DOCUMENT:
					writer.writeEndDocument();
					break;
				case XMLEvent.DTD:
					writer.writeDTD(reader.getText());
					break;
			}
		}
	}
}
