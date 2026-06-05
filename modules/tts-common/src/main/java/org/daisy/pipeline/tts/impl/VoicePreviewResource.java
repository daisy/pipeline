package org.daisy.pipeline.tts.impl;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.regex.Pattern;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioFileTypes;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.Sentence;
import org.daisy.pipeline.tts.TimedTTSExecutor;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSLog;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.TTSTimeout.ThreadFreeInterrupter;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceManager;
import org.daisy.pipeline.webservice.restlet.AuthenticatedResource;

import org.restlet.data.Disposition;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.xml.sax.InputSource;

public class VoicePreviewResource extends AuthenticatedResource {

	private static final Pattern SPEECH_RATE_REGEX = Pattern.compile("(x-)?(slow|fast)|medium|default|[0-9]+(\\.[0-9]+)?%?");

	private static final Logger logger = LoggerFactory.getLogger(VoiceResource.class.getName());
	private static final DocumentBuilder xmlParser = new Processor(false).newDocumentBuilder();
	private static final AudioFileFormat.Type audioFileType = AudioFileTypes.WAVE;
	private static final MediaType mediaType = MediaType.AUDIO_WAV;

	private VoicesWebServiceExtension provider;
	private AudioEncoder encoder;
	private String voiceID;
	private String text;
	private String speechRate;

	@SuppressWarnings("unchecked")
	@Override
	public void doInit() {
		super.doInit();
		if (!isAuthenticated()) {
			return;
		}
		provider = (VoicesWebServiceExtension)getContext().getAttributes().get(VoicesWebServiceExtension.CONTEXT_ATTRIBUTE_KEY);
		encoder = provider.getAudioEncoder(audioFileType);
		voiceID = (String)getRequestAttributes().get("id");
		text = getQuery().getFirstValue("text");
		speechRate = getQuery().getFirstValue("speech-rate");
	}

	/**
	 * Gets the resource.
	 *
	 * @return the resource
	 */
	@Get("xml")
	public Representation getResource() {
		logRequest();
		maybeEnableCORS();
		if (!isAuthenticated()) {
			setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
			return null;
		}
		try {
			VoiceManager voiceManager = provider.getRememberedVoiceManager(Properties.getSnapshot());
			if (voiceManager == null) {
				setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
				return getErrorRepresentation("No /voices call preceeded this call, or settings were changed");
			}
			Voice voice = voiceManager.getVoiceForID(voiceID);
			if (voice == null) {
				setStatus(Status.CLIENT_ERROR_NOT_FOUND);
				return getErrorRepresentation("Voice not found");
			}
			TTSEngine tts = voiceManager.getTTS(voice);
			if (text == null)
				text = "Hi, my name is " + voice.getName();
			if (speechRate != null) {
				if (!SPEECH_RATE_REGEX.matcher(speechRate).matches()) {
					setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
					return getErrorRepresentation("Invalid speech-rate parameter specified");
				} else if (!tts.handlesSpeakingRate()) {
					logger.warn("Voice " + voice.getName()  + " does not support changing the speaking rate.");
				} else {
					text = "<prosody rate=\"" + speechRate + "\">"
						+ text.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;")
						+ "</prosody>";
				}
			}
			XdmNode ssml = null; {
				try {
					ssml = xmlParser.build(new SAXSource(new InputSource(new StringReader(
					    "<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\"><s>" + text + "</s></speak>"))));
				} catch (SaxonApiException e) {
					throw new IllegalStateException(e); // should not happen
				}
			}
			if (encoder == null) {
				throw new IllegalStateException("No audio encoder found");
			}
			TTSTimeout timeout = new TTSTimeout();
			TTSResource res; {
				timeout.enableForCurrentThread(2);
				try {
					res = tts.allocateThreadResources();
				} catch (Exception e) {
					throw new Exception("Could not allocate resources: " + e.getMessage(), e);
				} finally {
					timeout.disable();
				}
			}
			TTSLog ttsLog = new TTSLog(logger);
			AudioInputStream pcm; {
				try {
					ThreadFreeInterrupter interrupter = new ThreadFreeInterrupter() {
							@Override
							public void threadFreeInterrupt() {
								ttsLog.addGeneralError(
									TTSLog.ErrorCode.WARNING,
									"Timeout while initializing " + tts.getProvider().getName()
									+ ". Forcing interruption of the current work of " + tts.getProvider().getName() + "...");
								tts.interruptCurrentWork(res);
							}
						};
					pcm = new TimedTTSExecutor().synthesizeWithTimeout(
						timeout, interrupter, null, ssml, Sentence.computeSize(ssml), tts, voice, res
					).audio;
				} finally {
					if (res != null)
						timeout.enableForCurrentThread(2);
					try {
						tts.releaseThreadResources(res);
					} catch (Exception e) {
						ttsLog.addGeneralError(
						    TTSLog.ErrorCode.WARNING,
							"Error while releasing resource of " + tts.getProvider().getName() + ": " + e.getMessage(),
							e);
					} finally {
						timeout.disable();
					}
				}
			}
			timeout.close();
			File tmpFile = File.createTempFile("pipeline-audio-encoder", ".tmp");
			tmpFile.deleteOnExit();
			encoder.encode(pcm, audioFileType, tmpFile);
			long size = tmpFile.length();
			InputStream audio = Files.newInputStream(tmpFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
			Representation rep = new InputRepresentation(audio, mediaType);
			rep.setSize(size);
			Disposition disposition = new Disposition();
			disposition.setFilename(voice.getName() + "." + audioFileType.getExtension());
			disposition.setType(Disposition.TYPE_INLINE);
			disposition.setSize(size);
			rep.setDisposition(disposition);
			setStatus(Status.SUCCESS_OK);
			return rep;
		} catch (Throwable e) {
			setStatus(Status.SERVER_ERROR_INTERNAL);
			return getErrorRepresentation(e);
		}
	}
}
