package org.daisy.pipeline.script.impl;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.Source;

import org.daisy.common.file.URLs;
import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.MessageAppender;
import org.daisy.common.messaging.MessageBuilder;
import org.daisy.pipeline.job.Job.Status;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.script.ScriptServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WordBasedScript implements ScriptService<Script>, ScriptServiceProvider {

	private final String id;
	private final String version;
	private final String formatId;
	private final String[] formatChain;
	private ScriptRegistry scriptRegistry = null;
	private ScriptService<?> wordToDTBookService = null;
	private ScriptService<?>[] scriptServiceChain = null;

	private static final Logger logger = LoggerFactory.getLogger(WordBasedScript.class);

	public WordBasedScript(String... formatChain) {
		if (formatChain.length == 0)
			throw new IllegalArgumentException();
		for (String format : formatChain)
			if (!("daisy3".equals(format)
			      || "daisy202".equals(format)
			      || "epub3".equals(format)
			      || "html".equals(format)
			      || "mp3".equals(format)))
				throw new IllegalArgumentException();
		this.formatChain = formatChain;
		this.formatId = formatChain[formatChain.length - 1];
		this.id = "word-to-" + this.formatId;
		Properties mavenProps = new Properties();
		try (InputStream is = URLs.getResourceFromJAR("/maven.properties", WordBasedScript.class)
		                          .openStream()) {
			mavenProps.load(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		this.version = mavenProps.getProperty("version");
	}

	protected void setScriptRegistry(ScriptRegistry registry) {
		scriptRegistry = registry;
	}

	/* --------------------- */
	/* ScriptServiceProvider */
	/* --------------------- */

	private Iterable<ScriptService<?>> scripts = null;

	@Override
	public Iterable<ScriptService<?>> getScripts() {
		if (scripts == null) {
			if (scriptRegistry == null)
				throw new IllegalStateException("setScriptRegistry() must be called first");
			String wordToDTBookScriptId = "word-to-dtbook";
			wordToDTBookService = scriptRegistry.getScript(wordToDTBookScriptId);
			if (wordToDTBookService == null) {
				logger.debug("failed to load script " + id + ": no script found with ID " + wordToDTBookScriptId);
				return Collections.emptyList();
			}
			scriptServiceChain = new ScriptService<?>[formatChain.length];
			int i = 0;
			String from = "dtbook";
			for (String to : formatChain) {
				String scriptId = from + "-to-" + to;
				ScriptService<?> scriptService = scriptRegistry.getScript(scriptId);
				if (scriptService == null) {
					logger.debug("failed to load script " + id + ": no script found with ID " + scriptId);
					return Collections.emptyList();
				}
				scriptServiceChain[i++] = scriptService;
				from = to;
			}
			scripts = Collections.singleton(this);
		}
		return scripts;
	}

	/* ------------- */
	/* ScriptService */
	/* ------------- */

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getVersion() {
		return version;
	}

	private Script script = null;
	private Script wordToDTBook = null;
	private Script[] scriptChain = null;

	@Override
	public Script load() {
		if (wordToDTBookService == null || scriptServiceChain == null)
			throw new IllegalStateException(); // the ScriptService interface is supposed to be used only
			                                   // after ScriptServiceProvider/ getScripts() has been called
		boolean rebuildScript = false;
		if (script == null)
			rebuildScript = true;
		Script wordToDTBook = wordToDTBookService.load();
		if (wordToDTBook != this.wordToDTBook) {
			rebuildScript = true;
			this.wordToDTBook = wordToDTBook;
		}
		if (scriptChain == null)
			scriptChain = new Script[scriptServiceChain.length];
		int i = 0;
		for (ScriptService<?> ss : scriptServiceChain) {
			if (ss == null)
				throw new IllegalStateException();
			Script s = ss.load();
			if (s != scriptChain[i]) {
				rebuildScript = true;
				scriptChain[i] = s;
			}
			i++;
		}
		if (rebuildScript) {
			Script.Builder builder = new Builder();
			String formatName = getFormatName(formatId);
			builder = builder
				.withInputFileset("docx")
				.withOutputFileset(formatId)
				.withShortName("Word to " + formatName + " (experimental)")
				.withDescription(
					"Transforms a Microsoft Office Word (.docx) document into "
					+ (formatId.equals("mp3")
					      ? "a folder structure with MP3 files suitable for playback on MegaVoice Envoy devices"
					      : (formatName + " format")) + ".")
				// FIXME: home page has yet to be created
				.withHomepage(
					"http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/word-to-"
					+ formatId + "/")
				;
			for (ScriptPort p : wordToDTBook.getInputPorts())
				builder = builder.withInputPort(p.getName(), p);
			for (ScriptOption o : wordToDTBook.getOptions()) {
				String name = o.getName();
				if (   "title".equals(name)
				    || "creator".equals(name)
				    || "publisher".equals(name)
				    || "uid".equals(name)
				    || "subject".equals(name)
				    || "accept-revisions".equals(name)
				    || "repair".equals(name)
				    || "tidy".equals(name)
				)
					builder = builder.withOption(name, o);
			}
			Script lastScript = null;
			for (Script s : scriptChain) {
				for (ScriptOption o : s.getOptions()) {
					String name = o.getName();                     //  daisy3  |  daisy202  |  epub3  |  html  |  mp3
					if (   "include-tts-log".equals(name)          //  x          x            x                  x
					    || "tts-config".equals(name)               //  x          x            x                  x
					    || "language".equals(name)                 //             x            x         x
					    || "validation".equals(name)               //             x            x         x
					    || "folder-depth".equals(name)             //                                             x
					    || (!"mp3".equals(formatId) &&
					         (   "audio".equals(name)              //  x          x            x
					          || "with-text".equals(name)          //  x
					          || "word-detection".equals(name)))   //  x
					)
						// note that this will override any previously defined options with the same name
						builder = builder.withOption(name, o);
				}
				lastScript = s;
			}
			// take outputs of last script
			for (ScriptPort p : lastScript.getOutputPorts())
				builder = builder.withOutputPort(p.getName(), p);
			script = builder.build();
		}
		return script;
	}

	private class Builder extends Script.Builder {

		Builder() {
			super(WordBasedScript.this);
		}

		@Override
		public ScriptImpl build() {
			return new ScriptImpl(shortName, description, homepage, inputPorts, outputPorts,
			                      options, inputFilesets, outputFilesets);
		}
	}

	private class ScriptImpl extends Script {

		private final Script wordToDTBook;
		private final Script[] scriptChain;

		private ScriptImpl(String name, String description, String homepage,
		                   Map<String,ScriptPort> inputPorts, Map<String,ScriptPort> outputPorts,
		                   Map<String,ScriptOption> options, List<String> inputFilesets,
		                   List<String> outputFilesets) {
			super(id, version, name, description, homepage, inputPorts, outputPorts, options,
			      inputFilesets, outputFilesets);
			this.wordToDTBook = WordBasedScript.this.wordToDTBook;
			this.scriptChain = WordBasedScript.this.scriptChain.clone();
		}

		@Override
		public Status run(ScriptInput input, Map<String,String> properties,
		                  MessageAppender messages, JobResultSet.Builder resultBuilder,
		                  File resultDir) throws IOException {
			ScriptInput.Builder stepInput = new ScriptInput.Builder(input.getResources());
			for (Source s : input.getInput("source"))
				stepInput = stepInput.withInput("source", s);
			stepInput = pickOptions(wordToDTBook, stepInput, input);
			JobResultSet.Builder stepResultBuilder = new JobResultSet.Builder(wordToDTBook);
			File stepResultDir = new File(resultDir, "word-to-dtbook");
			mkdirs(stepResultDir);
			Status status = null;
			BigDecimal portion = BigDecimal.ONE.divide(
				new BigDecimal(1 + scriptChain.length), MathContext.DECIMAL128);
			try (MessageAppender stepMessages = messages != null
			         ? messages.append(new MessageBuilder().withProgress(portion))
			         : null) {
				status = wordToDTBook.run(stepInput.build(),
			                              properties,
			                              stepMessages,
			                              stepResultBuilder,
			                              stepResultDir);
			}
			Script prevStep = wordToDTBook;
			String prevStepResultFormat = "dtbook";
			int i = 0;
			for (Script step : scriptChain) {
				switch (status) {
				case ERROR:
				case FAIL:
					return status;
				case SUCCESS:
				default:
				}
				JobResultSet stepResult = stepResultBuilder.build();
				stepInput = new ScriptInput.Builder();
				for (JobResult r : stepResult.getResults("result"))
					if (isPrimaryFile(r.getPath().getName(), prevStepResultFormat))
						stepInput = stepInput.withInput("source", r.getPath());
				stepInput = pickOptions(step, stepInput, input);
				if ("mp3".equals(formatId))
					// certain options must have a fixed value for mp3 output
					for (ScriptOption o : step.getOptions()) {
						String name = o.getName();
						if ("audio".equals(name))
							stepInput = stepInput.withOption(name, "true");
						else if ("with-text".equals(name))
							stepInput = stepInput.withOption(name, "false");
						else if ("word-detection".equals(name))
							stepInput = stepInput.withOption(name, "false");
					}
				String stepResultFormat = formatChain[i];
				stepResultBuilder = formatId.equals(stepResultFormat)
					? resultBuilder // this is the last step
					: new JobResultSet.Builder(step);
				stepResultDir = new File(resultDir, step.getId());
				mkdirs(stepResultDir);
				try (MessageAppender stepMessages = messages != null
				         ? messages.append(
				             new MessageBuilder().withProgress(portion)
				                                 .withText("Converting " + getFormatName(prevStepResultFormat)
				                                           + " to " + getFormatName(stepResultFormat))
				                                 .withLevel(Message.Level.INFO))
				         : null) {
					status = step.run(stepInput.build(),
					                  properties,
					                  stepMessages,
					                  stepResultBuilder,
					                  stepResultDir);
				}
				prevStep = step;
				prevStepResultFormat = stepResultFormat;
				i++;
			}
			return status;
		}
	}

	private static String getFormatName(String formatId) {
		if ("dtbook".equals(formatId))
			return "DTBook";
		else if ("daisy3".equals(formatId))
			return "DAISY 3";
		else if ("daisy202".equals(formatId))
			return "DAISY 2.02";
		else if ("epub3".equals(formatId))
			return "EPUB 3";
		else if ("html".equals(formatId))
			return "HTML";
		else if ("mp3".equals(formatId))
			return "navigable MP3 file-set";
		else
			throw new IllegalArgumentException("unknown format " + formatId);
	}

	private static boolean isPrimaryFile(String fileName, String formatId) {
		if ("dtbook".equals(formatId))
			return fileName.endsWith(".xml");
		if ("daisy3".equals(formatId))
			return fileName.endsWith(".opf");
		if ("daisy202".equals(formatId))
			return fileName.equals("ncc.html"); // assume output of epub3-to-daisy202
		else if ("epub3".equals(formatId))
			return fileName.endsWith(".epub");
		else
			throw new IllegalArgumentException();
	}

	private static void mkdirs(File dir) throws IOException {
		if (!dir.exists() && !dir.mkdirs())
			throw new IOException("Could not create directory:" + dir.getAbsolutePath());
	}

	private static ScriptInput.Builder pickOptions(Script script, ScriptInput.Builder toInput, ScriptInput fromInput) {
		for (ScriptOption o : script.getOptions()) {
			String name = o.getName();
			for (String s : fromInput.getOption(name))
				toInput = toInput.withOption(name, s);
		}
		return toInput;
	}
}
