package org.daisy.pipeline.job.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.xml.transform.Result;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.job.Index;
import org.daisy.pipeline.job.JobContext;
import org.daisy.pipeline.job.JobResult;
import org.daisy.pipeline.job.JobResultSet;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.script.XProcScript;

import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class JobResultSetBuilder {


	public static JobResultSet newResultSet(JobContext ctxt,URIMapper mapper){
		JobResultSet.Builder builder = new JobResultSet.Builder();
		//go through the outputs write them add the uri's to the 
		//result object

		JobResultSetBuilder.collectOutputs(ctxt.getScript(),ctxt.getOutputs(), mapper,builder);

		//go through the output options and add them, this is a bit more tricky 
		//as you have to check if the files exist
		//if your working with an anyURIDir then scan the directory to 
		//get all the files inside.
		JobResultSetBuilder.collectOptions(ctxt.getScript(), ctxt.getInputs(), mapper,builder);
		return builder.build();
	}

	static synchronized void collectOutputs(XProcScript script,XProcOutput outputs,URIMapper mapper,JobResultSet.Builder builder){

		for (XProcPortInfo info: script.getXProcPipelineInfo().getOutputPorts()){

			Supplier<Result> prov= outputs.getResultProvider(info.getName());
			
			if(prov==null)
				continue;
			String mediaType=script.getPortMetadata(info.getName()).getMediaType();
			List<JobResult> results=null;
			if(prov instanceof DynamicResultProvider){
				results=buildJobResult((DynamicResultProvider) prov,mapper,mediaType);
			}else{
				results=buildJobResult(prov,mapper,mediaType);
			}
			builder.addResults(info.getName(),results);
		}

	}

	//Non dymamic just one result will be returned in fact
	static List<JobResult> buildJobResult(Supplier<Result> provider,URIMapper mapper,String mediaType){
		List<JobResult> jobs= Lists.newLinkedList();
		URI path=URI.create(provider.get().getSystemId());
		jobs.add(singleResult(path,mapper,mediaType));
		return jobs;
	}
	static List<JobResult> buildJobResult(DynamicResultProvider provider,URIMapper mapper,String mediaType){
		List<JobResult> jobs= Lists.newLinkedList();
		for( Result res: provider.providedResults()){
			URI path=URI.create(res.getSystemId());
			jobs.add(singleResult(path,mapper,mediaType));
		}
		return jobs;

	}

	static JobResult singleResult(URI path, URIMapper mapper,String mediaType){
		return new JobResult.Builder().withPath(path)
			.withIdx(new Index(mapper.unmapOutput(path).toString()))
			.withMediaType(mediaType).build();
	};

	static void collectOptions(XProcScript script,XProcInput inputs,URIMapper mapper,JobResultSet.Builder builder){
		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());
		//options which are translatable and outputs
		Collection<XProcOptionInfo> options= Collections2.filter(optionInfos,URITranslatorHelper.getResultOptionsFilter(script));
		for(XProcOptionInfo option: options){
			if(inputs.getOptions().get(option.getName())==null)
				continue;
			String mediaType=script.getOptionMetadata(option.getName()).getMediaType();
			//is file
			if(XProcDecorator.TranslatableOption.ANY_FILE_URI.getName().equals(script.getOptionMetadata(option.getName()).getType())){
				URI path=URI.create(inputs.getOptions().get(option.getName()));
				JobResult result= singleResult(path,mapper,mediaType);
				builder.addResult(option.getName(),result);
				//is dir
			}else if (XProcDecorator.TranslatableOption.ANY_DIR_URI.getName().equals(script.getOptionMetadata(option.getName()).getType())){
				String dir=inputs.getOptions().get(option.getName());
				List<URI> ls=IOHelper.treeFileList(URI.create(dir));
				for(URI path: ls){
					JobResult result= singleResult(path,mapper,mediaType);
					builder.addResult(option.getName(),result);
				}
			}
		}

	}
	
}
