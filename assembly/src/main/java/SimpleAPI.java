import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.spi.CreateOnStart;
import org.daisy.common.spi.ServiceLoader;
import org.daisy.common.transform.LazySaxResultProvider;
import org.daisy.common.transform.LazySaxSourceProvider;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobFactory;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * A simplified Java API consisting of a {@link #startJob()} method that accepts a script name and a
 * list of options and returns a {@link Job}, and a {@link #getNewMessages()} method. It is used to
 * build a simple Java CLI (see the {@link #main()} method). The simplified API also makes it easier
 * to bridge with other programming languages using JNI.
 */
@Component(
	name = "SimpleAPI",
	immediate = true
)
public class SimpleAPI {

	private ScriptRegistry scriptRegistry;
	private JobFactory jobFactory;

	@Reference(
		name = "script-registry",
		unbind = "-",
		service = ScriptRegistry.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setScriptRegistry(ScriptRegistry scriptRegistry) {
		this.scriptRegistry = scriptRegistry;
	}

	@Reference(
		name = "job-factory",
		unbind = "-",
		service = JobFactory.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setJobFactory(JobFactory jobFactory) {
		this.jobFactory = jobFactory;
	}

	private Job _startJob(String scriptName, Map<String,String> options) throws IOException {
		XProcScriptService scriptService = scriptRegistry.getScript(scriptName);
		if (scriptService == null)
			throw new IllegalArgumentException(scriptName + " script not found");
		XProcScript script = scriptService.load();
		File cwd = new File(System.getProperty("org.daisy.pipeline.cli.cwd", "."));
		XProcInput.Builder inputs = new XProcInput.Builder(); {
			for (XProcPortInfo port : script.getXProcPipelineInfo().getInputPorts()) {
				if (options.containsKey(port.getName())) {
					File file = new File(options.remove(port.getName()));
					if (!file.isAbsolute())
						file = new File(cwd, file.getPath()).getCanonicalFile();
					if (!file.exists()) {
						throw new FileNotFoundException(file.getPath());
					}
					inputs.withInput(port.getName(), new LazySaxSourceProvider(file.toURI().toURL().toString()));
				} else if (script.getPortMetadata(port.getName()).isRequired()) {
					throw new IllegalArgumentException("Required option " + port.getName() + " missing");
				}
			}
			for (XProcOptionInfo option : script.getXProcPipelineInfo().getOptions()) {
				XProcOptionMetadata metadata = script.getOptionMetadata(option.getName());
				if (metadata.getOutput() == XProcOptionMetadata.Output.TEMP)
					continue;
				if (options.containsKey(option.getName().toString())) {
					String value = options.remove(option.getName().toString());
					String type = metadata.getType();
					if ("anyFileURI".equals(type)) {
						File file = new File(value);
						if (!file.isAbsolute())
							file = new File(cwd, file.getPath()).getCanonicalFile();
						if (metadata.getOutput() == XProcOptionMetadata.Output.RESULT) {
							if (file.exists()) {
								throw new IllegalArgumentException("File exists: " + file);
							}
						} else {
							if (!file.exists()) {
								throw new FileNotFoundException(file.getPath());
							}
						}
						value = file.toURI().toURL().toString();
					} else if ("anyDirURI".equals(type)) {
						File dir = new File(value);
						if (!dir.isAbsolute())
							dir = new File(cwd, dir.getPath()).getCanonicalFile();
						if (dir.exists() && !dir.isDirectory()) {
							throw new IllegalArgumentException("Not a directory: " + dir);
						}
						if (metadata.getOutput() == XProcOptionMetadata.Output.RESULT) {
							if (dir.exists() && dir.list().length > 0) {
								throw new IllegalArgumentException("Directory is not empty: " + dir);
							}
						} else {
							if (!dir.exists()) {
								throw new FileNotFoundException(dir.getPath());
							}
						}
						value = dir.toURI().toURL().toString();
					}
					inputs.withOption(option.getName(), value);
				} else if (option.isRequired() && metadata.getOutput() != XProcOptionMetadata.Output.RESULT) {
					throw new IllegalArgumentException("Required option " + option.getName() + " missing");
				}
			}
		}
		XProcOutput.Builder outputs = new XProcOutput.Builder(); {
			for (XProcPortInfo port : script.getXProcPipelineInfo().getOutputPorts()) {
				if (options.containsKey(port.getName())) {
					String filePath = options.remove(port.getName());
					File file = new File(filePath);
					if (!file.isAbsolute())
						file = new File(cwd, file.getPath()).getCanonicalFile();
					if (file.isDirectory() || filePath.endsWith("/") || port.isSequence()) {
						if (file.isDirectory()) {
							if (file.list().length > 0) {
								throw new IllegalArgumentException("Directory is not empty: " + file);
							}
						} else if (file.exists()) {
							throw new IllegalArgumentException("Not a directory: " + file);
						}
						filePath = file.toURI().toURL().toString() + "/";
					} else {
						if (file.exists()) {
							throw new IllegalArgumentException("File exists: " + file);
						}
						filePath = file.toURI().toURL().toString();
					}
					outputs.withOutput(port.getName(), new LazySaxResultProvider(filePath));
				}
			}
		}
		for (String unknown : options.keySet()) {
			throw new IllegalArgumentException("Unknown option: " + unknown);
		}
		BoundXProcScript boundScript = BoundXProcScript.from(script, inputs.build(), outputs.build());
		Job job = jobFactory.newJob(boundScript).isMapping(true).build().get();
		MessageAccessor accessor = job.getMonitor().getMessageAccessor();
		accessor.listen(
			num -> {
				consumeMessage(accessor, num);
			}
		);
		new Thread(job).start();
		return job;
	}

	public static Job startJob(String scriptName, Map<String,String> options) throws IOException {
		return getInstance()._startJob(scriptName, options);
	}

	/**
	 * Singleton thread safe instance of SimpleAPI.
	 */
	private static SimpleAPI INSTANCE;
	private static SimpleAPI getInstance() {
		if (INSTANCE == null) {
			for (CreateOnStart o : ServiceLoader.load(CreateOnStart.class))
				if (INSTANCE == null && o instanceof SimpleAPI)
					INSTANCE = (SimpleAPI)o;
			if (INSTANCE == null)
				throw new IllegalStateException();
		}
		return INSTANCE;
	}

	public static List<Message> messagesQueue = new ArrayList<>();
	public static int lastMessage = -1;
	private static synchronized void consumeMessage(MessageAccessor accessor, int seqNum) {
		for (Message m :
				accessor.createFilter()
				        .greaterThan(lastMessage)
				        .filterLevels(Collections.singleton(Level.INFO))
				        .getMessages()) {
			if (m.getSequence() > lastMessage) {
				messagesQueue.add(m);
			}
		}
		lastMessage = seqNum;
	}

	/**
	 * Get the list of new top-level messages (messages that have not
	 * been returned yet by a previous call to {@link #getNewMessages()}).
	 */
	public static synchronized List<Message> getNewMessages() {
		List<Message> result = List.copyOf(messagesQueue);
		messagesQueue.clear();
		return result;
	}

	/**
	 * Simple command line interface
	 */
	public static void main(String[] args) throws InterruptedException, IOException {
		if (args.length < 1) {
			System.err.println("Expected script argument");
			System.exit(1);
		}
		String script = args[0];
		Map<String,String> options = new HashMap<>();
		for (int i = 1; i < args.length; i += 2) {
			if (!args[i].startsWith("--")) {
				System.err.println("Expected option name argument, got " + args[i]);
				System.exit(1);
			}
			String option = args[i].substring(2);
			if (i + 1 >= args.length) {
				System.err.println("Expected option value argument");
				System.exit(1);
			}
			options.put(option, args[i + 1]);
		}
		Job job = null;
		try {
			job = SimpleAPI.startJob(script, options);
		} catch (IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		} catch (FileNotFoundException e) {
			System.err.println("File does not exist: " + e.getMessage());
			System.exit(1);
		}
		while (true) {
			for (Message m : SimpleAPI.getNewMessages()) {
				System.err.println(m.getText());
			}
			switch (job.getStatus()) {
			case SUCCESS:
			case FAIL:
			case ERROR:
				System.exit(0);
			case IDLE:
			case RUNNING:
			default:
				Thread.sleep(1000);
			}
		}
	}
}
