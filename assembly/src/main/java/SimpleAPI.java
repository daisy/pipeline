import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.daisy.common.messaging.Message;
import org.daisy.common.messaging.Message.Level;
import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.spi.CreateOnStart;
import org.daisy.common.spi.ServiceLoader;
import org.daisy.pipeline.job.Job;
import org.daisy.pipeline.job.JobFactory;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.script.BoundScript;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * A simplified Java API consisting of a {@link #startJob()} method that starts a job based on a
 * script name and a list of options, and {@link #getNewMessages()} and {@link #getLastJobStatus()}
 * methods. It is used to build a simple Java CLI (see the {@link #main()} method). The simplified
 * API also makes it easier to bridge with other programming languages using JNI.
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

	private void _startJob(String scriptName, Map<String,? extends Iterable<String>> options) throws IllegalArgumentException, FileNotFoundException {
		ScriptService<?> scriptService = scriptRegistry.getScript(scriptName);
		if (scriptService == null)
			throw new IllegalArgumentException(scriptName + " script not found");
		Script script = scriptService.load();
		File fileBase = new File(System.getProperty("org.daisy.pipeline.cli.cwd", "."));
		CommandLineJobParser parser = new CommandLineJobParser(script, fileBase);
		for (Map.Entry<String,? extends Iterable<String>> e : options.entrySet())
			for (String value : e.getValue())
				parser.withArgument(e.getKey(), value);
		CommandLineJob job = parser.createJob(jobFactory);
		MessageAccessor accessor = job.getMonitor().getMessageAccessor();
		accessor.listen(
			num -> {
				consumeMessage(accessor, num);
			}
		);
		job.getMonitor().getStatusUpdates().listen(s -> updateJobStatus(s));
		new Thread(job).start();
	}

	public static void startJob(String scriptName, Map<String,? extends Iterable<String>> options) throws IllegalArgumentException, FileNotFoundException {
		getInstance()._startJob(scriptName, options);
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

	private static Job.Status lastJobStatus = null;
	private static synchronized void updateJobStatus(Job.Status status) {
		lastJobStatus = status;
	}
	private static List<Message> messagesQueue = new ArrayList<>();
	private static int lastMessage = -1;
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

	public static synchronized Job.Status getLastJobStatus() {
		return lastJobStatus;
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
		Map<String,List<String>> options = new HashMap<>();
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
			List<String> list = options.get(option);
			if (list == null) {
				list = new ArrayList<>();
				options.put(option, list);
			}
			list.add(args[i + 1]);
		}
		try {
			SimpleAPI.startJob(script, options);
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
			switch (SimpleAPI.getLastJobStatus()) {
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

	private static class CommandLineJobParser {

		private final Script script;
		private final File fileBase;
		private final BoundScript.Builder builder;
		private final Map<String,URI> resultLocations;

		public CommandLineJobParser(Script script, File fileBase) {
			this.script = script;
			this.fileBase = fileBase;
			builder = new BoundScript.Builder(script);
			resultLocations = new HashMap<>();
		}

		/**
		 * Parse command line argument
		 */
		public CommandLineJobParser withArgument(String key, String value) throws IllegalArgumentException, FileNotFoundException {
			if (script.getInputPort(key) != null)
				return withInput(key, value);
			else if (script.getOption(key) != null)
				return withOption(key, value);
			else if (script.getOutputPort(key) != null)
				return withOutput(key, value);
			else
				throw new IllegalArgumentException("Unknown argument: " + key);
		}

		/**
		 * Parse command line argument as script input.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified port, or the
		 *         port does not accept a sequence of documents and multiple documents are supplied.
		 * @throws FileNotFoundException if <code>source</code> does not exist.
		 */
		private CommandLineJobParser withInput(String port, String source) throws IllegalArgumentException, FileNotFoundException {
			File file = new File(source);
			if (!file.isAbsolute()) {
				if (fileBase == null)
					throw new FileNotFoundException("File must be an absolute path, but got " + file);
				file = new File(fileBase, file.getPath());
			}
			try {
				file = file.getCanonicalFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			builder.withInput(port, file);
			return this;
		}

		/**
		 * Parse and validate command line argument as script option.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified option, the
		 *         option does not accept a sequence of values and multiple values are supplied, or
		 *         the value is not valid according to the option type.
		 * @throws FileNotFoundException if the option type is "anyFileURI" and the value can not be
		 *         resolved to a document.
		 */
		private CommandLineJobParser withOption(String name, String value) throws IllegalArgumentException, FileNotFoundException {
			ScriptOption o = script.getOption(name);
			if (o != null) {
				String type = o.getType().getId();
				if ("anyFileURI".equals(type)) {
					File file = new File(value);
					if (!file.isAbsolute()) {
						if (fileBase == null)
							throw new FileNotFoundException("File must be an absolute path, but got " + file);
						file = new File(fileBase, file.getPath());
					}
					try {
						value = file.getCanonicalFile().toURI().toString();
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				} else if ("anyDirURI".equals(type)) {
					File dir = new File(value);
					if (!dir.isAbsolute()) {
						if (fileBase == null)
							throw new FileNotFoundException("File must be an absolute path, but got " + dir);
						dir = new File(fileBase, dir.getPath());
					}
					// these checks are not done by BoundScript.Builder
					if (!dir.exists())
						throw new FileNotFoundException(dir.getPath());
					if (!dir.isDirectory())
						throw new IllegalArgumentException("Not a directory: " + dir);
					try {
						value = dir.getCanonicalFile().toURI().toString() + "/";
					} catch (IOException e) {
						throw new UncheckedIOException(e);
					}
				}
			}
			builder.withOption(name, value);
			return this;
		}

		/**
		 * Parse command line argument as script output.
		 *
		 * @throws IllegalArgumentException if the script does not have the specified port,
		 *         <code>result</code> is a non-empty directory, or exists and is not a directory.
		 */
		private CommandLineJobParser withOutput(String port, String result) throws IllegalArgumentException, FileNotFoundException {
			ScriptPort p = script.getOutputPort(port);
			if (p == null)
				throw new IllegalArgumentException(
					String.format("Output '%s' is not recognized by script '%s'", port, script.getId()));
			if (resultLocations.containsKey(port))
				throw new IllegalArgumentException(
					String.format("Output '%s' already specified", port));
			File file = new File(result);
			if (!file.isAbsolute()) {
				if (fileBase == null)
					throw new FileNotFoundException("File must be an absolute path, but got " + file);
				file = new File(fileBase, file.getPath());
			}
			try {
				file = file.getCanonicalFile();
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			if (result.endsWith("/")) {
				if (file.exists()) {
					if (!file.isDirectory())
						throw new IllegalArgumentException("Not a directory: " + file);
					else if (file.list().length > 0)
						throw new IllegalArgumentException("Directory is not empty: " + file);
				}
				resultLocations.put(port, URI.create(file.toURI() + "/"));
			} else {
				if (file.exists()) {
					if (file.isDirectory()) {
						if (file.list().length > 0)
							throw new IllegalArgumentException("Directory is not empty: " + file);
						resultLocations.put(port, URI.create(file.toURI() + "/"));
					} else {
						if (p.isSequence())
							throw new IllegalArgumentException("Not a directory: " + file);
						else
							throw new IllegalArgumentException("File exists: " + file);
					}
				} else {
					if (p.isSequence())
						resultLocations.put(port, URI.create(file.toURI() + "/"));
					else
						resultLocations.put(port, file.toURI());
				}
			}
			return this;
		}

		public CommandLineJob createJob(JobFactory factory) {
			return new CommandLineJob(factory.newJob(builder.build()).build().get(), resultLocations);
		}
	}

	/**
	 * Job with a simplified API that stores results after completion.
	 */
	public static class CommandLineJob implements Runnable, AutoCloseable {

		private final Job job;
		private final Map<String,URI> resultLocations;
		private final AtomicBoolean completed = new AtomicBoolean(false);

		private CommandLineJob(Job job, Map<String,URI> resultLocations) {
			this.job = job;
			this.resultLocations = resultLocations;
		}

		public void run() {
			job.run();
			try {
				switch (job.getStatus()) {
				case SUCCESS:
				case FAIL:
					List<File> existingFiles = new ArrayList<>();
					for (String port : job.getResults().getPorts()) {
						if (resultLocations.containsKey(port)) {
							URI u = resultLocations.get(port);
							File f = new File(u);
							if (u.toString().endsWith("/"))
								for (JobResult r : job.getResults().getResults(port)) {
									File dest = new File(f, r.strip().getIdx());
									if (dest.exists())
										existingFiles.add(dest);
									else
										writeResult(r, dest);
								}
							else
								for (JobResult r : job.getResults().getResults(port))
									if (f.exists())
										existingFiles.add(f);
									else
										writeResult(r, f);
						}
					}
					if (!existingFiles.isEmpty())
						throw new IOException("Some results could not be written: " + existingFiles);
				default:
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
			completed.set(true);
		}

		public Job.Status getStatus() {
			Job.Status s = job.getStatus();
			switch (s) {
			case SUCCESS:
			case FAIL:
			case ERROR:
				return completed.get() ? s : Job.Status.RUNNING;
			case IDLE:
			case RUNNING:
			default:
				return s;
			}
		}

		public JobMonitor getMonitor() {
			return job.getMonitor();
		}

		public void close() {
			job.close();
		}

		private void writeResult(JobResult result, File dest) throws IOException {
			dest.getParentFile().mkdirs();
			try (InputStream is = result.asStream();
			     OutputStream os = new FileOutputStream(dest)) {
				byte buff[] = new byte[1024];
				int read = 0;
				while ((read = is.read(buff)) > 0) {
					os.write(buff, 0, read);
				}
			}
		}
	}
}
