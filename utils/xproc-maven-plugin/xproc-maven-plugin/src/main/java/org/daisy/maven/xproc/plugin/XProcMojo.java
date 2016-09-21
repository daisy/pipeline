package org.daisy.maven.xproc.plugin;

import java.io.File;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;

import org.apache.maven.plugin.AbstractMojo;

import org.daisy.maven.xproc.api.XProcEngine;
import org.daisy.maven.xproc.api.XProcExecutionException;

/**
 * Run an XProc pipeline.
 *
 * @goal xproc
 */
public class XProcMojo extends AbstractMojo {
	
	/**
	 * Path to the pipeline.
	 *
	 * @parameter
	 * @required
	 */
	private File pipeline;
	
	/**
	 * Input paths.
	 *
	 * @parameter
	 */
	private Map<String,String> inputs;
	
	/**
	 * Output paths.
	 *
	 * @parameter
	 */
	private Map<String,String> outputs;
	
	/**
	 * Options.
	 *
	 * @parameter
	 */
	private Map<String,String> options;
	
	private XProcEngine engine;
	
	public XProcMojo() {
		ServiceLoader<XProcEngine> xprocEngines = ServiceLoader.load(XProcEngine.class);
		try {
			engine = xprocEngines.iterator().next();
		} catch (NoSuchElementException e) {
			throw new RuntimeException("Could not find any XProc engines on the classpath.");
		}
	}
	
	public void execute() {
		String pipelineAsURI = asURI(pipeline).toASCIIString();
		Map<String,List<String>> inputsAsURIs = null;
		Map<String,String> outputsAsURIs = null;
		if (inputs != null) {
			inputsAsURIs = new HashMap<String,List<String>>();
			for (String port : inputs.keySet()) {
				String[] sequence = inputs.get(port).trim().split("\\s+");
				for (int i = 0; i < sequence.length; i++)
					sequence[i] = asURI(new File(sequence[i])).toASCIIString();
				inputsAsURIs.put(port, Arrays.asList(sequence)); }}
		if (outputs != null) {
			outputsAsURIs = new HashMap<String,String>();
			for (String port : outputs.keySet())
				outputsAsURIs.put(port, asURI(new File(outputs.get(port))).toASCIIString()); }
		try {
			getLog().info("Running XProc ...");
			engine.run(pipelineAsURI,
			           inputsAsURIs,
			           outputsAsURIs,
			           options,
			           null); }
		catch (XProcExecutionException e) {
			getLog().error(e.getMessage());
			Throwable cause = e.getCause();
			if (cause != null)
				getLog().debug(cause); }
	}
	
	public static URI asURI(File file) {
		try { return file.toURI(); }
		catch (SecurityException e) {
			throw new RuntimeException(e); }
	}
}
