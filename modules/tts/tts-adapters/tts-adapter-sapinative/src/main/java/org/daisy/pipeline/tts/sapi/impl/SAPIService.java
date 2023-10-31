package org.daisy.pipeline.tts.sapi.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

import javax.naming.directory.InvalidAttributeValueException;
import javax.sound.sampled.AudioFormat;

import org.daisy.common.file.URLs;
import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.pipeline.tts.onecore.Onecore;
import org.daisy.pipeline.tts.onecore.OnecoreResult;
import org.daisy.pipeline.tts.onecore.SAPI;
import org.daisy.pipeline.tts.onecore.SAPIResult;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "sapi-onecore-tts-service",
	service = { TTSService.class }
)
public class SAPIService implements TTSService {

	private static final Logger logger = LoggerFactory.getLogger(SAPIEngine.class);

	private static final Property SAPI_SAMPLERATE = Properties.getProperty("org.daisy.pipeline.tts.sapi.samplerate",
	                                                                       true,
	                                                                       "Audio sample rate of legacy Windows voices (in Hz)",
	                                                                       false,
	                                                                       "22050");
	private static final Property SAPI_BYTESPERSAMPLE = Properties.getProperty("org.daisy.pipeline.tts.sapi.bytespersample",
	                                                                           true,
	                                                                           "Audio bit depth of legacy Windows voices (in bytes per sample)",
	                                                                           false,
	                                                                           "2");
	private static final Property SAPI_PRIORITY = Properties.getProperty("org.daisy.pipeline.tts.sapi.priority",
	                                                                     true,
	                                                                     "Priority of Windows voices relative to voices of other engines",
	                                                                     false,
	                                                                     "7");

	private static boolean onecoreDLLIsLoaded = false;
	private static boolean onecoreIsInitialized = false;
	private static boolean sapiDLLIsLoaded = false;

	/**
	 * AudioFormat of the sapi output.
	 * Can only be set by the initalize method,
	 * so SAPI.dispose() needs to be called and this need to be set to null
	 * before resetting it with the loadSAPI() method
	 */
	private static AudioFormat sapiAudioFormat = null;

	@Override
	public TTSEngine newEngine(Map<String,String> properties) throws Throwable {
		int priority = getPropertyAsInt(properties, SAPI_PRIORITY).get();
		synchronized (this) {
			// try to load both sapi and onecore to keep using third party voices that could have
			// been installed on sapi registry and not exposed to the onecore registry
			try {
				loadAndInitializeOnecore();
			} catch (Exception e) {
				logger.warn(e.getMessage());
			}
			try {
				int sapiSampleRate = getPropertyAsInt(properties, SAPI_SAMPLERATE).get();
				int sapiBytesPerSample = getPropertyAsInt(properties, SAPI_BYTESPERSAMPLE).get();
				AudioFormat format = new AudioFormat(sapiSampleRate, 8 * sapiBytesPerSample, 1, true, false);
				if (sapiAudioFormat != null && !sapiAudioFormat.matches(format)) {
					throw new InvalidAttributeValueException(
						"SAPI's audio properties cannot change at runtime.");
				}
				loadAndInitializeSAPI(sapiSampleRate,sapiBytesPerSample);

			} catch (Exception e){
				logger.warn(e.getMessage());
			}
			if (!onecoreIsInitialized && sapiAudioFormat == null){
				throw new SynthesisException("Neither SAPI or Onecore libraries could be loaded.");
			}
		}
		return new SAPIEngine(this, priority, onecoreIsInitialized, sapiAudioFormat);
	}

	@Override
	public String getName() {
		return "sapi";
	}

	@Deactivate
	protected void deactivate() {
		ReleaseOnecore();
		ReleaseSAPI();
	}

	private static Optional<Integer> getPropertyAsInt(Map<String,String> properties, Property prop) throws SynthesisException {
		String str = prop.getValue(properties);
		if (str != null) {
			try {
				return Optional.of(Integer.valueOf(str));
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property " + prop.getName());
			}
		}
		return Optional.empty();
	}

