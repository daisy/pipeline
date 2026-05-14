package org.daisy.pipeline.file.calabash.impl;


import net.sf.saxon.s9api.QName;

import com.xmlcalabash.core.XProcConfiguration;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.runtime.XAtomicStep;

public class Mocks  {

	public static XProcRuntime getXProcRuntime(){
		XProcRuntime runtime=new XProcRuntime(new XProcConfiguration());
		return runtime;
	}

	public static XAtomicStep getAtomicStep( XProcRuntime runtime){
		Step step = new Step(runtime,null,new QName("","qname"),"name");		
		return new XAtomicStep(runtime,step,null) {

		    public QName getType() {
		        return new QName("","qname");
		    }
		};
	}

	static class MyConf extends XProcConfiguration{
		
	}
}
