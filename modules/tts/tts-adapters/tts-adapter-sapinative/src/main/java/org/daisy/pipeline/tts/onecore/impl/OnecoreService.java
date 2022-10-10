package org.daisy.pipeline.tts.onecore.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.TTSService;
import org.daisy.pipeline.tts.onecore.Onecore;
import org.daisy.pipeline.tts.onecore.OnecoreResult;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.onecore.SAPI;
import org.daisy.pipeline.tts.onecore.SAPIResult;
import org.daisy.pipeline.tts.sapi.impl.SAPIservice;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.directory.InvalidAttributeValueException;
import javax.sound.sampled.AudioFormat;

@Component(
	name = "sapi-onecore-tts-service",
	service = { TTSService.class }
)
public class OnecoreService implements TTSService {

	private static final Logger Logger = LoggerFactory.getLogger(OnecoreEngine.class);
	private boolean onecoreIsLoaded = false;
	private boolean sapiIsLoaded = false;

	private AudioFormat mAudioFormat = null;

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		int OnecorePriority = convertToInt(params, "org.daisy.pipeline.tts.onecore.priority", 7);
		int sampleRate = convertToInt(params, "org.daisy.pipeline.tts.sapi.samplerate", 22050);
		int bytesPerSample = convertToInt(params, "org.daisy.pipeline.tts.sapi.bytespersample", 2);

		AudioFormat audioFormat = new AudioFormat(sampleRate, 8 * bytesPerSample, 1, true,
				false);

		synchronized (this) {
			// trying to load both sapi and onecore to keep using
			// third party voices that could have been installed on sapi registry
			// and not exposed to the onecore registry
			try{
				if (!onecoreIsLoaded) {
					loadOnecoreDLL();
					onecoreIsLoaded = true;
				}
				int res = Onecore.initialize();
				if (res != OnecoreResult.SAPINATIVE_OK.value()) {
					onecoreIsLoaded = false;
					Onecore.dispose();
					throw new SynthesisException(
							"Onecore initialization failed with error code '" + res + "': " + OnecoreResult.valueOfCode(res));

				}
			} catch (Exception e){
				Logger.warn(e.getMessage());
			}
			try{
				// From legacy SAPIService
				if (mAudioFormat != null && !mAudioFormat.matches(audioFormat)) {
					throw new InvalidAttributeValueException(
							"SAPI's audio properties cannot change at runtime.");
				}
				if (!sapiIsLoaded) {
					loadSAPIDLL();;
					sapiIsLoaded = true;
				}
				if (mAudioFormat == null) {
					int res = SAPI.initialize(sampleRate, (short)(8 * bytesPerSample));
					if (res != SAPIResult.SAPINATIVE_OK.value()) {
						SAPI.dispose();
						sapiIsLoaded = false;
						throw new SynthesisException(
								"SAPI initialization failed with error code '" + res + "': " + SAPIResult.valueOfCode(res));
					}
					mAudioFormat = audioFormat;
				}
			} catch (Exception e){
				Logger.warn(e.getMessage());
			}
			if(!onecoreIsLoaded && !sapiIsLoaded){
				throw new SynthesisException("Neither SAPI or Onecore libraries could be loaded.");
			}
		}
		//allocate the engine
		return new OnecoreEngine(this,OnecorePriority,audioFormat,onecoreIsLoaded, sapiIsLoaded);
	}

	@Override
	public String getName() {
		return "sapi-onecore";
	}

	@Deactivate
	protected void deactivate() {
		if(onecoreIsLoaded){
			Onecore.dispose();
			onecoreIsLoaded = false;
		}
		if(sapiIsLoaded){
			SAPI.dispose();
			sapiIsLoaded = false;
		}
	}

	private static int convertToInt(Map<String, String> params, String prop, int defaultVal)
	        throws SynthesisException {
		String str = params.get(prop);
		if (str != null) {
			try {
				defaultVal = Integer.parseInt(str);
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property "
				        + prop);
			}
		}
		return defaultVal;
	}


	/**
	 * Unpack and load onecore.dll
	 */
	static void loadOnecoreDLL() throws SynthesisException {
		OnecoreService.loadDLL("onecorenative.dll");
	}

	public static void loadSAPIDLL() throws SynthesisException {
		OnecoreService.loadDLL("sapinative.dll");
	}


	private static void loadDLL(String dllName) throws SynthesisException {
		if (!System.getProperty("os.name").toLowerCase().startsWith("windows"))
			throw new SynthesisException("Not on Windows");
		URL dll; {
			String arch = System.getProperty("os.arch").toLowerCase();
			if (arch.equals("amd64") || arch.equals("x86_64"))
				dll = URLs.getResourceFromJAR("x64/"+ dllName, SAPIservice.class);
			else
				dll = URLs.getResourceFromJAR("x86/"+ dllName, SAPIservice.class);
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
					int bytesRead = 0;
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
