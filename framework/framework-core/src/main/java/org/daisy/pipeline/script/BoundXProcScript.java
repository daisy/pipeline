package org.daisy.pipeline.script;

import org.daisy.common.xproc.XProcInput;
import org.daisy.common.xproc.XProcOutput;

public class BoundXProcScript {
	private final XProcScript script;
	private final XProcInput input;
	private final XProcOutput output;


	/**
	 * @param script
	 * @param input
	 * @param output
	 */
	private BoundXProcScript(XProcScript script, XProcInput input, XProcOutput output) {
		this.script = script;
		this.input = input;
		this.output = output;
	}

	public static BoundXProcScript from(XProcScript script, XProcInput input, XProcOutput output){
		return new BoundXProcScript(script,input,output);
	}


	/**
	 * @return the script
	 */
	public XProcScript getScript() {
		return script;
	}

	/**
	 * @return the input
	 */
	public XProcInput getInput() {
		return input;
	}

	/**
	 * @return the output
	 */
	public XProcOutput getOutput() {
		return output;
	}

}
