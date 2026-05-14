package org.daisy.pipeline.audio.lame.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioFileFormat;

import org.daisy.common.file.URLs;
import org.daisy.common.properties.Properties;
import org.daisy.common.properties.Properties.Property;
import org.daisy.common.shell.BinaryFinder;
import org.daisy.pipeline.audio.AudioEncoder;
import org.daisy.pipeline.audio.AudioEncoderService;
import static org.daisy.pipeline.audio.AudioFileTypes.MP3;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "audio-encoder-lame",
	immediate = true,
	service = { AudioEncoderService.class }
)
public class LameEncoderService implements AudioEncoderService {

	private static final Logger logger = LoggerFactory.getLogger(LameEncoderService.class);
	private static final Property MP3_BITRATE = Properties.getProperty("org.daisy.pipeline.tts.mp3.bitrate",
	                                                                   true,
	                                                                   "Bit rate of MP3 files",
	                                                                   false,
	                                                                   null);
	private static final Property CLI_OPTIONS = Properties.getProperty("org.daisy.pipeline.tts.lame.cli.options",
	                                                                   false,
	                                                                   "Additional command line options passed to lame (deprecated)",
	                                                                   false,
	                                                                   null);

	@Override
	public boolean supportsFileType(AudioFileFormat.Type fileType) {
		 return MP3.equals(fileType);
	}

	@Override
	public Optional<AudioEncoder> newEncoder(Map<String,String> params) {
		LameEncoder.LameEncodingOptions lameOpts = parseEncodingOptions(params);
		try {
			test(lameOpts);
			return Optional.of(new LameEncoder(lameOpts));
		} catch (Exception e) {
			logger.error("Lame encoder can not be instantiated", e);
			return Optional.empty();
		}
	}

	private static LameEncoder.LameEncodingOptions parseEncodingOptions(Map<String,String> params) {
		LameEncoder.LameEncodingOptions opts = new LameEncoder.LameEncodingOptions();
		{
			String bitrate = MP3_BITRATE.getValue(params);
			if (bitrate != null) {
				try {
					opts.bitrate = Integer.valueOf(bitrate);
				} catch (NumberFormatException e) {
					logger.warn(MP3_BITRATE.getName() + ": " + bitrate + "is  not a valid number");
				}
			}
		}
		{
			String prop = "org.daisy.pipeline.tts.lame.cli.options";
			String extraCliArguments = params.get(prop);
			if (extraCliArguments != null) {
				logger.warn("'" + prop + "' setting is deprecated. It may become unavailable in future version of DAISY Pipeline.");
				opts.extraCliArguments = extraCliArguments.split(" ");
			}
		}
		opts.binpath = findLame(params);
		return opts;
	}

	private static String findLame(Map<String,String> params) {
		String lamePath = null;
		String prop = "org.daisy.pipeline.tts.lame.path";
		lamePath = params.get(prop);
		if (lamePath == null) {
			try {
				Optional<File> lame = extractLame();
				if (lame.isPresent())
					lamePath = lame.get().getAbsolutePath();
			} catch (Throwable e) {
				logger.warn("Unexpected error happened while unpacking Lame executable", e);
			}
		}
		if (lamePath == null) {
			Optional<String> lame = BinaryFinder.find("lame");
			if (lame.isPresent())
				lamePath = lame.get();
		}
		return lamePath;
	}

	private static void test(LameEncoder.LameEncodingOptions lameOpts) throws Exception {
		if (lameOpts.binpath == null) {
			throw new RuntimeException("Lame executable not found");
		}
		if (!new File(lameOpts.binpath).exists()) {
			throw new RuntimeException("Lame executable not found: " + lameOpts.binpath);
		}
		logger.debug("Using Lame executable at path '" + lameOpts.binpath + "'");
		// check that the encoder can run
		String[] cmd = new String[] {
			lameOpts.binpath, "--help"
		};
		Process p = null;
		try {
			p = Runtime.getRuntime().exec(cmd);
			// read the output to prevent the process from sleeping
			BufferedReader stdOut = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((stdOut.readLine()) != null);
			p.waitFor();
		} catch (Exception e) {
			if (p != null)
				p.destroy();
			throw e;
		}
	}

	/**
	 * Unpack lame executable
	 */
	private static Optional<File> extractLame() throws IOException, InterruptedException {
		String os = System.getProperty("os.name");
		String lamePath; {
			if (os.toLowerCase().startsWith("windows"))
				lamePath = "windows_x86";
			else if (os.toLowerCase().startsWith("mac os x"))
				lamePath = "macosx";
			else
				return Optional.empty();
		}
		String lameFileName = "lame" + (os.toLowerCase().startsWith("windows") ? ".exe" : "");
		URL url = URLs.getResourceFromJAR(lamePath + "/" + lameFileName, LameEncoderService.class);
		File lame; {
			try {
				lame = new File(URLs.asURI(url));
			} catch (IllegalArgumentException iae) {
				File tmpDirectory = Files.createTempDirectory("pipeline-").toFile();
				tmpDirectory.deleteOnExit();
				// extract all files from directory
				Iterator<String> resources = URLs.listResourcesFromJAR(lamePath, LameEncoderService.class);
				while (resources.hasNext()) {
					String res = resources.next();
					File file = new File(tmpDirectory, new File(res).getName());
					file.deleteOnExit();
					copy(URLs.getResourceFromJAR(res, LameEncoderService.class), file);
				}
				lame = new File(tmpDirectory, lameFileName);
			}
		}
		if (!os.toLowerCase().startsWith("windows"))
			Runtime.getRuntime().exec(new String[] { "chmod", "775", lame.getAbsolutePath() }).waitFor();
		return Optional.of(lame);
	}

	public static void copy(URL url, File file) throws IOException {
		file.getParentFile().mkdirs();
		file.createNewFile();
		FileOutputStream writer = new FileOutputStream(file);
		url.openConnection();
		InputStream reader = url.openStream();
		byte[] buffer = new byte[153600];
		int bytesRead = 0;
		while ((bytesRead = reader.read(buffer)) > 0) {
			writer.write(buffer, 0, bytesRead);
			buffer = new byte[153600];
		}
		writer.close();
		reader.close();
	}
}
