import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Function;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.namespace.QName;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.messaging.MessageBus;
import org.daisy.common.properties.Properties;
import org.daisy.common.spi.CreateOnStart;
import org.daisy.common.spi.ServiceLoader;
import org.daisy.common.transform.LazySaxResultProvider;
import org.daisy.common.transform.LazySaxSourceProvider;
import org.daisy.common.xproc.XProcEngine;
import org.daisy.common.xproc.XProcErrorException;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPipeline;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.WebserviceStorage;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobManager;
import org.daisy.pipeline.job.JobManagerFactory;
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
 * A simplified Java API consisting of a {@link #startJob()} method
 * that accepts a script name and a list of options and returns a
 * {@link Job}, a {@link #deleteJob()} method, and a {@link
 * #getNewMessages()} method. It is used to build a simple Java CLI
 * (see the {@link #main()} method). The simplified API also makes it
 * easier to bridge with other programming languages using JNI.
 */
@Component(
	name = "SimpleAPI",
	immediate = true
)
public class SimpleAPI {

	private static Level messagesThreshold;
	static {
		try {
			messagesThreshold = Level.valueOf(
				Properties.getProperty("org.daisy.pipeline.log.level", "INFO"));
		} catch (IllegalArgumentException e) {
			messagesThreshold = Level.INFO;
		}
	}

	private XProcEngine xprocEngine;
	private ScriptRegistry scriptRegistry;
	private WebserviceStorage webserviceStorage;
	private JobManagerFactory jobManagerFactory;

	@Reference(
		name = "xproc-engine",
		unbind = "-",
		service = XProcEngine.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setXProcEngine(XProcEngine xprocEngine) {
		this.xprocEngine = xprocEngine;
	}

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
		name = "webservice-storage",
		unbind = "-",
		service = WebserviceStorage.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setWebserviceStorage(WebserviceStorage webserviceStorage) {
		this.webserviceStorage = webserviceStorage;
	}

	@Reference(
		name = "job-manager-factory",
		unbind = "-",
		service = JobManagerFactory.class,
		cardinality = ReferenceCardinality.MANDATORY,
		policy = ReferencePolicy.STATIC
	)
	public void setJobManagerFactory(JobManagerFactory jobManagerFactory) {
		this.jobManagerFactory = jobManagerFactory;
	}

	private static XProcInput buildXProcInput(Function<String,Optional<String>> inputValues,
	                                          Function<QName,Optional<Object>> optionValues,
	                                          XProcPipelineInfo pipelineInfo,
	                                          Optional<XProcScript> script) throws IOException {
		XProcInput.Builder inputs = new XProcInput.Builder();
		File cwd = new File(System.getProperty("org.daisy.pipeline.cli.cwd", "."));
		for (XProcPortInfo port : pipelineInfo.getInputPorts()) {
			Optional<String> v = inputValues.apply(port.getName());
			if (v.isPresent()) {
				File file = new File(v.get());
				if (!file.isAbsolute())
					file = new File(cwd, file.getPath()).getCanonicalFile();
				if (!file.exists()) {
					throw new FileNotFoundException(file.getPath());
				}
				inputs.withInput(port.getName(), new LazySaxSourceProvider(file.toURI().toURL().toString()));
			} else if (script.isPresent() && script.get().getPortMetadata(port.getName()).isRequired()) {
				throw new IllegalArgumentException("Required option " + port.getName() + " missing");
			}
		}
		for (XProcOptionInfo option : pipelineInfo.getOptions()) {
			XProcOptionMetadata metadata = script.map(s -> s.getOptionMetadata(option.getName())).orElse(null);
			if (metadata != null && metadata.getOutput() == XProcOptionMetadata.Output.TEMP)
				continue;
			Optional<Object> v = optionValues.apply(option.getName());
			if (v.isPresent()) {
				Object value = v.get();
				if (metadata != null) {
					try {
						value = (String)value;
					} catch (ClassCastException e) {
						throw new RuntimeException(
							"Expected string value for option " + option.getName() + " but got: " + value.getClass());
					}
					String type = metadata.getType();
					if ("anyFileURI".equals(type)) {
						File file = new File((String)value);
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
						File dir = new File((String)value);
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
				}
				inputs.withOption(option.getName(), value);
			} else if (option.isRequired() && !(metadata != null && metadata.getOutput() == XProcOptionMetadata.Output.RESULT)) {
				throw new IllegalArgumentException("Required option " + option.getName() + " missing");
			}
		}
		return inputs.build();
	}

	private static XProcOutput buildXProcOutput(Function<String,Optional<String>> outputValues,
	                                            XProcPipelineInfo pipelineInfo) throws IOException {
		XProcOutput.Builder outputs = new XProcOutput.Builder();
		File cwd = new File(System.getProperty("org.daisy.pipeline.cli.cwd", "."));
		for (XProcPortInfo port : pipelineInfo.getOutputPorts()) {
			Optional<String> v = outputValues.apply(port.getName());
			if (v.isPresent()) {
				String filePath = v.get();
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
		return outputs.build();
	}

	private void _runStep(URI step, Map<String,String> inputs, Map<String,Object> options, Map<String,String> outputs)
			throws IOException, XProcErrorException {
		XProcPipeline pipeline = xprocEngine.load(step);
		XProcInput input = buildXProcInput(port -> Optional.ofNullable(inputs.remove(port)),
		                                   option -> Optional.ofNullable(options.remove(option.toString())),
		                                   pipeline.getInfo(),
		                                   Optional.empty());
		for (String unknown : inputs.keySet()) {
			throw new IllegalArgumentException("Unknown input: " + unknown);
		}
		for (String unknown : options.keySet()) {
			throw new IllegalArgumentException("Unknown option: " + unknown);
		}
		XProcOutput output = buildXProcOutput(port -> Optional.ofNullable(outputs.remove(port)),
		                                      pipeline.getInfo());
		for (String unknown : outputs.keySet()) {
			throw new IllegalArgumentException("Unknown output: " + unknown);
		}
		MessageBus messageBus = new MessageBus("some-id", messagesThreshold);
		messageBus.listen(num -> consumeMessage(messageBus, num));
		pipeline.run(input, () -> messageBus, null).writeTo(output);
	}

	private static void runStep(URI step, Map<String,String> inputs, Map<String,Object> options, Map<String,String> outputs)
			throws IOException, XProcErrorException {
		getInstance()._runStep(step, inputs, options, outputs);
	}

	private JobManager jobManager = null;

	private Job _startJob(String scriptName, Map<String,String> options) throws IOException {
		XProcScriptService scriptService = scriptRegistry.getScript(scriptName);
		if (scriptService == null)
			throw new IllegalArgumentException(scriptName + " script not found");
		XProcScript script = scriptService.load();
		XProcInput input = buildXProcInput(port -> Optional.ofNullable(options.remove(port)),
		                                   option -> Optional.ofNullable(options.remove(option.toString())),
		                                   script.getXProcPipelineInfo(),
		                                   Optional.of(script));
		XProcOutput output = buildXProcOutput(port -> Optional.ofNullable(options.remove(port)),
		                                       script.getXProcPipelineInfo());
		for (String unknown : options.keySet()) {
			throw new IllegalArgumentException("Unknown option: " + unknown);
		}
		BoundXProcScript boundScript = BoundXProcScript.from(script, input, output);
		if (jobManager == null) {
			Client client = webserviceStorage.getClientStorage().defaultClient();
			jobManager = jobManagerFactory.createFor(client);
		}
		Job job = jobManager.newJob(boundScript).isMapping(true).build().get();
		MessageAccessor accessor = job.getContext().getMonitor().getMessageAccessor();
		accessor.listen(
			num -> {
				consumeMessage(accessor, num);
			}
		);
		return job;
	}

	public static Job startJob(String scriptName, Map<String,String> options) throws IOException {
		return getInstance()._startJob(scriptName, options);
	}

	private static void deleteJob(Job job) {
		if (INSTANCE == null || INSTANCE.jobManager == null)
			throw new IllegalStateException();
		INSTANCE.jobManager.deleteJob(job.getId());
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
