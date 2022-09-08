package org.daisy.pipeline.tts.onecore.impl;

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
import org.daisy.pipeline.tts.AbstractTTSService;
import org.daisy.pipeline.tts.onecore.OnecoreLib;
import org.daisy.pipeline.tts.onecore.OnecoreLibResult;
import org.daisy.pipeline.tts.TTSEngine;
import org.daisy.pipeline.tts.TTSService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

@Component(
	name = "onecore-tts-service",
	service = { TTSService.class }
)
public class OnecoreService extends AbstractTTSService {

	private boolean mFirstLoad = true;

	@Override
	public TTSEngine newEngine(Map<String, String> params) throws Throwable {
		int priority = convertToInt(params, "org.daisy.pipeline.tts.onecore.priority", 7);


		synchronized (this) {

			if (mFirstLoad) {
				loadDLL();
				mFirstLoad = false;
			}
			int res = OnecoreLib.initialize();
			if (res != OnecoreLibResult.SAPINATIVE_OK.value()) {
				throw new SynthesisException(
						"SAPI initialization failed with error code '" + res + "': " + OnecoreLibResult.valueOfCode(res));
			}
			
		}

		//allocate the engine
		return new OnecoreEngine(this, priority);
	}

	@Override
	public String getName() {
		return "onecore";
	}

	@Override
	public String getVersion() {
		return "native";
	}

	@Activate
	protected void loadSSMLadapter() {
		super.loadSSMLadapter("/transform-ssml-onecore.xsl", OnecoreService.class);
	}

	@Deactivate
	protected void deactivate() {
		OnecoreLib.dispose();
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
	 * Unpack and load onecore.dll
	 */
	static void loadDLL() throws SynthesisException {
		if (!System.getProperty("os.name").toLowerCase().startsWith("windows"))
			throw new SynthesisException("Not on Windows");
		String dllName = "onecorenative.dll";
		URL dll; {
			String arch = System.getProperty("os.arch").toLowerCase();
			if (arch.equals("amd64") || arch.equals("x86_64"))
				dll = URLs.getResourceFromJAR("x64/"+ dllName, OnecoreService.class);
			else
				dll = URLs.getResourceFromJAR("x86/"+ dllName, OnecoreService.class);
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
