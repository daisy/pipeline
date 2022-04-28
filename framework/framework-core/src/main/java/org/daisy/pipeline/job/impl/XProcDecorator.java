package org.daisy.pipeline.job.impl;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import javax.xml.transform.Source;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcOutput;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.job.JobResources;
import org.daisy.pipeline.job.URIMapper;
import org.daisy.pipeline.script.XProcScript;
import org.daisy.pipeline.script.XProcPortMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class XProcDecorator {
	

	private static final Logger logger = LoggerFactory.getLogger(XProcDecorator.class);

	private final XProcScript script;
	
	private final URIMapper mapper;

	/** The m generated outputs. */
	private final HashSet<String> generatedOutputs = Sets.newHashSet();
	
	enum TranslatableOption{
		ANY_DIR_URI("anyDirURI"),
		ANY_FILE_URI("anyFileURI");
		private final String name;
		TranslatableOption(String name){
			this.name=name;
		}

		public String getName() {
			return this.name;
		}
		@Override
		public String toString(){
			return this.name;
		}
		public static boolean contains(String optionType){
			//creating a map for just too elements is not going to make that much difference.
			for(TranslatableOption opt:TranslatableOption.values()){
				if(opt.getName().equals(optionType))
					return true;	
			}
			return false;
		}
	}

	/**
	 * Constructs a new instance.
	 *
	 * @param contextDir The contextDir for this instance.
	 */
	private XProcDecorator(URIMapper mapper,XProcScript script) {
		this.script=script;
		this.mapper=mapper;
	}

	public static XProcDecorator from(XProcScript script,URIMapper mapper) throws IOException {
		return XProcDecorator.from(script,mapper,null);
	}

	public static XProcDecorator from(XProcScript script,URIMapper mapper,JobResources resources) throws IOException {

		if (resources != null) {
			logger.debug("Storing the resource collection");
			IOHelper.dump(resources,mapper);
		}
		return new XProcDecorator(mapper,script);
	}


	public XProcInput decorate(XProcInput input) {
		logger.debug(String.format("Translating inputs for script :%s",script));
		XProcInput.Builder decorated = new XProcInput.Builder();
		try{
			decorateInputPorts(script, input, decorated);
			decorateOptions(script, input, decorated);
		}catch(IOException ex){
			throw new RuntimeException("Error translating inputs",ex);
		}
		return decorated.build();
	}

	/**
	*Output port 'result' use cases:
	*1. relative uri ./myoutput/file.xml is allowed and resolved to ../data/../outputs/myoutput/file.xml (file-1.xml if more)
	* in case there is no extension (myoutput/file) the outputs will be named as myoutput/file-1
	*2. ~/myscript/ this will be resolved to ../data/../outputs/myscript/result.xml  (result-1.xml if more)
	*3. No output provided will resolve to ../data/../outputs/result/result.xml → if more documents ../data/../outputs/result/result-1.xml
	 */
	public XProcOutput decorate(XProcOutput output) {
		logger.debug(String.format("Translating outputs for script :%s",script));
		//just make sure that any generated output gets a proper  	
		//place to be stored, map those ports which an uri has been provided
		//and generate a uri for those without. 
		XProcOutput.Builder builder = new XProcOutput.Builder();
		Iterable<XProcPortInfo> outputInfos=script.getXProcPipelineInfo().getOutputPorts();
		for(XProcPortInfo info:outputInfos){
			String port = info.getName();
			String mediaType = script.getPortMetadata(port).getMediaType();
			if (XProcPortMetadata.MEDIA_TYPE_STATUS_XML.equals(mediaType)) {
				builder.withOutput(port, new StatusResultProvider(port));
			} else {
				builder.withOutput(port, new DynamicResultProvider(output.getResultProvider(port), port, mediaType, mapper));
			}
		}
		return builder.build();
	}
	/**
	 * Resolve input ports.
	 *
	 * @param script the script
	 * @param input the input
	 * @param builder the builder
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	void decorateInputPorts(final XProcScript script,final  XProcInput input,
			XProcInput.Builder builder) throws IOException {

		Iterable<XProcPortInfo> inputInfos =script.getXProcPipelineInfo().getInputPorts();
		//filter those ports which are null

		//There shouldnt be any null input port because of how the XProcPipelineInfo works
		for (XProcPortInfo portInfo : inputInfos){
			//number of inputs for this port
			int inputCnt = 0;
			for (Supplier<Source> prov : input.getInputs(portInfo.getName())) {
				URI relUri = null;
				if (prov.get().getSystemId() != null) {
					try {
						relUri = URI.create(prov.get().getSystemId());
					} catch (Exception e) {
						throw new RuntimeException(
								"Error parsing uri when building the input port"
								+ portInfo.getName(), e);
					}
				} else {
					//this is the case when no zip context was provided (all comes from the xml)
					//is this case still applicable?
					relUri = URI.create(portInfo.getName() + '-' + inputCnt
							+ ".xml");
				}
				URI uri = mapper.mapInput(relUri);//contextDir.toURI().resolve(relUri);
				prov.get().setSystemId(uri.toString());
				builder.withInput(portInfo.getName(), prov);
				inputCnt++;
			}
		}
	}
	
	/**
	 * Resolve options, input/output options without value will be automaticaly assigned.
	 *
	 * @param script the script
	 * @param input the input
	 * @param resolvedInput the resolved input
	 */
	void decorateOptions(final XProcScript script , final XProcInput input,
			XProcInput.Builder resolvedInput) {

		Collection<XProcOptionInfo> optionInfos = Lists.newLinkedList(script.getXProcPipelineInfo().getOptions());

		//options which are translatable and outputs	
		Collection<XProcOptionInfo> outputs= Collections2.filter(optionInfos,URITranslatorHelper.getTranslatableOutputOptionsFilter(script));

		this.decorateOutputOptions(outputs,input,resolvedInput);
		//options which are translatable and inputs 
		Collection<XProcOptionInfo> inputs= Collections2.filter(optionInfos,URITranslatorHelper.getTranslatableInputOptionsFilter(script));
		this.decorateInputOptions(inputs,input,resolvedInput);

		//options that are to be verbatim copied 
		Collection<XProcOptionInfo> verbatims= Collections2.filter(optionInfos,Predicates.not(URITranslatorHelper.getTranslatableOptionFilter(script)));
		this.copyOptions(verbatims,input,resolvedInput);


	}

	void copyOptions(Collection<XProcOptionInfo> options,XProcInput input,XProcInput.Builder builder){
			for(XProcOptionInfo option: options){
				builder.withOption(option.getName(),input.getOptions().get(option.getName()));
			}
	}

	void decorateInputOptions(Collection<XProcOptionInfo> options,XProcInput input,XProcInput.Builder builder){
			for(XProcOptionInfo option: options){
				String optionString; {
					Object val = input.getOptions().get(option.getName());
					try {
						optionString = (String)val;
					} catch (ClassCastException e) {
						throw new RuntimeException("Expected string value for option " + option.getName() + " but got: " + val.getClass());
					}
				}
				LinkedList<String> translated= Lists.newLinkedList();
				//explode the content of the option
				for (String optionUri : URITranslatorHelper.explode(optionString,option,this.script) ){
					if(URITranslatorHelper.notEmpty(optionUri)){
						try{
							URI uri=mapper.mapInput(URI.create(optionUri));
							translated.add(uri.toString());
						}catch(IllegalArgumentException e){
							throw new RuntimeException(String.format("Error parsing uri (%s) for option %s",optionUri,option.getName()));
						}
					}
				}
				//implode the uris based on the separator
				builder.withOption(option.getName(), URITranslatorHelper.implode(translated,option,script));

			}
	}

	void decorateOutputOptions(Collection<XProcOptionInfo> options,XProcInput input,XProcInput.Builder builder){
			for(XProcOptionInfo option: options){

				String optionUri; {
					Object val = input.getOptions().get(option.getName());
					try {
						optionUri = (String)val;
					} catch (ClassCastException e) {
						throw new RuntimeException("Expected string value for option " + option.getName() + " but got: " + val.getClass());
					}
				}
				//explode the content of the option
				if(!URITranslatorHelper.notEmpty(optionUri)){
					//get the uri from select if possible otherwise generate
					optionUri=URITranslatorHelper.notEmpty(option.getSelect()) ? option.getSelect() : 
						URITranslatorHelper.generateOptionOutput(option,script); 
					//maybe it should be better to check all the outputs at the end?
					if (generatedOutputs.contains(optionUri)) {
						throw new IllegalArgumentException(
								String.format("Conflict when generating uri's a default value and option name have are equal: %s",optionUri));
					}
					generatedOutputs.add(optionUri);
				}

				try{
					URI uri=mapper.mapOutput(URI.create(optionUri));//outputDir.toURI().resolve(URI.create(optionUri));
					builder.withOption(option.getName(), uri.toString());
				}catch(IllegalArgumentException e){
					throw new RuntimeException(String.format("Error parsing uri (%s) for option %s",optionUri,option.getName()),e);
				}
			}

	}


}
