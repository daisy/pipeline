package org.daisy.pipeline.persistence.impl.job;

import java.io.FileNotFoundException;
import java.util.List;

import javax.xml.transform.Source;

import org.daisy.pipeline.job.AbstractJobContext;
import org.daisy.pipeline.job.JobId;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.script.Script;
import org.daisy.pipeline.script.ScriptInput;
import org.daisy.pipeline.script.ScriptOption;
import org.daisy.pipeline.script.ScriptPort;

import com.google.common.collect.Lists;

class ContextHydrator {
	//
	static void hydrateInputPorts(ScriptInput.Builder builder, List<PersistentInputPort> inputPorts) throws FileNotFoundException {
		for ( PersistentInputPort input:inputPorts){
			for (PersistentSource src:input.getSources()){
				builder.withInput(input.getName(),src);
			}
		}
	}

	static  void hydrateOptions(ScriptInput.Builder builder, List<PersistentOption> options) {
		for (PersistentOption option:options){
			builder.withOption(option.getName(), option.getValue());
		}
	}

	static void hydrateResultPorts(JobResultSet.Builder builder,List<PersistentPortResult> portResults){
		for(PersistentPortResult pRes: portResults){
			builder.addResult(pRes.getPortName(), pRes.getIdx(), pRes.getPath(), pRes.getMediaType());
		}
	}
	
	static List<PersistentInputPort> dehydrateInputPorts(JobId id, Script script, ScriptInput input) {
		List<PersistentInputPort> inputPorts = Lists.newLinkedList();
		for (ScriptPort port : script.getInputPorts()) {
			PersistentInputPort anon = new PersistentInputPort(id, port.getName());
			for (Source src : input.getInput(port.getName())) {
				anon.addSource(new PersistentSource(src.getSystemId()));
			}
			inputPorts.add(anon);
		}
		return inputPorts;
	}

	static List<PersistentOption> dehydrateOptions(JobId id, Script script, ScriptInput input) {
		List<PersistentOption> options = Lists.newLinkedList();
		for (ScriptOption option : script.getOptions()) {
			options.add(new PersistentOption(id, option.getName(), input.getOption(option.getName())));
		}
		return options;
	}

	static List<PersistentPortResult> dehydratePortResults(AbstractJobContext ctxt){
		List<PersistentPortResult> portResults= Lists.newLinkedList();
		JobResultSet rSet= ctxt.getResults();
		for(String port:rSet.getPorts()){
			for(JobResult res:rSet.getResults(port)){
				portResults.add(new PersistentPortResult(ctxt.getId(),res,port));
			}
		}
		return portResults;
	}
}
