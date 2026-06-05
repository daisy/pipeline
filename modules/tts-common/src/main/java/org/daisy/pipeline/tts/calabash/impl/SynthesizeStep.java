package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.sound.sampled.AudioFileFormat;

import com.google.common.collect.Iterables;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.pipeline.audio.AudioClip;
import org.daisy.pipeline.audio.AudioFileTypes;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.calabash.impl.EncodingThread.EncodingException;
import static org.daisy.pipeline.tts.calabash.impl.SynthesizeProvider.ENABLE_LOG;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.config.DynamicPropertiesExtension;
import org.daisy.pipeline.tts.config.VoiceConfigExtension;
import org.daisy.pipeline.tts.TTSLog;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSLog.ErrorCode;
import org.daisy.pipeline.tts.TTSService.SynthesisException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynthesizeStep extends DefaultStep implements FormatSpecifications, XProcStep {

	private static final Logger logger = LoggerFactory.getLogger(SynthesizeStep.class);
	private static final Property AUDIO_TMPDIR = Properties.getProperty("org.daisy.pipeline.tts.audio.tmpdir",
	                                                                    false,
	                                                                    "Temporary directory used during speech synthesis",
	                                                                    false,
	                                                                    null);
	private static final Property SENTENCES_PER_FILE = Properties.getProperty("org.daisy.pipeline.tts.sentences.per.file",
	                                                                          true,
	                                                                          "Maximum number of sentences per audio file",
	                                                                          false,
	                                                                          null);

	/**
	 * Encoding error
	 */
	private static QName ERR_TTS_001 = new QName("pe", "http://www.daisy.org/ns/pipeline/errors", "TTS001");

	private ReadablePipe source = null;
	private ReadablePipe config = null;
	private WritablePipe result = null;
	private WritablePipe status = null;
	private WritablePipe logOutput = null;
	private XProcRuntime mRuntime;
	private TTSRegistry mTTSRegistry;
	private Random mRandGenerator;
	private AudioServices mAudioServices;
	private Semaphore mStartSemaphore;
	private AudioFootprintMonitor mAudioFootprintMonitor;
	private String mTempDirOpt;
	private boolean mIncludeLogOpt;
	private AudioFileFormat.Type mAudioFileType;
	private final Map<String,String> properties;

	private static String convertDurationToString(Duration duration) {
		long hours = duration.toHours();
		long minutes = duration.toMinutes() % 60;
		long seconds = duration.getSeconds() % 60;
		long milliseconds = duration.getNano() / 1000000;
		return String.format("%d:%02d:%02d.%03d",
		                     hours,
		                     minutes,
		                     seconds,
		                     milliseconds);
	}

	public SynthesizeStep(XProcRuntime runtime, XAtomicStep step, TTSRegistry ttsRegistry,
	                      AudioServices audioServices, Semaphore startSemaphore,
	                      AudioFootprintMonitor audioFootprintMonitor, Map<String,String> properties) {
		super(runtime, step);
		mStartSemaphore = startSemaphore;
		mAudioFootprintMonitor = audioFootprintMonitor;
		mAudioServices = audioServices;
		mRuntime = runtime;
		mTTSRegistry = ttsRegistry;
		mRandGenerator = new Random();
		this.properties = properties;
	}

	public void setInput(String port, ReadablePipe pipe) {
		if ("source".equals(port)) {
			source = pipe;
		} else if ("config".equals(port)) {
			config = pipe;
		}
	}

	public void setOutput(String port, WritablePipe pipe) {
		if ("result".equals(port)) {
			result = pipe;
		} else if ("status".equals(port)) {
			status = pipe;
		} else if ("log".equals(port)) {
			logOutput = pipe;
		}
	}

	@Override
	public void setOption(QName name, RuntimeValue value) {
		if ("temp-dir".equals(name.getLocalName())) {
			mTempDirOpt = value.getString();
		} else if ("include-log".equals(name.getLocalName())) {
			mIncludeLogOpt = value.getBoolean();
		} else if ("audio-file-type".equals(name.getLocalName())) {
			mAudioFileType = AudioFileTypes.fromMediaType(value.getString());
			if (mAudioFileType == null) {
				logger.warn("Audio file type not recognized or not supported: " + value.getString()
				            + ". Falling back to MP3.");
				mAudioFileType = AudioFileTypes.MP3;
			}
		} else
			super.setOption(name, value);
	}

	public void reset() {
		source.resetReader();
		config.resetReader();
		result.resetWriter();
		status.resetWriter();
		logOutput.resetWriter();
	}

	public void run() throws SaxonApiException {
		super.run();

		try {
			mStartSemaphore.acquire();
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
			return;
		}

		VoiceConfigExtension configExt = new VoiceConfigExtension();
		DynamicPropertiesExtension propsExt = new DynamicPropertiesExtension();
		new ConfigReader(mRuntime.getProcessor(), config.read(), this.properties, configExt, propsExt);
		Map<String,String> properties = this.properties;
		Map<String,String> dynProperties = propsExt.getDynamicProperties();
		if (dynProperties != null && !dynProperties.isEmpty()) {
			properties = new HashMap<>(properties);
			properties.putAll(dynProperties);
		}

		boolean logEnabled = mIncludeLogOpt;
		if (!logEnabled) {
			String logEnabledProp = ENABLE_LOG.getValue(properties);
			//if (logEnabledProp != null)
			//	logger.warn("'" + ENABLE_LOG.getName() + "' setting is deprecated. " +
			//	            "It may become unavailable in future version of DAISY Pipeline.");;
			logEnabled = "true".equalsIgnoreCase(logEnabledProp);
		}
		TTSLog log = new TTSLog(logger);
		File audioOutputDir; {
			if (mTempDirOpt != null && !mTempDirOpt.isEmpty()) {
				try {
					audioOutputDir = new File(new URI(mTempDirOpt));
				} catch (URISyntaxException|IllegalArgumentException e) {
					throw new RuntimeException("temp-dir option invalid: " + mTempDirOpt);
				}
				if (audioOutputDir.exists())
					throw new RuntimeException("temp-dir option must be a non-existing directory: "+mTempDirOpt);
			} else {
				String tmpDir = AUDIO_TMPDIR.getValue(properties);
				if (tmpDir == null)
					tmpDir = System.getProperty("java.io.tmpdir");
				do {
					String audioDir = tmpDir + "/";
					for (int k = 0; k < 2; ++k)
						audioDir += Long.toString(mRandGenerator.nextLong(), 32);
					audioOutputDir = new File(audioDir);
				} while (audioOutputDir.exists());
			}
		}
		audioOutputDir.mkdirs();
		audioOutputDir.deleteOnExit();
		int maxSentencesPerSection = 100; {
			String v = SENTENCES_PER_FILE.getValue(properties);
			if (v != null) {
				try {
					maxSentencesPerSection = Integer.valueOf(v);
				} catch (NumberFormatException e) {
					throw new RuntimeException(v + " is not a valid a value for property " + SENTENCES_PER_FILE.getName());
				}
			}
		}

		SSMLtoAudio ssmltoaudio = new SSMLtoAudio(audioOutputDir, mAudioFileType, maxSentencesPerSection,
		        mTTSRegistry, logger, mAudioFootprintMonitor, mRuntime.getProcessor(), properties,
		        configExt, log);

		Iterable<SoundFileLink> soundFragments = Collections.EMPTY_LIST;
		try {
			while (source.moreDocuments()) {
				ssmltoaudio.feedSSML(source.read());
			}
			Iterable<SoundFileLink> newfrags = ssmltoaudio.blockingRun(mAudioServices);
			soundFragments = Iterables.concat(soundFragments, newfrags);
		} catch (SynthesisException e) {
			logger.error("Synthesis failed", e);
			return;
		} catch (EncodingException e) {
			throw new XProcException(ERR_TTS_001,
			                         step,
			                         "Encoding error",
			                         XProcException.fromException(e)
			                                       .rebase(step.getLocation(),
			                                               new RuntimeException().getStackTrace()));
		} catch (InterruptedException e) {
			logger.error("Interrupted", e);
			return;
		} finally {
			mStartSemaphore.release();
		}

		TreeWriter tw = new TreeWriter(runtime);
		tw.startDocument(runtime.getStaticBaseURI());
		tw.addStartElement(OutputRootTag);

		int num = 0;
		for (SoundFileLink link : soundFragments) {
			AudioClip clip = link.getAudioFragment(log);
			if (clip != null) {
				if (!clip.clipEnd.equals(clip.clipBegin)) { // empty clips are not useful and can eventually
				                                            // lead to validation errors
					tw.addStartElement(ClipTag);
					tw.addAttribute(Audio_attr_textref, link.getTextFragment().toString());
					tw.addAttribute(Audio_attr_clipBegin,
					                convertDurationToString(clip.clipBegin));
					tw.addAttribute(Audio_attr_clipEnd,
					                convertDurationToString(clip.clipEnd));
					tw.addAttribute(Audio_attr_src, clip.src.toString());
					tw.addEndElement();
				}
				++num;
				TTSLog.Entry entry = log.getOrCreateEntry(link.getTextFragment().toString());
				entry.setClip(clip);
			} else {
				log.getOrCreateEntry(link.getTextFragment().toString()).addError(
				        new TTSLog.Error(ErrorCode.AUDIO_MISSING,
				                "not synthesized or not encoded"));
			}
		}
		tw.addEndElement();
		tw.endDocument();
		result.write(tw.getResult());

		logger.debug("Number of synthesized sound fragments: " + num);
		logger.debug("audio encoding unreleased bytes : "
		        + mAudioFootprintMonitor.getUnreleasedEncondingMem());
		logger.debug("TTS unreleased bytes: " + mAudioFootprintMonitor.getUnreleasedTTSMem());

		/*
		 * Write status document
		 */

		tw = new TreeWriter(runtime);
		tw.startDocument(runtime.getStaticBaseURI());
		tw.addStartElement(StatusRootTag);
		if (ssmltoaudio.getErrorRate() == 0)
			tw.addAttribute(Status_attr_result, "ok");
		else {
			tw.addAttribute(Status_attr_result, "error");
			tw.addAttribute(Status_attr_success_rate, (int)Math.floor(100 * (1 - ssmltoaudio.getErrorRate())) + "%");
		}
		tw.addEndElement();
		tw.endDocument();
		status.write(tw.getResult());

		/*
		 * Write the log file
		 */
		if (logEnabled) {
			logger.info("Writing TTS log");
			TreeWriter xmlLog = new TreeWriter(runtime);
			xmlLog.startDocument(runtime.getStaticBaseURI());
			xmlLog.addStartElement(LogRootTag);
			xmlLog.startContent();
			for (TTSLog.Error err : log.readonlyGeneralErrors()) {
				writeXMLerror(xmlLog, err);
			}
			for (Map.Entry<String, TTSLog.Entry> entry : log.getEntries()) {
				TTSLog.Entry le = entry.getValue();
				xmlLog.addStartElement(LogTextTag);

				xmlLog.addAttribute(Log_attr_id, entry.getKey());
				if (le.getClip() != null) {
					String basename = new File(le.getClip().src).getName();
					xmlLog.addAttribute(Log_attr_file, basename);
					xmlLog.addAttribute(Log_attr_begin, String.valueOf(le.getClip().clipBegin)); // ISO-8601 seconds based representation
					xmlLog.addAttribute(Log_attr_end, String.valueOf(le.getClip().clipEnd));
				}

				xmlLog.addAttribute(Log_attr_timeout, "" + le.getTimeout() + "s");

				if (le.getSelectedVoice() != null)
					xmlLog.addAttribute(Log_attr_selected_voice, le.getSelectedVoice()
					        .toString());
				if (le.getActualVoice() != null) {
					xmlLog.addAttribute(Log_attr_actual_voice, le.getActualVoice().toString());
					xmlLog.addAttribute(Log_attr_time_elapsed, "" + le.getTimeElapsed() + "s");
				}

				for (TTSLog.Error err : le.getReadOnlyErrors())
					writeXMLerror(xmlLog, err);

				if (le.getSSML() != null) {
					xmlLog.addStartElement(LogSsmlTag);
					xmlLog.addSubtree(le.getSSML());
					xmlLog.addEndElement();
				}else{
					xmlLog.addText("No SSML available. This piece of text has probably been extracted from inside another sentence.");
				}

				xmlLog.addEndElement(); //LogTextTag
			}

			xmlLog.addEndElement(); //root
			xmlLog.endDocument();
			
			logOutput.write(xmlLog.getResult());
		}
	}

	private static void writeXMLerror(TreeWriter tw, TTSLog.Error err) {
		tw.addStartElement(LogErrorTag);
		tw.addAttribute(Log_attr_code, err.getErrorCode().toString());
		String message = err.getMessage();
		if (err.getCause() != null)
			message += ("\nError stack trace: " + getStack(err.getCause()));
		tw.addText(message);
		tw.addEndElement();
	}

	private static String getStack(Throwable t) {
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		t.printStackTrace(printWriter);
		printWriter.flush();
		return writer.toString();
	}
}
