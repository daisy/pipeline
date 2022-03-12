package org.daisy.pipeline.tts.acapela.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.LoadBalancer;
import org.daisy.pipeline.tts.LoadBalancer.Host;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.acapela.impl.NscubeLibrary.PNSC_FNSPEECH_DATA;
import org.daisy.pipeline.tts.acapela.impl.NscubeLibrary.PNSC_FNSPEECH_EVENT;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * AcapelaTTS requires libnscube.so to be available in the library path.
 * 
 * Warning: The SSML tags interpreter works only for some languages: • French
 * (fr) • British English (uk) • American English (us) • Spanish (es) • Italian
 * (it) • Brazilian (br) • German (de). For other languages, we can adapt the
 * SSML to the tag language (/mark{...}, /voice{...}) etc.
 */
public class AcapelaEngine extends TTSEngine {

	//private Logger mLogger = LoggerFactory.getLogger(AcapelaEngine.class);

	private static final URL ssmlTransformer = URLs.getResourceFromJAR("/transform-ssml.xsl", AcapelaEngine.class);

	private final AudioFormat mAudioFormat;
	private final LoadBalancer mLoadBalancer;
	private final int mMsPerWord;
	private final int mReserved;
	private final int mPriority;

	public AcapelaEngine(AcapelaService provider, AudioFormat format,
	        LoadBalancer loadBalancer, int speed, int reserved, int priority)
	        throws SynthesisException {
		super(provider);
		mPriority = priority;
		mAudioFormat = format;
		mMsPerWord = speed;
		mReserved = reserved;
		mLoadBalancer = loadBalancer;
	}

	private static class ThreadResources extends TTSResource {
		Pointer dispatcher;
		NativeLong channelId;
		PointerByReference channelLock;
		Pointer server;
		OutputStream audio;
		List<Integer> marks;
		NSC_EXEC_DATA execData;
		IOException error;

		public ThreadResources() {
			execData = new NSC_EXEC_DATA();
			execData.ulEventFilter = NscubeLibrary.NSC_EVTBIT_TEXT
			        | NscubeLibrary.NSC_EVTBIT_BOOKMARK;
			execData.bEventSynchroReq = 1;
			execData.vsSoundData.uiSize = 0;
			execData.vsSoundData.pSoundBuffer = null;

			execData.pfnSpeechData = new PNSC_FNSPEECH_DATA() {
				@Override
				public int apply(Pointer pData, int cbDataSize, NSC_SOUND_DATA pSoundData,
				        Pointer pAppInstanceData) {
					if (error == null) {
						byte[] chunk = new byte[cbDataSize];
						pData.read(0, chunk, 0, cbDataSize);
						try {
							audio.write(chunk);
						} catch (IOException e) {
							error = e;
						}
					}
					return cbDataSize;
				}
			};
			execData.pfnSpeechEvent = new PNSC_FNSPEECH_EVENT() {
				@Override
				public int apply(int nEventID, int cbEventDataSize, NSC_EVENT_DATA pEventData,
				        Pointer pAppInstanceData) {
					
					if (nEventID == NscubeLibrary.NSC_EVID_ENUM.NSC_EVID_BOOKMARK) {
						NSC_EVENT_DATA_Bookmark bookmark = new NSC_EVENT_DATA_Bookmark(
						        pEventData.getPointer());
						bookmark.read();
						marks.add(bookmark.uiByteCount);
					} else if (nEventID == NscubeLibrary.NSC_EVID_ENUM.NSC_EVID_BOOKMARK_EXT) {
						// In regular cases, this should not happen because the marks are numeric.
						// It is only used for running the tests for which the SSML serialization
						// is not enable.
						NSC_EVENT_DATA_BookmarkExt bookmark = new NSC_EVENT_DATA_BookmarkExt(
						        pEventData.getPointer());
						bookmark.read();
						marks.add(bookmark.uiByteCount);
					}

					return 0;
				}
			};
		};
	}

