package org.daisy.pipeline.persistence.impl.job;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.daisy.common.messaging.MessageAccessor;
import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.AbstractJob;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobMonitor;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.StatusNotifier;
import org.daisy.pipeline.job.impl.IOHelper;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.ScriptService;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;

public class Mocks   {

	public static final String scriptUri= "http://daisy.com";
	public static final String scriptId= "foo-to-bar";
	public static final String testLogFile="http://daisy.com/log.txt";
	public static final String file1 = "f1.xml";
	public static final String file2 = "f2.xml";
	public static final String opt1Name = "opt1";
	public static final String opt2Name = "opt2";
	public static final String value1 = "value1";
	public static final File result1;
	public static final File result2;
	public static final File out = new File("/tmp/out/");
	public static final String portResult="res"; 

	static {
		try {
			result1 = File.createTempFile("res", null);
			result2 = File.createTempFile("res", null);
			result1.deleteOnExit();
			result2.deleteOnExit();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static class DummyScriptService extends ScriptRegistry {

		private XProcScript script;

		/**
		 * Constructs a new instance.
		 *
		 * @param script The script for this instance.
		 */
		public DummyScriptService(XProcScript script) {
			this.script = script;
		}

		@Override
		public XProcScriptService getScript(String name) {
			return new XProcScriptService() {
				@Override
				public XProcScript load() {
					return script;
				}
			};
		}

		@Override
		public Iterable<ScriptService<?>> getScripts() {
			return null;
		}
	}


	static class SimpleSourceProvider implements Source {
		String sysId;

		/**
		 * Constructs a new instance.
		 *
		 * @param sysId The sysId for this instance.
		 */
		public SimpleSourceProvider(String sysId) {
			this.sysId = sysId;
		}

		@Override
		public String getSystemId() {
			return this.sysId;
		}

		@Override
		public void setSystemId(String systemId) {
			
		}
	}

	public static XProcScript buildScript(){
		XProcScript.Builder builder = new XProcScript.Builder(Mocks.scriptId, "", URI.create(Mocks.scriptUri),
		                                                      null, null, null);
		builder = builder.withInputPort(XProcPortInfo.newInputPort("source", true, false, true),
		                                new XProcPortMetadata("", "", ""));
		builder = builder.withOutputPort(XProcPortInfo.newOutputPort(portResult, true, true),
		                                 new XProcPortMetadata("", "", ""));
		builder = builder.withOutputPort(XProcPortInfo.newOutputPort(opt1Name, true, false),
		                                 new XProcPortMetadata("", "", ""));
		builder = builder.withOption(XProcOptionInfo.newOption(new QName(opt2Name), false, ""),
		                             new XProcOptionMetadata(null, null, null, null));
		return builder.build();
	}

	public static AbstractJob buildJob() {
		return buildJob(Priority.MEDIUM);
	}

	public static AbstractJob buildJob(File contextDir) {
		return new AbstractJob(buildContext(contextDir), Priority.MEDIUM, true) {};
	}

	public static AbstractJob buildJob(Priority priority) {
		return new AbstractJob(buildContext(), priority, true) {};
	}

	public static AbstractJob buildJob(Priority priority, File contextDir) {
		return new AbstractJob(buildContext(contextDir), priority, true) {};
	}

	public static AbstractJob buildJob(Client client) {
		return new AbstractJob(buildContext(client), Priority.MEDIUM, true) {};
	}

	public static AbstractJob buildJob(Client client, File contextDir) {
		return new AbstractJob(buildContext(client, null, contextDir), Priority.MEDIUM, true) {};
	}

	public static AbstractJob buildJob(Client client, JobBatchId batchId) {
		return new AbstractJob(buildContext(client, batchId), Priority.MEDIUM, true) {};
	}

	public static AbstractJob buildJob(Client client, JobBatchId batchId, File contextDir) {
		return new AbstractJob(buildContext(client, batchId, contextDir), Priority.MEDIUM, true) {};
	}

	public static AbstractJobContext buildContext() {
		return buildContext(null, null);
	}

	public static AbstractJobContext buildContext(File contextDir) {
		return buildContext(null, null, contextDir);
	}

	public static AbstractJobContext buildContext(Client client) {
		return buildContext(client, null);
	}

	public static AbstractJobContext buildContext(Client client, JobBatchId batchId) {
		return buildContext(client, batchId, null);
	}

	public static AbstractJobContext buildContext(Client client, JobBatchId batchId, File contextDir) {
		final Script script = Mocks.buildScript();
		JobResources resources = new JobResources() {
				public Iterable<String> getNames() {
					return Lists.newArrayList(file1, file2);
				}
				public Supplier<InputStream> getResource(String name) {
					return () -> new ByteArrayInputStream("foo".getBytes());
				}
			};
		ScriptInput input;
		try {
			input = new ScriptInput.Builder(resources).withInput("source", new Mocks.SimpleSourceProvider(file1))
		                                              .withInput("source", new Mocks.SimpleSourceProvider(file2))
		                                              .withOption(opt2Name, value1)
		                                              .build();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		final JobId id = JobIdFactory.newId();
		if (contextDir != null)
			try {
				input = input.storeToDisk(contextDir);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		final JobResultSet rSet = new JobResultSet.Builder(script)
		                                          .addResult(portResult, result1.getName(), result1, null)
		                                          .addResult(opt1Name, result2.getName(), result2, null)
		                                          .build();
                //add to the db
                if ( client ==null){
                        client=new PersistentClient("Client_"+Math.random(),"b",Role.ADMIN,"a@a",Priority.LOW);
                        DatabaseProvider.getDatabase().addObject(client);
                }
		//inception!
		return new MyHiddenContext(rSet,script,input,out,client,id,batchId);
	}

	static class MyHiddenContext extends AbstractJobContext{
			public MyHiddenContext(JobResultSet set, Script script, ScriptInput input, File resultDir, Client client, JobId id, JobBatchId batchId){
				super();
				this.client = client;
				this.id = id;
				this.logFile = URI.create("/tmp/job.log");
				this.batchId = batchId;
				this.niceName = "hidden";
				this.script = script;
				this.input = input;
				this.resultDir = resultDir;
				this.results = set;
				this.monitor = new JobMonitor() {
						@Override
						public MessageAccessor getMessageAccessor() {
							return null;
						}
						@Override
						public StatusNotifier getStatusUpdates() {
							return null;
						}
					};
			}
		};

}