	/**
	 * Counter of times the loading of Onecore was requested (to avoid early disposal of the library)
	 */
	private static int onecoreRequestCounter = 0;
	/**
	 * Unpack and load onecorenative.dll and initialize the Onecore API.
	 * <br/>
	 * The dll is not reloaded and the API is not initialized if it has already been done before
	 * @throws SynthesisException if the initialization of the Onecore API fails
	 */
	synchronized static void loadAndInitializeOnecore() throws SynthesisException {
		// Load and initialize Onecore if not loaded a
		if (!onecoreDLLIsLoaded) {
			SAPIService.loadDLL("onecorenative.dll");
			onecoreDLLIsLoaded = true;
		}
		if(!onecoreIsInitialized){
			int res = Onecore.initialize();
			if (res != OnecoreResult.SAPINATIVE_OK.value()) {
				Onecore.dispose();
				throw new SynthesisException(
						"Onecore initialization failed with error code '" + res + "': " + OnecoreResult.valueOfCode(res));
			}
			onecoreIsInitialized = true;
		}
		onecoreRequestCounter += 1;
	}

	/**
	 * Dispose of the Onecore library if it is not used anymore.
	 */
	synchronized static void ReleaseOnecore(){
		onecoreRequestCounter--;
		// no more code requesting onecore access, allow the dispose method to run
		if(onecoreRequestCounter <= 0 && onecoreIsInitialized){
			Onecore.dispose();
			onecoreIsInitialized = false;
		}
	}


	/**
	 * Counter of times the loading of SAPI was requested (to avoid early disposal of the library)
	 */
	private static int sapiRequestCounter = 0;

	/**
	 * Unpack and load sapinative.dll and initialize the SAPI API.
	 * <br/>
	 * The dll is not reloaded and the API is not initialized if it has already been done before
	 * @param requestedSampleRate is the wanted number of sample per second in the audio stream
	 * @param requestedBytesPerSample is the wanted number of bytes (not bits) per sample in the audio stream
	 * @throws SynthesisException if the initialization of the SAPI API fails
	 */
	synchronized static void loadAndInitializeSAPI(int requestedSampleRate, int requestedBytesPerSample) throws SynthesisException {
		if(!sapiDLLIsLoaded){
			SAPIService.loadDLL("sapinative.dll");
			sapiDLLIsLoaded = true;
		}
		if(sapiAudioFormat == null){
			int res = SAPI.initialize(requestedSampleRate, (short)(8 * requestedBytesPerSample));
			if (res != SAPIResult.SAPINATIVE_OK.value()) {
				SAPI.dispose();
				throw new SynthesisException(
						"SAPI initialization failed with error code '" + res + "': " + SAPIResult.valueOfCode(res));
			}
			sapiAudioFormat = new AudioFormat(
					requestedSampleRate,
					8 * requestedBytesPerSample,
					1,
					true,
					false
			);
		}
		sapiRequestCounter += 1;
	}

	/**
	 * Dispose of the SAPI library if it is not used anymore.
	 */
	synchronized static void ReleaseSAPI(){
		sapiRequestCounter--;
		// no more code requesting onecore access, allow the dispose method to run
		if(sapiRequestCounter <= 0 && sapiAudioFormat != null){
			SAPI.dispose();
			sapiAudioFormat = null;
		}
	}

	/**
	 * Load an embedded windows dll in the JVM (to expose JNI library like sapinative and onecorenative dlls)
	 * @param dllName is the name of the dll in the jar
	 * @throws SynthesisException if the system is not windows or if the dll could not be unpacked
	 */
	private static void loadDLL(String dllName) throws SynthesisException {
		if (!System.getProperty("os.name").toLowerCase().startsWith("windows"))
			throw new SynthesisException("Not on Windows");
		URL dll; {
			String arch = System.getProperty("os.arch").toLowerCase();
			if (arch.equals("amd64") || arch.equals("x86_64"))
				dll = URLs.getResourceFromJAR("x64/"+ dllName, SAPIService.class);
			else
				dll = URLs.getResourceFromJAR("x86/"+ dllName, SAPIService.class);
		}
		File dllFile; {
			try {
				dllFile = new File(URLs.asURI(dll));
			} catch (IllegalArgumentException iae) {
				try {
					File tmpDirectory = Files.createTempDirectory("pipeline-").toFile();
					tmpDirectory.deleteOnExit();
					dllFile = new File(tmpDirectory, dllName);
					dllFile.deleteOnExit();
					dllFile.getParentFile().mkdirs();
					dllFile.createNewFile();
					FileOutputStream writer = new FileOutputStream(dllFile);
					dll.openConnection();
					InputStream reader = dll.openStream();
					byte[] buffer = new byte[153600];
					int bytesRead;
					while ((bytesRead = reader.read(buffer)) > 0) {
						writer.write(buffer, 0, bytesRead);
						buffer = new byte[153600];
					}
					writer.close();
					reader.close();
				} catch (IOException e) {
					throw new SynthesisException("Could not unpack " + dllName, e);
				}
			}
		}
		System.load(dllFile.getAbsolutePath());
	}
}
