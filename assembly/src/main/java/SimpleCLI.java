import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.daisy.common.spi.CreateOnStart;
import org.daisy.common.spi.ServiceLoader;
import org.daisy.common.transform.LazySaxResultProvider;
import org.daisy.common.transform.LazySaxSourceProvider;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
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

@Component(
	name = "SimpleCLI",
	immediate = true
)
public class SimpleCLI {

	private ScriptRegistry scriptRegistry;
	private WebserviceStorage webserviceStorage;
	private JobManagerFactory jobManagerFactory;

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

	private void run(String scriptName, Map<String,String> options) throws InterruptedException, IOException {
		XProcScriptService scriptService = scriptRegistry.getScript(scriptName);
		if (scriptService == null)
			throw new IllegalStateException();
		XProcScript script = scriptService.load();
		File cwd = new File(System.getProperty("org.daisy.pipeline.cli.cwd", "."));
		XProcInput.Builder inputs = new XProcInput.Builder(); {
			for (XProcPortInfo port : script.getXProcPipelineInfo().getInputPorts()) {
				if (options.containsKey(port.getName())) {
					File file = new File(options.remove(port.getName()));
					if (!file.isAbsolute())
						file = new File(cwd, file.getPath()).getCanonicalFile();
					if (!file.exists()) {
						System.err.println("File does not exist: " + file);
						System.exit(1);
					}
					inputs.withInput(port.getName(), new LazySaxSourceProvider(file.getPath()));
				} else if (script.getPortMetadata(port.getName()).isRequired()) {
					System.err.println("Required option --" + port.getName() + " missing");
					System.exit(1);
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
								System.err.println("File exists: " + file);
								System.exit(1);
							}
						} else {
							if (!file.exists()) {
								System.err.println("File does not exist: " + file);
								System.exit(1);
							}
						}
						value = file.getPath();
					} else if ("anyDirURI".equals(type)) {
						File dir = new File(value);
						if (!dir.isAbsolute())
							dir = new File(cwd, dir.getPath()).getCanonicalFile();
						if (dir.exists() && !dir.isDirectory()) {
							System.err.println("Not a directory: " + dir);
							System.exit(1);
						}
						if (metadata.getOutput() == XProcOptionMetadata.Output.RESULT) {
							if (dir.exists() && dir.list().length > 0) {
								System.err.println("Directory is not empty: " + dir);
								System.exit(1);
							}
						} else {
							if (!dir.exists()) {
								System.err.println("Directory does not exist: " + dir);
								System.exit(1);
							}
						}
						value = dir.getPath();
					}
					inputs.withOption(option.getName(), value);
				} else if (option.isRequired() && metadata.getOutput() != XProcOptionMetadata.Output.RESULT) {
					System.err.println("Required option --" + option.getName() + " missing");
					System.exit(1);
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
								System.err.println("Directory is not empty: " + file);
								System.exit(1);
							}
						} else if (file.exists()) {
							System.err.println("Not a directory: " + file);
							System.exit(1);
						}
						filePath = file.getPath() + "/";
					} else {
						if (file.exists()) {
							System.err.println("File exists: " + file);
							System.exit(1);
						}
						filePath = file.getPath();
					}
					outputs.withOutput(port.getName(), new LazySaxResultProvider(filePath));
				}
			}
		}
		for (String unknown : options.keySet()) {
			System.err.println("Unknown option: " + unknown);
			System.exit(1);
		}
		BoundXProcScript boundScript = BoundXProcScript.from(script, inputs.build(), outputs.build());
		Client client = webserviceStorage.getClientStorage().defaultClient();
		JobManager jobManager = jobManagerFactory.createFor(client);
		Job job = jobManager.newJob(boundScript).isMapping(true).build().get();
		while (true) {
			switch (job.getStatus()) {
			case SUCCESS:
			case FAIL:
			case ERROR:
				return;
			case IDLE:
			case RUNNING:
			default:
				Thread.sleep(1000);
			}
		}
	}

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
		SimpleCLI cli = null;
		for (CreateOnStart o : ServiceLoader.load(CreateOnStart.class))
			if (cli == null && o instanceof SimpleCLI)
				cli = (SimpleCLI)o;
		if (cli == null)
			throw new IllegalStateException();
		cli.run(script, options);
		System.exit(0);
	}
}
