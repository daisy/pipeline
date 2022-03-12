package org.daisy.pipeline.tts.sapinative.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;

import javax.naming.directory.InvalidAttributeValueException;
import javax.sound.sampled.AudioFormat;

import org.daisy.common.file.URLs;
import org.daisy.pipeline.tts.sapinative.SAPILib;
import org.daisy.pipeline.tts.sapinative.SAPILibResult;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(
	name = "sapinative-tts-service",
	service = { TTSService.class }
)
public class SAPIservice implements TTSService {

	private boolean mFirstLoad = true;
	private AudioFormat mAudioFormat = null;

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		int sampleRate = convertToInt(params, "org.daisy.pipeline.tts.sapi.samplerate", 22050);
		int bytesPerSample = convertToInt(params, "org.daisy.pipeline.tts.sapi.bytespersample", 2);
		int priority = convertToInt(params, "org.daisy.pipeline.tts.sapi.priority", 7);

		AudioFormat audioFormat = new AudioFormat(sampleRate, 8 * bytesPerSample, 1, true,
		        false);

		synchronized (this) {
			if (mAudioFormat != null && !mAudioFormat.matches(audioFormat)) {
				throw new InvalidAttributeValueException(
				        "SAPI's audio properties cannot change at runtime.");
			}

			if (mFirstLoad) {
				loadDLL();
				mFirstLoad = false;
			}

			if (mAudioFormat == null) {
				int res = SAPILib.initialize(sampleRate, (short)(8 * bytesPerSample));
				if (res != SAPILibResult.SAPINATIVE_OK.value()) {
					throw new SynthesisException(
					        "SAPI initialization failed with error code '" + res + "': " + SAPILibResult.valueOfCode(res));
				}
				mAudioFormat = audioFormat;
			}
		}

		//allocate the engine
		return new SAPIengine(this, audioFormat, priority);
	}

	@Override
	public String getName() {
		return "sapi";
	}

	@Deactivate
	protected void deactivate() {
		SAPILib.dispose();
	}

	private static int convertToInt(Map<String, String> params, String prop, int defaultVal)
	        throws SynthesisException {
		String str = params.get(prop);
		if (str != null) {
			try {
				defaultVal = Integer.valueOf(str);
			} catch (NumberFormatException e) {
				throw new SynthesisException(str + " is not a valid a value for property "
				        + prop);
			}
		}
		return defaultVal;
	}

	/**
	 * Unpack and load sapinative.dll
	 */
	static void loadDLL() throws SynthesisException {
		if (!System.getProperty("os.name").toLowerCase().startsWith("windows"))
			throw new SynthesisException("Not on Windows");
		URL dll; {
			String arch = System.getProperty("os.arch").toLowerCase();
			if (arch.equals("amd64") || arch.equals("x86_64"))
				dll = URLs.getResourceFromJAR("x64/sapinative.dll", SAPIservice.class);
			else
				dll = URLs.getResourceFromJAR("x86/sapinative.dll", SAPIservice.class);
		}
		File dllFile; {
			try {
				dllFile = new File(URLs.asURI(dll));
			} catch (IllegalArgumentException iae) {
				try {
					File tmpDirectory = Files.createTempDirectory("pipeline-").toFile();
					tmpDirectory.deleteOnExit();
					dllFile = new File(tmpDirectory, "sapinative.dll");
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
					throw new SynthesisException("Could not unpack sapinative.dll", e);
				}
			}
		}
		System.load(dllFile.getAbsolutePath());
	}
}
