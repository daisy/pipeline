package org.daisy.pipeline.tts;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.pipeline.tts.TTSEngine.SynthesisResult;
import org.daisy.pipeline.tts.TTSLog.ErrorCode;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.TTSTimeout;
import org.daisy.pipeline.tts.TTSTimeout.ThreadFreeInterrupter;
import org.daisy.pipeline.tts.Voice.MarkSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.xml.sax.InputSource;

@Component(
	name = "tts-registry",
	service = { TTSRegistry.class }
)
public class TTSRegistry {

	public static class TTSResource {
		public boolean invalid = false;
	}

	private static Logger logger = LoggerFactory.getLogger(TTSRegistry.class);
	// for parsing test SSML
	private static DocumentBuilder xmlParser = new Processor(false).newDocumentBuilder();
	private URIResolver mURIResolver; //not used so far
	private List<TTSService> mServices = new CopyOnWriteArrayList<TTSService>(); //List of active services
	//Services and resources used by the current running steps (some of them may not be active anymore):
	private Map<TTSService, List<TTSResource>> mTTSResources = new HashMap<TTSService, List<TTSResource>>();

	/**
	 * Service component callback
	 */
	@Reference(
		name = "uri-resolver",
		unbind = "-",
		service = URIResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setURIResolver(URIResolver uriResolver) {
		mURIResolver = uriResolver;
	}

	/**
	 * Service component callback
	 */
	public void unsetURIResolver(URIResolver uriResolver) {
		mURIResolver = null;
	}

	/**
	 * Service component callback
	 */
	@Reference(
		name = "TTSService",
		unbind = "removeTTS",
		service = TTSService.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.DYNAMIC
	)
	public void addTTS(TTSService tts) {
		logger.info("Adding TTSService " + tts.getName());
		mServices.add(tts);
		synchronized (mTTSResources) {
			mTTSResources.put(tts, new ArrayList<TTSResource>());
		}
	}

	/**
	 * Service component callback
	 */
	public void removeTTS(TTSService tts) {
		logger.info("Removing TTSService " + tts.getName());

		List<TTSResource> resources = null;
		synchronized (mTTSResources) {
			resources = mTTSResources.get(tts);
		}
		if (resources != null) {
			if (resources.size() > 0)
				logger.warn("Stopping bundle of " + tts.getName()
				        + " while a TTS job is running");
			for (TTSResource resource : resources) {
				synchronized (resource) {
					resource.invalid = true;
				}
			}
		}

		mServices.remove(tts);
	}

	public Collection<TTSService> getServices() {
		return mServices;
	}

	/**
	 * Allocate a list of working engines.
	 *
	 * @param properties Key-value pairs for the allocation of engines. See {@link TTSService#newEngine}.
	 * @param ttsLog     For logging engine allocations errors. May be null.
	 * @param log        For logging the engine status summary. May be null.
	 */
	public Collection<TTSEngine> getWorkingEngines(Map<String,String> properties,
	                                               TTSLog ttsLog,
	                                               Logger log) {
		List<TTSEngine> workingEngines = new ArrayList<>();
		TTSTimeout timeout = new TTSTimeout();
		TimedTTSExecutor executor = new TimedTTSExecutor();
		/*
		 * Create a piece of SSML that will be used for testing. Useless
		 * attributes and namespaces are inserted on purpose. A <mark> is
		 * included for engines that support marks. It is inserted somewhere
		 * in the middle of the string because the SAPI adapter ignores
		 * marks that appear at the end.
		 */
		XdmNode testSSMLWithoutMark; {
			String ssml = "<s:speak version=\"1.0\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\">"
				+ "<s:s xmlns:tmp=\"http://\" id=\"s1\"><s:token>small</s:token> sentence</s:s></s:speak>";
			try {
				testSSMLWithoutMark = xmlParser.build(new SAXSource(new InputSource(new StringReader(ssml))));
			} catch (SaxonApiException e) {
				throw new RuntimeException(e); // should not happen
			}
		}
		XdmNode testSSMLWithMark; {
			String ssml = "<s:speak version=\"1.0\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\">"
				+ "<s:s xmlns:tmp=\"http://\" id=\"s1\"><s:token>small</s:token>"
				+ "<s:mark name=\"mark\"></s:mark> sentence</s:s></s:speak>";
			try {
				testSSMLWithMark = xmlParser.build(new SAXSource(new InputSource(new StringReader(ssml))));
			} catch (SaxonApiException e) {
				throw new RuntimeException(e); // should not happen
			}
		}
		List<String> engineStatus = log != null ? new ArrayList<>() : null;
		for (TTSService service : mServices) {
			try {
				TTSEngine engine; {
					timeout.enableForCurrentThread(2);
					try {
						engine = service.newEngine(properties);
					} finally {
						timeout.disable();
					}
				}

				// get a voice supporting SSML marks (so far as they are supported by the engine)
				Voice firstVoice = null;
				int timeoutSecs = 30;
				timeout.enableForCurrentThread(timeoutSecs);
				try {
					for (Voice v : engine.getAvailableVoices()) {
						if (!engine.handlesMarks() || v.getMarkSupport() != MarkSupport.MARK_NOT_SUPPORTED) {
							firstVoice = v;
							break;
						}
					}
					if (firstVoice == null) {
						throw new Exception("no voices available");
					}
				} catch (InterruptedException e) {
					throw new Exception("timeout while retrieving voices (exceeded " + timeoutSecs + " seconds)");
				} catch (Exception e) {
					throw new Exception("failed to retreive voices: " + e.getMessage(), e);
				} finally {
					timeout.disable();
				}

				// allocate resources
				final TTSEngine fengine = engine;
				TTSResource resource = null;
				timeout.enableForCurrentThread(2);
				try {
					resource = engine.allocateThreadResources();
				} catch (Exception e) {
					throw new Exception("could not allocate resources: " + e.getMessage(), e);
				} finally {
					timeout.disable();
				}

				// create a custom interrupter in case the engine hangs
				final TTSResource res = resource;
				TTSTimeout.ThreadFreeInterrupter interrupter = new ThreadFreeInterrupter() {
						@Override
						public void threadFreeInterrupt() {
							ttsLog.addGeneralError(
								ErrorCode.WARNING,
								"Timeout while initializing " + service.getName()
								+ ". Forcing interruption of the current work of " + service.getName() + "...");
							fengine.interruptCurrentWork(res);
						}
					};

				// synthesize
				SynthesisResult result = null;
				try {
					XdmNode ssml = engine.handlesMarks() ? testSSMLWithMark : testSSMLWithoutMark;
					result = executor.synthesizeWithTimeout(
						timeout, interrupter, null, ssml, Sentence.computeSize(ssml),
						engine, firstVoice, res);
				} catch (Exception e) {
					throw new Exception("test failed: " + e.getMessage(), e);
				} finally {
					if (res != null)
						timeout.enableForCurrentThread(2);
					try {
						engine.releaseThreadResources(res);
					} catch (Exception e) {
						ttsLog.addGeneralError(
							ErrorCode.WARNING,
							"Error while releasing resource of " + service.getName() + ": " + e.getMessage(),
							e);
					} finally {
						timeout.disable();
					}
				}

				// check that the output buffer is big enough
				String msg = "";
				if (result.audio.getFrameLength() * result.audio.getFormat().getFrameSize() < 2500) {
					msg = "Audio output is not big enough. ";
				}

				if (engine.handlesMarks()) {
					// check that the result contains a single mark
					String details = " voice: "+ firstVoice;
					if (result.marks.size() != 1) {
						msg += "One bookmark event expected, but received " + result.marks.size() + " events instead. " + details;
					} else {
						int offset = result.marks.get(0);
						if (offset < 2500) {
							msg += "Expecting mark offset to be bigger, got " + offset + " as offset. "+details;
						}
					}
				}
				if (!msg.isEmpty()) {
					throw new Exception("test failed: " + msg);
				}
				workingEngines.add(engine);
				if (log != null)
					engineStatus.add("[x] " + service.getName());
			} catch (Throwable e) {
				// Show the full error with stack trace only in the main and TTS log. A short version is included
				// in the engine status summary. An engine that could not be activated is not an error
				// unless no engines could be activated at all. This is to not confuse users because it
				// is normal that only a part of the engines work.
				String msg = service.getName() + " could not be activated";
				if (ttsLog != null)
					ttsLog.addGeneralError(ErrorCode.WARNING, msg + ": " + e.getMessage(), e);
				if (log != null)
					engineStatus.add("[ ] " + msg);
			}
		}
		timeout.close();
		if (log != null) {
			String summary = "Number of working TTS engine(s): " + workingEngines.size() + "/" + mServices.size();
			if (workingEngines.size() == 0) {
				log.error(summary);
				for (String s : engineStatus)
					log.error(" * " + s);
			} else {
				log.info(summary);
				for (String s : engineStatus)
					log.info(" * " + s);
			}
		}
		return workingEngines;
	}

	public TTSResource allocateResourceFor(TTSEngine tts) throws SynthesisException,
	        InterruptedException {
		List<TTSResource> resources = null;
		synchronized (mTTSResources) {
			resources = mTTSResources.get(tts.getProvider());
		}

		if (resources == null)
			return null; //mTTSResources has been clear because the OSGi component has been stopped

		TTSResource r = tts.allocateThreadResources();
		if (r == null)
			r = new TTSResource();
		resources.add(r);

		return r;
	}
}
