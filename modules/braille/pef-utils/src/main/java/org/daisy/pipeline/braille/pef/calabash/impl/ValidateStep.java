package org.daisy.pipeline.braille.pef.calabash.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.XdmNode;

public class ValidateStep extends DefaultStep implements XProcStep {
		
	private static final QName _assert_valid = new QName("assert-valid");
	private static final QName _temp_dir = new QName("temp-dir");
	
	private ReadablePipe source = null;
	private WritablePipe result = null;
	private WritablePipe validationStatus = null;
	
	private ValidateStep(XProcRuntime runtime, XAtomicStep step) {
		super(runtime, step);
	}
	
	@Override
	public void setInput(String port, ReadablePipe pipe) {
		source = pipe;
	}
	
	@Override
	public void setOutput(String port, WritablePipe pipe) {
		if ("validation-status".equals(port)) {
			validationStatus = pipe;
		} else {
			result = pipe;
		}
	}
	
	@Override
	public void reset() {
		source.resetReader();
		result.resetWriter();
		validationStatus.resetWriter();
	}
	
	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			
			boolean assertValid = true;
			if (getOption(_assert_valid) != null)
				assertValid = getOption(_assert_valid).getBoolean();
			File tempDir = new File(new URI(getOption(_temp_dir).getString()));
			XdmNode pef = source.read();

			// FIXME: PEFValidator has moved to dotify.task.impl and should be created through the
			// streamline API.
			logger.warn("Skipping PEF validation (currently not supported)");
			boolean valid = true;
			
			// Write PEF document to file
			/*File pefFile = File.createTempFile("validate.", ".pef", tempDir);
			OutputStream pefStream = new FileOutputStream(pefFile);
			Serializer serializer = runtime.getProcessor().newSerializer();
			serializer.setOutputStream(pefStream);
			serializer.setCloseOnCompletion(true);
			serializer.serializeNode(pef);
			serializer.close();
			pefStream.close();
			
			PEFValidator validator = new PEFValidator();
			boolean valid = validator.validate(pefFile.toURI().toURL());
			InputStreamReader is = new InputStreamReader(validator.getReportStream());
			LineNumberReader lnr = new LineNumberReader(is);
			String line;
			while ((line=lnr.readLine())!=null) {
				logger.info(line);
			}*/
			
			if (assertValid && !valid)
				throw new RuntimeException("PEF document is invalid.");
			
			if (validationStatus!=null) {
				String validationXML = "<d:validation-status xmlns:d=\"http://www.daisy.org/ns/pipeline/data\" result=\""+(valid?"ok":"error")+"\"/>";
				XdmNode validationResult = runtime.getProcessor().newDocumentBuilder()
					.build(
						new StreamSource(
							new StringReader(validationXML)));
				validationStatus.write(validationResult);
			}
			result.write(pef);

		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}
	
	@Component(
		name = "pef:validate",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/2008/pef}validate" }
	)
	public static class Provider implements XProcStepProvider {
		
		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new ValidateStep(runtime, step);
		}
	}
	
	private static final Logger logger = LoggerFactory.getLogger(ValidateStep.class);
	
}
