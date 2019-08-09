package org.daisy.pipeline.epub;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStepProvider;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;

import com.xmlcalabash.runtime.XAtomicStep;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.daisy.common.shell.CommandRunner;
import org.daisy.common.shell.BinaryFinder;

import java.util.Optional;


/**
 * Accessibilty Checker for Epub tools validator provider : 
 * This class provide a step that tries to validate an epub using
 * a system-wide install of the ace tools if it is installed.
 */
@Component(
	name = "pxi:ace",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}ace" }
)
public class AceProvider implements XProcStepProvider {
	
	
	
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new AceStep(runtime, step);
	}
	
	private static File aceProgram = null;
	
	/**
	 * Initialize the provider :
	 * Retrieve the Ace executable path
	 * @throws RuntimeException if Ace is not found on the system
	 */
	@Activate
	public void init() {
		Optional<String> lpath = BinaryFinder.find("ace");
		if(lpath.isPresent()) {
			aceProgram = new File(lpath.get());
		} else throw new RuntimeException("ACE was not found on your system");
	}

	public static class AceStep extends DefaultStep {
		
		private final static Logger mLogger = LoggerFactory.getLogger(AceStep.class);
		
		private static final QName _epubFile = new QName("epub");
		private static final QName _tempDir = new QName("temp-dir");
		private static final QName _lang = new QName("lang");
		
		private WritablePipe htmlReportURIport = null;
		private WritablePipe jsonReportData = null;
		
		private AceStep(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}

		@Override
		public void setOutput(String port, WritablePipe pipe) {
			if ("json-report".equals(port)) {
				jsonReportData = pipe;
			} else { // default output to htmlReportURI
				htmlReportURIport = pipe;
			}
		}

		@Override
		public void reset() {
			htmlReportURIport.resetWriter();
			jsonReportData.resetWriter();
		}

		@Override
		public void run() throws SaxonApiException {
			super.run();
			try {
				URI epubURI = new URI(getOption(_epubFile).getString());
				File epubFile = new File(epubURI);
				
				
				if(aceProgram == null) throw new Exception("(running pxi:ace) ACE was not found in the PATH of your system");

				String path = epubFile.getCanonicalPath();
				
				// Output where the ace reports (report.html and report.json) and unzipped epub will be stored
				File tempDir;
				if(getOption(_tempDir).getString().equals("")) {
					tempDir = Files.createTempDirectory("ace-").toFile();
				}else tempDir = new File(new URI(getOption(_tempDir).getString())); 
				
				System.out.println(getOption(_tempDir).getString());
				System.out.println(tempDir.getAbsolutePath());
				
				String language = getOption(_lang).getString();
				
				String[] cmd = new String[] {
						aceProgram.getAbsolutePath(),
						"-o", tempDir.getAbsolutePath(),
						"-t", tempDir.getAbsolutePath(),
						"-l", language.equals("") ? "en" : language,
						path
				};
				
				// NOTE : the command runner would raised an IOException on windows if the consumeOutput is not used
				// (the CommandRunner pipes the process output to the file '/dev/null' by default)
				new CommandRunner(cmd)
						.consumeOutput(stream -> {})
						.consumeError(mLogger)
						.run();
				
				
				File htmlReport = new File(tempDir.getAbsolutePath() + File.separator + "report.html");
				File jsonReport = new File(tempDir.getAbsolutePath() + File.separator + "report.json");
				
				// write the results uri in temps document, and pipe them to the output ports as xml documents
				// (reading the html report does not work here : the ace report is not strictly valid, 
				// some non closed link tags are blocking the load by saxon
				File tempFile = File.createTempFile("html-uri-",".xml");
				PrintWriter tempWriter = new PrintWriter(tempFile,"UTF-8");
				tempWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				tempWriter.println("<c:result xmlns:c=\"http://www.w3.org/ns/xproc-step\">" + htmlReport.toURI().toString() + "</c:result>");
				tempWriter.close();
				htmlReportURIport.write(runtime.getProcessor().newDocumentBuilder().build(tempFile));
				tempFile.delete();

				String jsonContent = new String(Files.readAllBytes(Paths.get(jsonReport.getAbsolutePath())),StandardCharsets.UTF_8)
						.replaceAll("<", "&#x3e;")
						.replaceAll(">", "&#x3c;")
						.replaceAll("&", "&#x26;");
				
				if (!jsonContent.isBlank()) {
					tempFile = File.createTempFile("json-data",".xml");
					tempWriter = new PrintWriter(tempFile,"UTF-8");
					tempWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
					// Wrapping (and replacing by entities the <, > and & characters in) the json data in a c:data tag
					tempWriter.println("<c:data xmlns:c=\"http://www.w3.org/ns/xproc-step\" content-type=\"application/json\" enconding=\"UTF-8\" >" + 
							new String(Files.readAllBytes(Paths.get(jsonReport.getAbsolutePath())),StandardCharsets.UTF_8)
								.replaceAll("<", "&#x3e;")
								.replaceAll(">", "&#x3c;")
								.replaceAll("&", "&#x26;") + 
							"</c:data>");
					tempWriter.close();
					jsonReportData.write(runtime.getProcessor().newDocumentBuilder().build(tempFile));
					tempFile.delete();
					
				}
				
			} catch (Throwable e) {
				logger.error("Exception raised while checking the epub with ACE : " + e.getMessage());
				e.printStackTrace();
				throw new XProcException(step.getNode(), e);
			}
		}
	}
	
	
	
}