	String findWorkingVoice(Host h) throws SynthesisException {
		if (h == null)
			h = mLoadBalancer.getMaster();

		String workingVoice = null;

		NscubeLibrary lib = NscubeLibrary.INSTANCE;
		Pointer server = createServerContext(h);

		PointerByReference phDispatch = new PointerByReference();
		int ret = lib.nscCreateDispatcher(phDispatch);
		if (ret != NscubeLibrary.NSC_OK) {
			lib.nscReleaseServerContext(server);
			throw new SynthesisException(
			        "Could not create one Acapela's dispatcher (err code: " + ret + ")");
		}
		Pointer dispatcher = phDispatch.getValue();

		PointerByReference voiceEnumerator = new PointerByReference();
		NSC_FINDVOICE_DATA voiceData = new NSC_FINDVOICE_DATA();
		ret = lib.nscFindFirstVoice(server, (String) null, (int) mAudioFormat.getSampleRate(),
		        0, 0, voiceData, voiceEnumerator);
		while (ret == NscubeLibrary.NSC_OK) {
			if (voiceData.nInitialCoding == NscubeLibrary.NSC_VOICE_ENCODING_PCM) {
				String voiceName = nullTerminatedString(voiceData.cVoiceName);
				NativeLongByReference pChId = new NativeLongByReference();
				ret = lib.nscInitChannel(server, voiceName,
				        (int) mAudioFormat.getSampleRate(), 0, dispatcher, pChId);
				if (ret == NscubeLibrary.NSC_OK) {
					lib.nscCloseChannel(server, pChId.getValue());
					workingVoice = voiceName;
					break;
				}

			}
			ret = lib.nscFindNextVoice(voiceEnumerator.getValue(), voiceData);
		}

		lib.nscCloseFindVoice(voiceEnumerator.getValue());
		lib.nscDeleteDispatcher(dispatcher);
		lib.nscReleaseServerContext(server);

		return workingVoice;
	}

	@Override
	public TTSResource allocateThreadResources() throws SynthesisException,
	        InterruptedException {
		return allocateThreadResources(mLoadBalancer.selectHost());
	}

	ThreadResources allocateThreadResources(Host h) throws SynthesisException,
	        InterruptedException {

		NscubeLibrary lib = NscubeLibrary.INSTANCE;
		ThreadResources th = new ThreadResources();

		// Acapela's doc says:
		//  "It is possible to create multiple different ServerContext to handle communication with a
		//  single server or multiple servers at the same time."
		// And:
		//   "For a single server context object (created by nscCreateServerContext) the
		//   whole search sequence nscFindFirstVoice, nscFindNextVoice must not be called
		//   from different threads at the same time."

		th.server = createServerContext(h);

		// Acapela's doc says:
		//  "In synchronous mode (channels launched with the nscExecChannel() function), one
		//  dispatcher must be created for each thread because the nscExecChannel() function
		//  internally makes a call to the nscGetandProcess() function."
		// And:
		//  "In any case the application must not use a single dispatcher object (created by
		//   nscCreateDispatcher) from different threads, neither in direct calls (nscProcessEvent,
		//	nscGetandProcess functions) nor in indirect calls (nscExecChannel function)."

		PointerByReference phDispatch = new PointerByReference();
		int ret = lib.nscCreateDispatcher(phDispatch);
		if (ret != NscubeLibrary.NSC_OK) {
			releaseThreadResources(th);
			throw new SynthesisException(
			        "Could not create one Acapela's dispatcher (err code: " + ret + ")");
		}
		th.dispatcher = phDispatch.getValue();

		// Acapela's doc says:
		//   "The capacity of specifying a list of voices for pVoiceList argument is now deprecated and
		//   kept only for compatibility (this functionality will disappear in next version).
		//   To switch to an another language/voice, you can use directly switch tag ( \vox, \voice or
		//   \vce) without the need to load a list of voice."
		// But it seems that a channel must be initialized with a least one valid voice, 
		// i.e. empty string and null string are not accepted.
		// It proceeds to say: " If too many threads attempts to run nscInitChannel at the same time, there may be a
		// limitation from the TCP/IP driver. In that case, the function returns the code
		// NSC_ERR_CONNECT. It is up to the application to retry until the function succeeds."

		NativeLongByReference pChId = new NativeLongByReference();
		PointerByReference voiceEnumerator = new PointerByReference();
		NSC_FINDVOICE_DATA voiceData = new NSC_FINDVOICE_DATA();
		ret = lib.nscFindFirstVoice(th.server, (String) null, (int) mAudioFormat
		        .getSampleRate(), 0, 0, voiceData, voiceEnumerator);
		while (ret == NscubeLibrary.NSC_OK) {
			if (voiceData.nInitialCoding == NscubeLibrary.NSC_VOICE_ENCODING_PCM) {
				String voiceName = nullTerminatedString(voiceData.cVoiceName);
				ret = lib.nscInitChannel(th.server, voiceName, (int) mAudioFormat
				        .getSampleRate(), 0, th.dispatcher, pChId);
				if (ret == NscubeLibrary.NSC_OK) {
					break;
				}

			}
			ret = lib.nscFindNextVoice(voiceEnumerator.getValue(), voiceData);
		}

		if (ret != NscubeLibrary.NSC_OK) {
			throw new SynthesisException("Could not init Acapela's channel (err code: " + ret
			        + ")");
		}

		th.channelId = pChId.getValue();

		return th;
	}

