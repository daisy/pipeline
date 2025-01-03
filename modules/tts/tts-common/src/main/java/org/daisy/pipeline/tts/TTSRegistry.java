package org.daisy.pipeline.tts;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.transform.URIResolver;
import javax.xml.transform.sax.SAXSource;

import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.tts.TTSEngine.SynthesisResult;
import org.daisy.pipeline.tts.TTSLog.ErrorCode;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.ServiceDisabledException;
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
	private Map<String,TTSServiceWrapper> ttsServices = new ConcurrentHashMap<>();
	//Services and resources used by the current running steps (some of them may not be active anymore):
	private Map<String,List<TTSResource>> ttsResources = new ConcurrentHashMap<>();
	private static XdmNode testSSMLWithMark = null;
	private static XdmNode testSSMLWithoutMark = null;

	/**
	 * Service component callback
	 */
	@Reference(
		name = "TTSService",
		unbind = "-",
		service = TTSService.class,
		cardinality = ReferenceCardinality.MULTIPLE,
		policy = ReferencePolicy.STATIC
	)
	public void addTTS(TTSService tts) {
		String name = tts.getName();
		logger.info("Adding TTSService " + name);
		// wrap in a TTSService that tests the allocated TTSEngine before making it available
		ttsServices.put(name, new TTSServiceWrapper(tts));
		ttsResources.put(name, new ArrayList<TTSResource>());
	}

	/**
	 * List all available TTS services, including the disabled ones (in which case {@link
	 * TTSService#newEngine} will throw a {@link TTSService.ServiceDisabledException}),
	 * and the services that may not allocate a working {@link TTSEngine} for other
	 * reasons (because of missing configuration, because the list of available voices
	 * can not be retrieved, because the engine failed a test, etc.).
	 */
	public Collection<TTSService> getServices() {
		return Collections.unmodifiableCollection(ttsServices.values());
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
		List<String> workingEngineNames = log != null ? new ArrayList<>() : null;
		List<String> disabledEngines = log != null ? new ArrayList<>() : null;
		List<String> enginesWithError = log != null ? new ArrayList<>() : null;
		for (TTSServiceWrapper service : ttsServices.values()) {
			try {
				workingEngines.add(service.newEngine(properties, ttsLog));
				if (log != null)
					workingEngineNames.add(service.getName());
			} catch (ServiceDisabledException e) {
				if (log != null) {
					log.debug(service.getName() + " is disabled", e);
					disabledEngines.add(service.getName());
				}
			} catch (Throwable e) {
				// Show the full error with stack trace only in the main and TTS log. A short version is included
				// in the engine status summary. An engine that could not be activated is not an error
				// unless no engines could be activated at all. This is to not confuse users because it
				// is normal that only a part of the engines work.
				String msg = service.getName() + " could not be activated: " + e.getMessage();
				if (ttsLog != null)
					ttsLog.addGeneralError(ErrorCode.WARNING, msg, e);
				else if (log != null) {
					log.warn(msg + " (Please see detailed log for more info.)");
					log.debug("Error stack trace:", e);
				}
				if (log != null)
					enginesWithError.add(service.getName());
			}
		}
		if (log != null) {
			if (workingEngines.size() == 0) {
				log.error("No available TTS engines");
				if (!enginesWithError.isEmpty())
					log.error("Some engines could not be activated: " + String.join(", ", enginesWithError));
			} else {
				log.info("Available TTS engines: " + String.join(", ", workingEngineNames));
				if (!enginesWithError.isEmpty())
					log.warn("Some engines could not be activated: " + String.join(", ", enginesWithError));
			}
			if (!disabledEngines.isEmpty())
				log.info("Some engines are disabled: " + String.join(", ", disabledEngines));
		}
		return workingEngines;
	}

	public TTSResource allocateResourceFor(TTSEngine tts) throws SynthesisException,
	        InterruptedException {
		List<TTSResource> resources = null;
		synchronized (ttsResources) {
			resources = ttsResources.get(tts.getProvider().getName());
		}
		if (resources == null)
			return null;
		TTSResource r = tts.allocateThreadResources();
		if (r == null)
			r = new TTSResource();
		resources.add(r);
		return r;
	}

	/**
	 * TTSService wrapper that tests the allocated TTSEngine before making it available.
	 */
	private static class TTSServiceWrapper implements TTSService {

		private final TTSService service;
		private final String name;
		private final Property enabled;

		TTSServiceWrapper(TTSService wrap) {
			this.service = wrap;
			this.name = wrap.getName();
			this.enabled = Properties.getProperty("org.daisy.pipeline.tts." + name + ".enabled",
		                                          true,
		                                          "Enable " + wrap.getDisplayName(),
		                                          false,
		                                          "true");
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public String getDisplayName() {
			return service.getDisplayName();
		}

		@Override
		public TTSEngine newEngine(Map<String,String> params) throws Throwable {
			return newEngine(params, null);
		}

		TTSEngine newEngine(Map<String,String> params, TTSLog ttsLog) throws Throwable {
			String str = enabled.getValue(params);
			if (str != null && "false".equals(str.toLowerCase()) || "0".equals(str))
				throw new ServiceDisabledException(
				    "In order to enable the " + name + " service, set property '" + enabled.getName() + "' to 'true'");
			/*
			 * Set timeouts on all the {@link TTSService} and {@link TTSEngine} API calls.
			 */
			TTSTimeout timeout = new TTSTimeout();
			TTSEngine engine; {
				timeout.enableForCurrentThread(2);
				try {
					engine = service.newEngine(params);
				} finally {
					timeout.disable();
				}
			}
			/*
			 * Test the engine using a piece of SSML. Useless attributes and
			 * namespaces are inserted on purpose. A <mark> is included for
			 * engines that support marks. It is inserted somewhere in the middle
			 * of the string because the SAPI adapter ignores marks that appear
			 * at the end.
			 */
			if (engine.handlesMarks()) {
				if (testSSMLWithMark == null) {
					String ssml = "<s:speak version=\"1.0\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\">"
						+ "<s:s xmlns:tmp=\"http://\" id=\"s1\"><s:token>small</s:token>"
						+ "<s:mark name=\"mark\"></s:mark> sentence</s:s></s:speak>";
					try {
						testSSMLWithMark = xmlParser.build(new SAXSource(new InputSource(new StringReader(ssml))));
					} catch (SaxonApiException e) {
						throw new RuntimeException(e); // should not happen
					}
				}
			} else {
				if (testSSMLWithoutMark == null) {
					String ssml = "<s:speak version=\"1.0\" xmlns:s=\"http://www.w3.org/2001/10/synthesis\">"
						+ "<s:s xmlns:tmp=\"http://\" id=\"s1\"><s:token>small</s:token> sentence</s:s></s:speak>";
					try {
						testSSMLWithoutMark = xmlParser.build(new SAXSource(new InputSource(new StringReader(ssml))));
					} catch (SaxonApiException e) {
						throw new RuntimeException(e); // should not happen
					}
				}
			}
			/* Get a voice for the test*/
			Voice firstVoice; {
				int timeoutSecs = 30;
				timeout.enableForCurrentThread(timeoutSecs);
				try {
					firstVoice = null;
					for (Voice v : engine.getAvailableVoices()) {
						if (!engine.handlesMarks() || v.getMarkSupport() != MarkSupport.MARK_NOT_SUPPORTED) {
							firstVoice = v;
							break;
						}
					}
					if (firstVoice == null) {
						throw new Exception("No voices available");
					}
				} catch (InterruptedException e) {
					throw new Exception("Timeout while retrieving voices (exceeded " + timeoutSecs + " seconds)");
				} catch (Exception e) {
					// No need to include something like "Failed to retreive voices", because if
					// there is something wrong with the connection with the engine, failing to
					// retreive the voices is the first thing that will go wrong because that is the
					// first thing that happens.
					throw e;
				} finally {
					timeout.disable();
				}
			}
			TTSResource resource; {
				timeout.enableForCurrentThread(2);
				try {
					resource = engine.allocateThreadResources();
				} catch (Exception e) {
					throw new Exception("Could not allocate resources: " + e.getMessage(), e);
				} finally {
					timeout.disable();
				}
			}
			// synthesize
			SynthesisResult result; {
				try {
					XdmNode ssml = engine.handlesMarks() ? testSSMLWithMark : testSSMLWithoutMark;
					// create a custom interrupter in case the engine hangs
					TTSTimeout.ThreadFreeInterrupter interrupter = new ThreadFreeInterrupter() {
							@Override
							public void threadFreeInterrupt() {
								if (ttsLog != null)
									ttsLog.addGeneralError(
										ErrorCode.WARNING,
										"Timeout while initializing " + service.getName()
										+ ". Forcing interruption of the current work of " + service.getName() + "...");
								engine.interruptCurrentWork(resource);
							}
						};
					result = new TimedTTSExecutor().synthesizeWithTimeout(
					    timeout, interrupter, null, ssml, Sentence.computeSize(ssml),
						engine, firstVoice, resource);
				} catch (Exception e) {
					// No need to include something like "Test failed". Assume that the error has
					// enough information to know that it happened during synthesis.
					throw e;
				} finally {
					if (resource != null)
						timeout.enableForCurrentThread(2);
					try {
						engine.releaseThreadResources(resource);
					} catch (Exception e) {
						if (ttsLog != null)
							ttsLog.addGeneralError(
								ErrorCode.WARNING,
								"Error while releasing resource of " + service.getName() + ": " + e.getMessage(),
								e);
					} finally {
						timeout.disable();
					}
				}
			}
			timeout.close();
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
				throw new Exception("Test failed: " + msg);
			}
			return engine;
		}
	}
}
