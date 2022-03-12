package org.daisy.pipeline.tts.calabash.impl;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;

import javax.sound.sampled.AudioFileFormat;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.pipeline.audio.AudioFileTypes;
import org.daisy.pipeline.audio.AudioServices;
import org.daisy.pipeline.tts.AudioFootprintMonitor;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.calabash.impl.EncodingThread.EncodingException;
import org.daisy.pipeline.tts.calabash.impl.TTSLog.ErrorCode;

import com.google.common.collect.Iterables;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SynthesizeStep extends DefaultStep implements FormatSpecifications, XProcStep {

	private static final Logger logger = LoggerFactory.getLogger(SynthesizeStep.class);

	private static QName ENCODING_ERROR = new QName("TTS01");

	/*
	 * The maximum number of sentences that a section (ContiguousText) can contain.
	 */
	private static int MAX_SENTENCES_PER_SECTION = 100;
	
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
	private int mSentenceCounter = 0;
	private int mErrorCounter = 0;

	private static String convertSecondToString(double seconds) {
		int iseconds = (int) (Math.floor(seconds));
		int milliseconds = (int) (Math.floor(1000 * (seconds - iseconds)));
		return String.format("%d:%02d:%02d.%03d", iseconds / 3600, (iseconds / 60) % 60,
		        (iseconds % 60), milliseconds);
	}

	public static XdmNode getFirstChild(XdmNode node) {
		XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
		if (iter.hasNext()) {
			return (XdmNode) iter.next();
		} else {
			return null;
		}
	}

	public SynthesizeStep(XProcRuntime runtime, XAtomicStep step, TTSRegistry ttsRegistry,
	        AudioServices audioServices, Semaphore startSemaphore,
	        AudioFootprintMonitor audioFootprintMonitor) {
		super(runtime, step);
		mStartSemaphore = startSemaphore;
		mAudioFootprintMonitor = audioFootprintMonitor;
		mAudioServices = audioServices;
		mRuntime = runtime;
		mTTSRegistry = ttsRegistry;
		mRandGenerator = new Random();
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

	public void traverse(XdmNode node, SSMLtoAudio pool) throws SynthesisException {
		if (SentenceTag.equals(node.getNodeName())) {
			if (!pool.dispatchSSML(node))
				mErrorCounter++;
			if (++mSentenceCounter % MAX_SENTENCES_PER_SECTION == 0)
				pool.endSection();
		} else {
			XdmSequenceIterator iter = node.axisIterator(Axis.CHILD);
			while (iter.hasNext()) {
				traverse((XdmNode) iter.next(), pool);
			}
		}
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
		ConfigReader cr = new ConfigReader(mRuntime.getProcessor(), config.read(), configExt);

		boolean logEnabled = mIncludeLogOpt;
		if (!logEnabled) {
			String logEnabledProp = cr.getDynamicProperties().get("org.daisy.pipeline.tts.log");
			if (logEnabledProp == null)
				logEnabledProp = cr.getStaticProperties().get("org.daisy.pipeline.tts.log");
			logEnabled = "true".equalsIgnoreCase(logEnabledProp);
		}
		TTSLog log;
		if (logEnabled) {
			log = new TTSLogImpl();
		} else
			log = new TTSLogEmpty();
		File audioOutputDir; {
			if (mTempDirOpt != null && !mTempDirOpt.isEmpty()) {
				try {
					audioOutputDir = new File(new URI(mTempDirOpt));
				} catch (URISyntaxException e) {
					throw new RuntimeException("temp-dir option invalid: " + mTempDirOpt);
				}
				if (audioOutputDir.exists())
					throw new RuntimeException("temp-dir option must be a non-existing directory: "+mTempDirOpt);
			} else {
				String tmpDir = cr.getAllProperties().get("org.daisy.pipeline.tts.audio.tmpdir");
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

		SSMLtoAudio ssmltoaudio = new SSMLtoAudio(audioOutputDir, mAudioFileType, mTTSRegistry, logger,
		        mAudioFootprintMonitor, mRuntime.getProcessor(), configExt, log);

		Iterable<SoundFileLink> soundFragments = Collections.EMPTY_LIST;
		mErrorCounter = 0;
		mSentenceCounter = 0;
		try {
			while (source.moreDocuments()) {
				traverse(getFirstChild(source.read()), ssmltoaudio);
				ssmltoaudio.endSection();
			}
			Iterable<SoundFileLink> newfrags = ssmltoaudio.blockingRun(mAudioServices);
			mErrorCounter += ssmltoaudio.getErrorCount();
			soundFragments = Iterables.concat(soundFragments, newfrags);
		} catch (SynthesisException e) {
			logger.error("Synthesis failed", e);
			return;
		} catch (EncodingException e) {
			throw new XProcException(ENCODING_ERROR,
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
		for (SoundFileLink sf : soundFragments) {
			String soundFileURI = sf.soundFileURIHolder.toString();
			if (!soundFileURI.isEmpty()) {
				if (sf.clipBegin < sf.clipEnd){
					//sf.clipBegin = sf.clipEnd if the input text is empty. Those clips are not useful
					//and they can eventually lead to validation errors
					tw.addStartElement(ClipTag);
					tw.addAttribute(Audio_attr_id, sf.xmlid);
					tw.addAttribute(Audio_attr_clipBegin, convertSecondToString(sf.clipBegin));
					tw.addAttribute(Audio_attr_clipEnd, convertSecondToString(sf.clipEnd));
					tw.addAttribute(Audio_attr_src, soundFileURI);
					tw.addEndElement();
				}
				++num;
				TTSLog.Entry entry = log.getOrCreateEntry(sf.xmlid);
				entry.setSoundfile(soundFileURI);
				entry.setPositionInFile(sf.clipBegin, sf.clipEnd);
			} else {
				log.getOrCreateEntry(sf.xmlid).addError(
				        new TTSLog.Error(ErrorCode.AUDIO_MISSING,
				                "not synthesized or not encoded"));
			}
		}
		tw.addEndElement();
		tw.endDocument();
		result.write(tw.getResult());

		logger.info("Number of synthesized sound fragments: " + num);
		logger.debug("audio encoding unreleased bytes : "
		        + mAudioFootprintMonitor.getUnreleasedEncondingMem());
		logger.debug("TTS unreleased bytes: " + mAudioFootprintMonitor.getUnreleasedTTSMem());

		/*
		 * Write status document
		 */

		tw = new TreeWriter(runtime);
		tw.startDocument(runtime.getStaticBaseURI());
		tw.addStartElement(StatusRootTag);
		if (mErrorCounter == 0)
			tw.addAttribute(Status_attr_result, "ok");
		else {
			tw.addAttribute(Status_attr_result, "error");
			tw.addAttribute(Status_attr_success_rate,
			                (int)Math.floor(100 * (1 - (double)mErrorCounter/mSentenceCounter)) + "%");
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
				if (le.getSoundFile() != null) {
					String basename = new File(le.getSoundFile()).getName();
					xmlLog.addAttribute(Log_attr_file, basename);
					xmlLog.addAttribute(Log_attr_begin, String.valueOf(le.getBeginInFile()));
					xmlLog.addAttribute(Log_attr_end, String.valueOf(le.getEndInFile()));
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
		tw.addText(err.getMessage());
		tw.addEndElement();
	}
}