	private Pointer createServerContext(Host h) throws SynthesisException {
		PointerByReference phandler = new PointerByReference();

		int cmdPort = h.port;
		int dataPort = cmdPort == 0 ? 0 : (cmdPort + 1);

		int ret = NscubeLibrary.INSTANCE.nscCreateServerContextEx(
		        NscubeLibrary.NSC_AFTYPE_ENUM.NSC_AF_INET, cmdPort, dataPort, h.address,
		        phandler);
		if (ret != NscubeLibrary.NSC_OK) {
			throw new SynthesisException("could not connect to the Acapela Server (err code: "
			        + ret + ")");
		}
		return phandler.getValue();
	}

	@Override
	public void releaseThreadResources(TTSResource resources) throws SynthesisException {
		ThreadResources th = (ThreadResources) resources;
		if (th.channelId != null && th.server != null)
			NscubeLibrary.INSTANCE.nscCloseChannel(th.server, th.channelId);
		if (th.dispatcher != null)
			NscubeLibrary.INSTANCE.nscDeleteDispatcher(th.dispatcher);
		if (th.server != null) {
			NscubeLibrary.INSTANCE.nscReleaseServerContext(th.server);
		}
	}

	@Override
	public SynthesisResult synthesize(XdmNode ssml, Voice voice,
	        TTSResource threadResources) throws SynthesisException,
	        InterruptedException {

		ThreadResources th = (ThreadResources) threadResources;

		//note: the Acapela's markup for SSML interpretation is active by default.
		Map<String,Object> xsltParams = new HashMap<>(); {
			xsltParams.put("voice", voice.name);
			// add ending mark to ensure the complete SSML is processed
			xsltParams.put("ending-mark", "ending-mark");
		}
		try {
			List<Integer> marks = new ArrayList<>();
			AudioInputStream audio
				= speak(transformSsmlNodeToString(ssml, ssmlTransformer, xsltParams), th, marks);
			// remove ending mark
			marks.subList(marks.size() - 1, marks.size()).clear();
			return new SynthesisResult(audio, marks);
		} catch (IOException | SaxonApiException e) {
			throw new SynthesisException(e);
		}
	}

