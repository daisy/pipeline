package org.daisy.pipeline.persistence.impl.job;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;

import org.daisy.common.priority.Priority;
import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcPipelineInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.clients.Client;
import org.daisy.pipeline.clients.Client.Role;
import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobBatchId;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobIdFactory;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.persistence.impl.webservice.PersistentClient;
import org.daisy.pipeline.script.BoundXProcScript;
import org.daisy.pipeline.script.ScriptRegistry;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcScriptService;

import com.google.common.base.Supplier;

public class Mocks   {
	
	public static final String scriptUri= "http://daisy.com";
	public static final String testLogFile="http://daisy.com/log.txt";
	public static final String file1="file:/tmp/f1.xml";
	public static final String file2="file:/tmp/f2.xml";
	public static final QName opt1Qname=new QName("www.daisy.org","opt1"); 
	public static final QName opt2Qname=new QName("www.daisy.org","opt2"); 
	public static final String value1="value1";
	public static final String value2="value2";
	public static final String paramPort="params";	
	public static final String qparam="param1"; 
	public static final String paramVal="pval";
	public static final URI in=URI.create("file:/tmp/in/");
	public static final URI out=URI.create("file:/tmp/out/");
	public static final String portResult="res"; 

	
	public static final JobResult res1= new JobResult.Builder().withPath(in).withIdx(value1).build();
	public static final JobResult res2= new JobResult.Builder().withPath(out).withIdx(value2).build();

	public static class DummyScriptService implements ScriptRegistry{

		protected XProcScript script;

		/**
		 * Constructs a new instance.
		 *
		 * @param script The script for this instance.
		 */
		public DummyScriptService(XProcScript script) {
			this.script = script;
		}

		@Override
		public XProcScriptService getScript(URI uri){
			return new XProcScriptService(){
				
				public XProcScript load(){
					return DummyScriptService.this.script;
				}
			};

		}

		@Override
		public XProcScriptService getScript(String name) {
			return null;
		}

		@Override
		public Iterable<XProcScriptService> getScripts() {
			return null;
		}
	}


	static class SimpleSourceProvider implements Source,Supplier<Source>{
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
		public Source get() {
			return this;
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
		XProcPortInfo pinfo= XProcPortInfo.newInputPort("source",true,true);
		XProcPortInfo ppinfo= XProcPortInfo.newParameterPort(Mocks.paramPort,true);
		XProcPipelineInfo pipelineInfo = new XProcPipelineInfo.Builder().withURI(URI.create(Mocks.scriptUri)).withPort(pinfo).withPort(ppinfo).build();
		List<String> fileset=Collections.emptyList();	
		final XProcScript script = new XProcScript(pipelineInfo, "", "", "", null, null, null,fileset,fileset); 
		return script;
	}

	public static AbstractJobContext buildContext(){  
                return buildContext(null,null);
	}

	public static AbstractJobContext buildContext(Client client){  
                return buildContext(client,null);
        }
	public static AbstractJobContext buildContext(Client client,JobBatchId batchId){  
                //new RuntimeException().printStackTrace();
		final XProcScript script = Mocks.buildScript();
		//ScriptRegistryHolder.setScriptRegistry(new Mocks.DummyScriptService(script));
		//Input setup
		final XProcInput input= new XProcInput.Builder().withInput("source",new Mocks.SimpleSourceProvider(file1)).withInput("source", new Mocks.SimpleSourceProvider(file2)).withOption(opt1Qname,value1).withOption(opt2Qname,value2).withParameter(paramPort,new QName(qparam),paramVal).build();
		
		final JobId id = JobIdFactory.newId();
		final URIMapper mapper= new URIMapper(in,out);
		final JobResultSet rSet=new JobResultSet.Builder().addResult(portResult,res1).addResult(opt1Qname,res2).build();
                //add to the db
                if ( client ==null){
                        client=new PersistentClient("Client_"+Math.random(),"b",Role.ADMIN,"a@a",Priority.LOW);
                        DatabaseProvider.getDatabase().addObject(client);
                }
		//inception!
		return new MyHiddenContext(rSet,script,input,mapper,client,id,batchId);
	}

	static class MyHiddenContext extends AbstractJobContext{
			public MyHiddenContext(JobResultSet set,XProcScript script,XProcInput input,URIMapper mapper, Client client,JobId id,JobBatchId batchId){
				super(client,id,batchId,"hidden",BoundXProcScript.from(script,input,null),mapper);
				this.setResults(set);
			}


		};

}
