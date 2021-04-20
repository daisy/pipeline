package org.daisy.pipeline.tts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.xml.transform.URIResolver;

import net.sf.saxon.s9api.Processor;

import org.daisy.pipeline.tts.TTSService.SynthesisException;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
	name = "tts-registry",
	service = { TTSRegistry.class }
)
public class TTSRegistry {

	public static class TTSResource {
		public boolean invalid = false;
	}

	private Logger ServerLogger = LoggerFactory.getLogger(TTSRegistry.class);
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
		ServerLogger.info("Adding TTSService " + TTSServiceUtil.displayName(tts));
		mServices.add(tts);
		synchronized (mTTSResources) {
			mTTSResources.put(tts, new ArrayList<TTSResource>());
		}
	}

	/**
	 * Service component callback
	 */
	public void removeTTS(TTSService tts) {
		ServerLogger.info("Removing TTSService " + TTSServiceUtil.displayName(tts));

		List<TTSResource> resources = null;
		synchronized (mTTSResources) {
			resources = mTTSResources.get(tts);
		}
		if (resources != null) {
			if (resources.size() > 0)
				ServerLogger.warn("Stopping bundle of " + TTSServiceUtil.displayName(tts)
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
	 * Return the list of available voices. The SSML adapters and the engines
	 * are not checked, so it might be the case that some of the returned voices
	 * are actually not usable.
	 */
	public Collection<Voice> getAllAvaibleVoices(Processor saxonProcessor) {
		ConfigReader cr = new ConfigReader(saxonProcessor);
		TTSTimeout timeout = new TTSTimeout();
		List<Voice> result = new ArrayList<Voice>();
		for (TTSService service : mServices) {
			try {
				timeout.enableForCurrentThread(30);
				TTSEngine engine = service.newEngine(cr.getStaticProperties());
				result.addAll(engine.getAvailableVoices());
			} catch (Throwable e) {
				//ignore
			} finally {
				timeout.disable();
			}
		}
		timeout.close();
		return result;
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