	AudioInputStream speak(String ssml, TTSResource tr, List<Integer> marks)
			throws SynthesisException, IOException {

		ThreadResources th = (ThreadResources) tr;
		ByteArrayOutputStream audio = new ByteArrayOutputStream();
		th.audio = audio;
		th.marks = marks;
		th.channelLock = new PointerByReference();
		th.error = null;
		NscubeLibrary lib = NscubeLibrary.INSTANCE;

		int ret = lib.nscLockChannel(th.server, th.channelId, th.dispatcher, th.channelLock);
		if (ret != NscubeLibrary.NSC_OK)
			throw new SynthesisException("nscLockChannel returned error code: " + ret);

		ret = lib.nscAddTextUTF8(th.channelLock.getValue(), ssml, null);
		if (ret != NscubeLibrary.NSC_OK) {
			lib.nscUnlockChannel(th.channelLock.getValue());
			throw new SynthesisException("nscAddTextUTF8 returned error code: " + ret);
		}

		ret = lib.nscExecChannel(th.channelLock.getValue(), th.execData);
		lib.nscUnlockChannel(th.channelLock.getValue());
		if (ret != NscubeLibrary.NSC_OK) {
			throw new SynthesisException("nscExecChannel returned error code: " + ret);
		}

		if (th.error != null) {
			throw th.error;
		}

		return createAudioStream(mAudioFormat, audio.toByteArray());
	}

	@Override
	public void interruptCurrentWork(TTSResource resource) {
		ThreadResources th = (ThreadResources) resource;
		NscubeLibrary.INSTANCE.nscExitChannel(th.channelLock.getValue());
	}

	@Override
	public Collection<Voice> getAvailableVoices() throws SynthesisException,
	        InterruptedException {
		NscubeLibrary lib = NscubeLibrary.INSTANCE;
		Pointer server = createServerContext(mLoadBalancer.getMaster());

		Set<Voice> result = new HashSet<Voice>();

		/*
		 * Only the voices with the pre-selected sample rate are kept to prevent
		 * the server from producing data using another sample rate without any
		 * notification. If there are two versions with the same speaker (for
		 * example 8kHz and 22kHz) then the server should choose the voice whose
		 * sample rate matches with the one provided to the channel
		 * initialization. Such a situation has not been tested though. The
		 * returned voices are not thoroughly tested one by one. Further
		 * improvements should check that they are at least suitable for
		 * initializing channels.
		 */
		PointerByReference voiceEnumerator = new PointerByReference();
		NSC_FINDVOICE_DATA voiceData = new NSC_FINDVOICE_DATA();
		int ret = lib.nscFindFirstVoice(server, (String) null, (int) mAudioFormat
		        .getSampleRate(), 0, 0, voiceData, voiceEnumerator);
		while (ret == NscubeLibrary.NSC_OK) {
			if (voiceData.nInitialCoding == NscubeLibrary.NSC_VOICE_ENCODING_PCM) {
				result.add(new Voice(getProvider().getName(),
				        nullTerminatedString(voiceData.cSpeakerName)));
			}
			ret = lib.nscFindNextVoice(voiceEnumerator.getValue(), voiceData);
		}

		lib.nscCloseFindVoice(voiceEnumerator.getValue());
		lib.nscReleaseServerContext(server);

		return result;
	}

	/**
	 * @param str is supposed to be ascii-encoded
	 */
	private static String nullTerminatedString(byte[] str) {
		int end = 0;
		for (; end < str.length && str[end] != 0; ++end);
		return new String(str, 0, end);
	}

	@Override
	public int getOverallPriority() {
		return mPriority;
	}

	@Override
	public int reservedThreadNum() {
		//there is no point to use a lot of threads since the server's total output rate
		//is constant: it will be divided by the number of channels (one thread per channel).
		return mReserved;
	}

	@Override
	public int expectedMillisecPerWord() {
		return mMsPerWord;
	}

	@Override
	public boolean handlesMarks() {
		return true;
	}
}
