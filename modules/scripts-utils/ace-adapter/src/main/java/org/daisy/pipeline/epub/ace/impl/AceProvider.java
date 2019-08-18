package org.daisy.pipeline.epub.ace.impl;

import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.util.Optional;
import javax.xml.transform.stream.StreamSource;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.shell.BinaryFinder;
import org.daisy.common.shell.CommandRunner;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import static org.daisy.common.xproc.file.FileUtils.cResultDocument;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Accessibility Checker for EPUB (Ace) step provider</p>
 *
 * <p>This class provides a step that validates an EPUB using a system-wide install of the Ace tool
 * if it is installed.</p>
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
	 * Retrieve the Ace executable path
	 *
	 * @throws RuntimeException if Ace is not found on the system
	 */
	@Activate
	public void init() {
		Optional<String> lpath = BinaryFinder.find("ace");
		if (lpath.isPresent())
			aceProgram = new File(lpath.get());
		else
			throw new RuntimeException("Ace was not found on your system");
	}

	public static class AceStep extends DefaultStep {

		private final static Logger mLogger = LoggerFactory.getLogger(AceStep.class);

		private static final QName _epubFile = new QName("epub");
		private static final QName _tempDir = new QName("temp-dir");
		private static final QName _lang = new QName("lang");

		private WritablePipe htmlReport = null;
		private WritablePipe jsonReport = null;

		private AceStep(XProcRuntime runtime, XAtomicStep step) {
			super(runtime, step);
		}

		@Override
		public void setOutput(String port, WritablePipe pipe) {
			if ("html-report-uri".equals(port)) {
				htmlReport = pipe;
			} else { // json-report-uri
				jsonReport = pipe;
			}
		}

		@Override
		public void reset() {
			htmlReport.resetWriter();
			jsonReport.resetWriter();
		}

		@Override
		public void run() throws SaxonApiException {
			super.run();
			try {
				URI epubURI = new URI(getOption(_epubFile).getString());
				File epubFile = new File(epubURI);

				// Note that this should normally never happen because init() would have thrown an Exception
				if (aceProgram == null) throw new Exception("Ace was not found on your system");

				// Output where the Ace reports (report.html and report.json) and unzipped epub will be stored
				File tempDir;
				if (getOption(_tempDir).getString().equals(""))
					tempDir = Files.createTempDirectory("ace-").toFile();
				else
					tempDir = new File(new URI(getOption(_tempDir).getString()));

				String language = getOption(_lang).getString();

				String[] cmd = new String[] {
					aceProgram.getAbsolutePath(),
					"-o", tempDir.getAbsolutePath(),
					"-t", tempDir.getAbsolutePath(),
					"-l", language.equals("") ? "en" : language,
					epubFile.getCanonicalPath()
				};

				// FIXME: the command runner would raise an IOException on Windows if consumeOutput() is not used
				// (the CommandRunner pipes the process output to the file '/dev/null' by default)
				new CommandRunner(cmd)
					.consumeOutput(stream -> {})
					.consumeError(mLogger)
					.run();

				File htmlReportFile = new File(tempDir.getAbsolutePath() + File.separator + "report.html");
				File jsonReportFile = new File(tempDir.getAbsolutePath() + File.separator + "report.json");

				// write the result uris in c:result documents
				writeCResult(htmlReport, htmlReportFile.toURI());
				writeCResult(jsonReport, jsonReportFile.toURI());

			} catch (Throwable e) {
				logger.error("Exception raised while checking the epub with Ace: " + e.getMessage());
				e.printStackTrace();
				throw new XProcException(step.getNode(), e);
			}
		}

		private void writeCResult(WritablePipe port, URI uri) throws SaxonApiException {
			port.write(runtime.getProcessor().newDocumentBuilder().build(new StreamSource(cResultDocument(uri.toString()))));
		}
	}
}
