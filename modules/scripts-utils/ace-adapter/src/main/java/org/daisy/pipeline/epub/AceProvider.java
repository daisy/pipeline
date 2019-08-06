package org.daisy.pipeline.epub;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;


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

/**
 * Accessibilty Checker for Epub tools validator provider : 
 * This class provide a step that tries to validate an epub using
 * a system-wide install of the ace tools if it is installed.
 * @author Nicolas Pavie
 *
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
	
	private static String OS = System.getProperty("os.name").toLowerCase();
	private static File aceProgram = null;
	
	/**
	 * Initialize the provider :
	 * Retrieve the ACE executable path
	 * @throws RuntimeException if ACE is not found on the system
	 */
	@Activate
	public void init() {
		// Searching for a "ace" or "ace.cmd" in directory pointed by the path env var
		String pathVariable = System.getenv("PATH");
		System.out.println((OS.contains("win") ? "OS Windows :" : "OS unix : ") +  pathVariable);
		String[] pathes = pathVariable.split(File.pathSeparator);
		for (String path : pathes) {
			if(aceProgram != null) break;
			
			File currentPath = new File(path);
			if(currentPath.listFiles() != null ) for(File currentFile : currentPath.listFiles()) {
				if(aceProgram != null) break;
				// for windows, also check .cmd ou .exe
				if( OS.contains("win") ) {
					if (currentFile.getName().equalsIgnoreCase("ace.cmd")) {
						aceProgram = currentFile;
					}
				} else if (currentFile.getName().equalsIgnoreCase("ace")){
					aceProgram = currentFile;	
				}
			}
		}
		if(aceProgram == null) throw new RuntimeException("ACE was not found in the PATH of your system");
	}

	public static class AceStep extends DefaultStep {
		
		private static final QName _epubFile = new QName("epub");
		private static final QName _tempDir = new QName("temp-dir");
		private static final QName _outputDir = new QName("output-dir");
		
		private WritablePipe outputResultNode = null;
		
		private AceStep(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}

		@Override
		public void setOutput(String port, WritablePipe pipe) {
			outputResultNode = pipe;
		}

		@Override
		public void reset() {
			outputResultNode.resetWriter();
		}

		@Override
		public void run() throws SaxonApiException {
			super.run();
			File htmlReport = null;
			try {
				URI epubURI = new URI(getOption(_epubFile).getString());
				File epubFile = new File(epubURI);
				
				if(aceProgram == null) throw new Exception("(running pxi:ace) ACE was not found in the PATH of your system");
				
				// constructing command line
				String path = epubFile.getCanonicalPath();
				ArrayList<String> arguments = new ArrayList<String>();
				arguments.add(aceProgram.getAbsolutePath());
				// Output where the ace reports (report.html and report.json) and unzipped epub will be stored
				File tempDir = Files.createTempDirectory("ace-").toFile();
				if(getOption(_tempDir).getString() != "") {
					URI tempURI = new URI(getOption(_tempDir).getString());
					tempDir = new File(tempURI);
				}
				arguments.add("-o");
				arguments.add(tempDir.getAbsolutePath());
				arguments.add("-t");
				arguments.add(tempDir.getAbsolutePath());
				arguments.add(path);
				System.out.println(arguments.toString());
				ProcessBuilder pb = new ProcessBuilder(arguments.toArray(new String[0]));
				final Process ace = pb.start();
				ace.waitFor();
				
				htmlReport = new File(tempDir.getAbsolutePath() + File.separator + "report.html");
				
				// move and rename the report to output dir
				URI outputURI = new URI(getOption(_outputDir).getString());
				File outputDir = new File(outputURI);
				String newReportName = epubFile.getName()+ "_ace_report.html";
				if(!outputDir.exists()) if(!outputDir.mkdirs()) throw new Exception("Output directory does not exists and cannot be created - " + outputDir.getAbsolutePath());
				Files.move(htmlReport.toPath(), outputDir.toPath().resolve(newReportName), StandardCopyOption.REPLACE_EXISTING);
				
				URI reportURI = outputDir.toPath().resolve(newReportName).toUri();
				
				File tempFile = File.createTempFile("result-",".xml");
				// solution : write the result in a file and build a document from it
				PrintWriter tempWriter = new PrintWriter(tempFile,"UTF-8");
				tempWriter.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
				tempWriter.println("<c:result xmlns:c=\"http://www.w3.org/ns/xproc-step\">" + reportURI.toString() + "</c:result>");
				tempWriter.close();
				// Read the resulting temp document and write it to the output xml report
				// (reading the html report does not work here : the ace report is not strictly valid, 
				// some non closed link tags are blocking the load by saxon within this code
				outputResultNode.write(runtime.getProcessor().newDocumentBuilder().build(tempFile));
			} catch (Exception e) {
				logger.error("Exception raised while checking the epub with ACE : " + e.getMessage());
				e.printStackTrace();
				throw new XProcException(step.getNode(), e);
			} finally {
			}
		}
	}
	
	
	
}
