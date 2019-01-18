package org.daisy.pipeline.job.impl;

import java.util.Collections;
import java.util.LinkedList;

import javax.xml.transform.Result;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOptionInfo;
import org.daisy.common.xproc.XProcPortInfo;
import org.daisy.pipeline.script.XProcOptionMetadata;
import org.daisy.pipeline.script.XProcScript;

import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.base.Supplier;

public final class URITranslatorHelper   {

	public static final boolean notEmpty(String value){
		return value != null && !value.isEmpty() && ! value.equals("''")
			&& !value.equals("\"\"");
	}

	
	public static final Predicate<XProcPortInfo> getNullPortFilter(final XProcInput input){
		return  new Predicate<XProcPortInfo>(){
			public boolean apply(XProcPortInfo portInfo){
				return input.getInputs(portInfo.getName()) != null;
			}
		};
	}



	/** Tranlatable options are those marked as anyFileURI or anyDirURI 
	 */
	public static final Predicate<XProcOptionInfo> getTranslatableOptionFilter(final XProcScript script){
		return  new Predicate<XProcOptionInfo>(){
			public boolean apply(XProcOptionInfo optionInfo){
				return (XProcDecorator.TranslatableOption.contains(script.getOptionMetadata(
					optionInfo.getName()).getType()));
			}
		};
	}

	public static final Predicate<XProcOptionInfo> getOutputOptionFilter(final XProcScript script){
		return  new Predicate<XProcOptionInfo>(){
			public boolean apply(XProcOptionInfo optionInfo){
				return script.getOptionMetadata(optionInfo.getName())
					.getOutput() != XProcOptionMetadata.Output.NA;
			}
		};
	}

	public static final Predicate<XProcOptionInfo> getTemporalOptionsFilter(final XProcScript script){
		return  new Predicate<XProcOptionInfo>(){
			public boolean apply(XProcOptionInfo optionInfo){
				return script.getOptionMetadata(optionInfo.getName())
					.getOutput() == XProcOptionMetadata.Output.TEMP;
			}
		};
	}
	public static final Predicate<XProcOptionInfo> getTranslatableOutputOptionsFilter(final XProcScript script){
		return Predicates.and(URITranslatorHelper.getTranslatableOptionFilter(script),
					URITranslatorHelper.getOutputOptionFilter(script));

	}

	public static final Predicate<XProcOptionInfo> getResultOptionsFilter(final XProcScript script){
		return Predicates.and(URITranslatorHelper.getTranslatableOptionFilter(script),
					URITranslatorHelper.getOutputOptionFilter(script),Predicates.not(URITranslatorHelper.getTemporalOptionsFilter(script)));

	}
	public static final Predicate<XProcOptionInfo> getTranslatableInputOptionsFilter(final XProcScript script){
		return Predicates.and(URITranslatorHelper.getTranslatableOptionFilter(script),
				Predicates.not(URITranslatorHelper.getOutputOptionFilter(script)));

	}
	public static final String generateOptionOutput(XProcOptionInfo option,XProcScript script){
		return URITranslatorHelper.generateOutput(
					option.getName().toString(),
					script.getOptionMetadata(option.getName())
					.getType(),
					script.getOptionMetadata(option.getName())
					.getMediaType());
	}
	/**
	 * Returns the prefix (unmmaped) at index 0 and suffix at index 1 for the a dynamic result provider based on the provider and 
	 * the port info
	 * TODO: At some point it would be nice to generate the names based on the mime-type, ask jostein where 
	 * he got the list of mime-types for the webui
	 */
	public static final String[] getDynamicResultProviderParts(String name, String systemId, String mimetype){
		String parts[]=null;
		//on the result/result.xml way
		if (systemId==null || systemId.isEmpty()){
			parts= new String[]{String.format("%s/%s",name,name),".xml"};
		//directory-> dir/name, .xml
		//the first part is the last char of the sysId
		}else if(systemId.charAt(systemId.length()-1)=='/'){
			parts= new String[]{String.format("%s%s",systemId,name),".xml"};
		//file name/name, (".???"|"")
		}else{
			String ext="";
			String path=systemId;
			int idx;

			//get the extension if there is one
			if((idx=path.lastIndexOf('.'))>-1)
				ext=path.substring(idx);

			// the path had a dot in the middle, t'is not an extension
			if(ext.indexOf('/')>0)
				ext="";
				
			//there's extension so we divide
			//lastIndexOf(.) will never be -1
			if(!ext.isEmpty())
				path=path.substring(0,path.lastIndexOf('.'));

			parts= new String[]{path,ext};
		}

		
		return parts;	
	}

	/**
	 * Generate output names  based on its media type.
	 *
	 * @param name the name
	 * @param type the type
	 * @param mediaType the media type
	 * @return the string
	 */
	public static String generateOutput(String name, String type, String mediaType) {
		if(type.equals(XProcDecorator.TranslatableOption.ANY_DIR_URI.getName())){
			return name+'/';
		}else{
			return name+".xml";
		}

	}


	public static String implode(LinkedList<String> optionItems,
			XProcOptionInfo option, XProcScript script) {
		String separator=script.getOptionMetadata(option.getName()).getSeparator();
		return Joiner.on(separator).skipNulls().join(optionItems);
	}


	public static Iterable<String> explode(String optionString, XProcOptionInfo option,
			XProcScript script) {
		if (optionString==null)
			return Collections.emptyList();
		String separator=script.getOptionMetadata(option.getName()).getSeparator();
	
		return Splitter.on(separator).split(optionString);
	}
	
}
