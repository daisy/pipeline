package org.daisy.maven.xproc.morgana;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import com.xml_project.morganaxproc.XProcCompiler;
import com.xml_project.morganaxproc.XProcCompiler.XProcCompilerException;
import com.xml_project.morganaxproc.XProcConfiguration;
import com.xml_project.morganaxproc.XProcEngine;
import com.xml_project.morganaxproc.XProcInput;
import com.xml_project.morganaxproc.XProcOutput;
import com.xml_project.morganaxproc.XProcPipeline;
import com.xml_project.morganaxproc.XProcResult;
import com.xml_project.morganaxproc.XProcSource;

import org.daisy.maven.xproc.api.XProcExecutionException;

import org.xml.sax.InputSource;

public class Morgana implements org.daisy.maven.xproc.api.XProcEngine {
	
	private XProcEngine engine;
	private XProcCompiler compiler;
	private File nextConfigFile;
	private File currentConfigFile;
	
	@Override
	public void setCatalog(File catalogFile) {
		throw new UnsupportedOperationException();
	}
	
	public void setConfiguration(File configFile) {
		nextConfigFile = configFile;
	}
	
	@Override
	public void run(String pipeline,
	                Map<String,List<String>> inputs,
	                Map<String,String> outputs,
	                Map<String,String> options,
	                Map<String,Map<String,String>> parameters)
			throws XProcExecutionException {
		
		try {
			if (compiler == null || nextConfigFile != currentConfigFile) {
				currentConfigFile = nextConfigFile;
				if (engine == null)
					engine = XProcEngine.newXProc();
				if (currentConfigFile != null) {
					XProcConfiguration config = XProcConfiguration.newConfiguration(currentConfigFile.toURI().toASCIIString());
					compiler = engine.newXProcCompiler(config); }
				else
					compiler = engine.newXProcCompiler(); }
			XProcPipeline xpipeline = compiler.compile(new XProcSource(pipeline));
			XProcOutput output; {
				if (inputs != null || options != null || parameters != null) {
					XProcInput input = new XProcInput();
					if (inputs != null)
						for (String port : inputs.keySet())
							for (String document : inputs.get(port))
								input.addInput(port, new XProcSource(document));
					if (options != null)
						for (String name : options.keySet())
							input.setOption(new QName(null, name), options.get(name));
					if (parameters != null)
						for (String port : parameters.keySet())
							for (String name : parameters.get(port).keySet())
								input.setParameter(port, name, null, parameters.get(port).get(name));
					output = xpipeline.run(input); }
				else
					output = xpipeline.run(); }
			if (output.wasSuccessful()) {
				if (outputs != null)
					for (String port : output.getPortNames()) {
						if (outputs.containsKey(port)) {
							File outFile = new File(new URI(outputs.get(port)).getPath());
							outFile.getParentFile().mkdirs();
							FileWriter outWriter = new FileWriter(outFile);
							XProcResult result = output.getResults(port);
							for (String document : result.getDocumentsSerialized()) {
								outWriter.write(document); }
							outWriter.flush();
							outWriter.close(); }}}
			else
				throw new XProcExecutionException("The XProc resulted in an error:\n" + output.getErrorDocumentSerialized());
		} catch (XProcCompilerException e) {
			throw new XProcExecutionException("Morgana failed to execute XProc:\n" + e.getErrorDocumentSerialized(), e);
		} catch (Exception e) {
			throw new XProcExecutionException("Morgana failed to execute XProc", e);
		}
	}
}
