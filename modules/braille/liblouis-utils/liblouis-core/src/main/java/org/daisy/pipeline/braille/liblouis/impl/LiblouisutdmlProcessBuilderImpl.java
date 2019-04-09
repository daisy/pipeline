package org.daisy.pipeline.braille.liblouis.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;

import static org.daisy.common.file.URLs.asURL;
import org.daisy.pipeline.braille.common.NativePath;
import org.daisy.pipeline.braille.common.ResourceResolver;
import static org.daisy.pipeline.braille.common.util.Files.asFile;
import static org.daisy.pipeline.braille.common.util.Files.isAbsoluteFile;
import static org.daisy.pipeline.braille.common.util.Strings.join;

import org.daisy.pipeline.braille.liblouis.LiblouisTable;
import org.daisy.pipeline.braille.liblouis.LiblouisTableResolver;
import org.daisy.pipeline.braille.liblouis.Liblouisutdml;
import org.daisy.pipeline.braille.liblouis.LiblouisutdmlConfigResolver;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
	name = "org.daisy.pipeline.braille.liblouis.impl.LiblouisutdmlProcessBuilderImpl",
	service = {
		Liblouisutdml.class
	}
)
public class LiblouisutdmlProcessBuilderImpl implements Liblouisutdml {
	
	private File file2brl;
	private LiblouisTableResolver tableResolver;
	private ResourceResolver configResolver;
	
	@Activate
	protected void activate() {
		logger.debug("Loading liblouisutdml service");
	}
	
	@Deactivate
	protected void deactivate() {
		logger.debug("Unloading liblouisutdml service");
	}

	@Reference(
		name = "File2brlExecutable",
		unbind = "-",
		service = NativePath.class,
		target = "(identifier=http://www.liblouis.org/native/*)",
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindExecutable(NativePath nativePath) {
		URI executablePath = nativePath.get("liblouisutdml/file2brl").iterator().next();
		file2brl = asFile(nativePath.resolve(executablePath));
		logger.debug("Registering file2brl executable: " + executablePath);
	}
	
	@Reference(
		name = "LiblouisTableResolver",
		unbind = "-",
		service = LiblouisTableResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindTableResolver(LiblouisTableResolver tableResolver) {
		this.tableResolver = tableResolver;
	}
	
	@Reference(
		name = "LiblouisutdmlConfigResolver",
		unbind = "-",
		service = LiblouisutdmlConfigResolver.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	protected void bindConfigResolver(LiblouisutdmlConfigResolver configResolver) {
		this.configResolver = configResolver;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void translateFile(
			List<String> configFiles,
			List<String> semanticFiles,
			LiblouisTable table,
			Map<String,String> otherSettings,
			File input,
			File output,
			URI configPath,
			File tempDir) {
		
		try {
			
			File configPathFile = (configPath != null) ? resolveConfigPath(configPath) : tempDir;
			
			if (!Arrays.asList(configPathFile.list()).contains("liblouisutdml.ini"))
				throw new RuntimeException("liblouisutdml.ini must be placed in " + configPathFile);
			if (configFiles != null)
				configFiles.remove("liblouisutdml.ini");
			
			List<String> command = new ArrayList<String>();
			
			command.add(file2brl.getAbsolutePath());
			command.add("-f");
			command.add(configPathFile.getAbsolutePath() + File.separator +
					(configFiles != null ? join(configFiles, ",") : ""));
			Map<String,String> settings = new HashMap<String,String>();
			if (semanticFiles != null && semanticFiles.size() > 0)
				settings.put("semanticFiles", join(semanticFiles, ","));
			if (table != null) {
				String tablePath = "\"" + resolveTable(table) + "\"";
				settings.put("literaryTextTable", tablePath);
				settings.put("editTable", tablePath); }
			if (otherSettings != null)
				settings.putAll(otherSettings);
			for (String key : settings.keySet())
				command.add("-C" + key + "=" + settings.get(key));
			command.add(input.getAbsolutePath());
			command.add(output.getAbsolutePath());
			
			logger.debug("\n" + join(command, "\n\t"));
			
			ProcessBuilder builder = new ProcessBuilder(command);
			builder.directory(tempDir);
			
			// Hack to make sure tables on configPath are found
			if (!configPathFile.equals(tempDir))
				builder.environment().put("LOUIS_TABLEPATH", configPathFile.getCanonicalPath());
			
			Process process = builder.start();
			
			new StreamReaderThread(
					process.getErrorStream(),
					new Function<List<String>,Void>() {
						public Void apply(List<String> error) {
							logger.debug("\nstderr:\n\t" + join(error, "\n\t"));
							return null; }}).start();
			
			int exitValue = process.waitFor();
			logger.debug("\nexit value: " + exitValue);
			if (exitValue != 0)
				throw new RuntimeException("liblouisutdml exited with value " + exitValue); }
			
		catch (Exception e) {
			logger.error("Error during liblouisutdml conversion", e);
			throw new RuntimeException("Error during liblouisutdml conversion", e); }
	}
	
	private String resolveTable(LiblouisTable table) throws IOException {
		File[] resolved = tableResolver.resolveLiblouisTable(table, null);
		if (resolved == null)
			throw new RuntimeException("Liblouis table " + table + " could not be resolved");
		String[] files = new String[resolved.length];
		for (int i = 0; i < resolved.length; i++)
			files[i] = resolved[i].getCanonicalPath();
		return join(files, ",");
	}
	
	private File resolveConfigPath(URI configPath) {
		URL resolvedConfigPath = isAbsoluteFile(configPath) ? asURL(configPath) : configResolver.resolve(configPath);
		if (resolvedConfigPath == null)
			throw new RuntimeException("Liblouisutdml config path " + configPath + " could not be resolved");
		return asFile(resolvedConfigPath);
	}
	
	private static final Logger logger = LoggerFactory.getLogger(LiblouisutdmlProcessBuilderImpl.class);
	
	private static class StreamReaderThread extends Thread {
		
		private InputStream stream;
		private Function<List<String>,Void> callback;
		
		public StreamReaderThread(InputStream stream, Function<List<String>,Void> callback) {
			this.stream = stream;
			this.callback = callback;
		}
		
		@Override
		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
				List<String> result = new ArrayList<String>();
				String line = null;
				while ((line = reader.readLine()) != null) result.add(line);
				if (callback != null)
					callback.apply(result); }
			catch (IOException e) {
				throw new RuntimeException(e); }
		}
	}
}
