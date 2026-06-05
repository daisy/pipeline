package org.daisy.pipeline.tts.mock.impl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.xml.transform.stream.StreamSource;

import com.google.common.collect.Collections2;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.XdmNode;

import org.daisy.common.properties.Properties;
import org.daisy.pipeline.audio.AudioUtils;
import org.daisy.pipeline.tts.config.ConfigReader;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSRegistry;
import org.daisy.pipeline.tts.TTSRegistry.TTSResource;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.TTSService.ServiceDisabledException;
import org.daisy.pipeline.tts.Voice;
import org.daisy.pipeline.tts.VoiceInfo;
import org.daisy.pipeline.tts.VoiceInfo.Gender;
import org.daisy.pipeline.tts.VoiceInfo.LanguageRange;

import org.json.JSONArray;
import org.json.JSONObject;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a local HTTP server that mocks the Google Cloud TTS
 * service. It delegates to {@link TTSEngine} implementations to
 * actually perform the speech synthesis.
 */
@Component(
	name = "mock-google",
	immediate = true
)
public class MockGoogle {

	private static Logger logger = LoggerFactory.getLogger(MockGoogle.class);

	private HttpServer server;
	private TTSRegistry ttsRegistry;
	private final Map<String,TTSEngine> engines = new HashMap<>();
	private final Map<String,Voice> voices = new HashMap<>();
	private final Processor saxonProcessor = new Processor(false);

	@Activate
	public void start() {
		int port = 8080;
		try {
			server = HttpServer.create(new InetSocketAddress(8080), 0);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		// only taking into account system properties and properties defined in global TTS config file
		new ConfigReader(saxonProcessor, new ConfigReader.Extension[]{});
		Map<String,String> properties = Properties.getSnapshot();
		for (TTSService tts : ttsRegistry.getServices())
			if (!"google".equals(tts.getName()))
				try {
					TTSEngine engine = tts.newEngine(properties);
					for (Voice v : engine.getAvailableVoices())
						// only list voices with locale and gender because we don't want to read configuration files
						if (!v.getLocale().isEmpty() && v.getGender().isPresent()) {
							String name = String.format("%s-%s", v.getLocale().iterator().next(), v.getName());
							voices.put(name, v);
							engines.put(name, engine); }}
				catch (ServiceDisabledException e) {
					logger.debug(tts.getName() + " is disabled", e);
					continue; }
				catch (Throwable e) {
					logger.debug(tts.getName() + " could not be activated", e);
					continue; }
		server.createContext(
			"/v1/voices",
			new HttpHandler() {
				public void handle(HttpExchange exchange) throws IOException {
					// ignoring "languageCode" parameter
					try {
						JSONArray jsonVoices = new JSONArray();
						for (String voiceName : voices.keySet()) {
							Voice voice = voices.get(voiceName);
							Collection<String> locale = Collections2.filter(
								Collections2.transform(voice.getLocale(), LanguageRange::toString),
								x -> !x.contains("*")); // only language tags
							if (locale.isEmpty())
								continue; // languageCodes array must be present and not empty
							String gender; {
								switch (voice.getGender().get()) {
								case MALE_ADULT:
								case MALE_CHILD:
								case MALE_ELDERLY:
									gender = "MALE";
									break;
								case FEMALE_CHILD:
								case FEMALE_ADULT:
								case FEMALE_ELDERLY:
									gender = "FEMALE";
									break;
								case ANY:
								default:
									gender = "NEUTRAL";
									break; }}
							jsonVoices = jsonVoices.put(
								new JSONObject().put("name", voiceName)
								.put("languageCodes", new JSONArray(locale.toArray(new String[locale.size()])))
								.put("ssmlGender", gender));
						}
						String response = new JSONObject().put("voices", jsonVoices).toString();
						exchange.sendResponseHeaders(200, response.length());
						OutputStream os = exchange.getResponseBody();
						os.write(response.getBytes());
						os.close();
					} catch (Throwable e) {
						e.printStackTrace();
						exchange.sendResponseHeaders(500, -1); // should not happen
					}
				}
			}
		);
		server.createContext(
			"/v1/text:synthesize",
			new HttpHandler() {
				public void handle(HttpExchange exchange) throws IOException {
					try {
						JSONObject request = new JSONObject(
							new BufferedReader(
								new InputStreamReader(exchange.getRequestBody(), "UTF-8"))
									.lines()
									.collect(Collectors.joining("\n")));
						// assume input is specified as SSML
						XdmNode ssml = saxonProcessor.newDocumentBuilder().build(
							new StreamSource(new StringReader(request.getJSONObject("input").getString("ssml"))));
						// assume voice is specified and ignore languageCode and ssmlGender
						String voiceName = request.getJSONObject("voice").getString("name");
						Voice voice = voices.get(voiceName);
						if (voice == null) {
							exchange.sendResponseHeaders(400, -1);
							return;
						}
						// assume sampleRateHertz is specified
						int sampleRateHertz = request.getJSONObject("audioConfig").getInt("sampleRateHertz");
						// assume audioEncoding is "LINEAR16" (linear PCM, 16-bit, signed, little-endian)
						AudioFormat audioFormat = new AudioFormat(sampleRateHertz, 16, 1, true, false);
						// other settings are ignored
						TTSEngine engine = engines.get(voiceName);
						TTSResource threadResources = engine.allocateThreadResources();
						AudioInputStream audio; {
							try {
								audio = engine.synthesize(
									ssml, voice, threadResources).audio;
							} finally {
								if (threadResources != null)
									engine.releaseThreadResources(threadResources);
							}
							if (!audioFormat.equals(audio.getFormat()))
								audio = AudioUtils.convertAudioStream(audioFormat, audio);
						}
						ByteArrayOutputStream wav = new ByteArrayOutputStream();
						AudioSystem.write(audio, AudioFileFormat.Type.WAVE, wav);
						String response = new JSONObject().put(
							"audioContent",
							Base64.getEncoder().encodeToString(wav.toByteArray())).toString();
						exchange.sendResponseHeaders(200, response.length());
						OutputStream os = exchange.getResponseBody();
						os.write(response.getBytes());
						os.close();
					} catch (Throwable e) {
						e.printStackTrace();
						exchange.sendResponseHeaders(500, -1); // should not happen
					}
				}
			}
		);
		server.setExecutor(null); // default executor uses current thread for all requests (requests are handled sequentially)
		server.start();
		System.err.println("Google Cloud TTS server is listening on http://localhost:" + port);
	}

	@Deactivate
	public void stop() {
		server.stop(1);
	}

	@Reference(
		name = "TTSRegistry",
		unbind = "-",
		service = TTSRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void setTTSRegistry(TTSRegistry registry) {
		ttsRegistry = registry;
	}
}
