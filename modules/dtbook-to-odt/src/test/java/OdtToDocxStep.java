import static org.daisy.pipeline.file.FileUtils.cResultDocument;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.transform.stream.StreamSource;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.runtime.XAtomicStep;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.spi.ActivationException;
import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.common.xproc.XProcMonitor;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OdtToDocxStep extends DefaultStep implements XProcStep {

	private static final Logger logger = LoggerFactory.getLogger(OdtToDocxStep.class);

	private static final QName _SOURCE = new QName("source");
	private static final QName _HREF = new QName("href");
	private static final QName _TEMP_DIR = new QName("temp-dir");

	private final LibreOfficeConverter libreoffice;
	private WritablePipe result = null;

	private OdtToDocxStep(XProcRuntime runtime, XAtomicStep step, LibreOfficeConverter libreoffice) {
		super(runtime, step);
		this.libreoffice = libreoffice;
	}

	@Override
	public void setOutput(String port, WritablePipe pipe) {
		result = pipe;
	}

	@Override
	public void reset() {
		result.resetWriter();
	}

	@Override
	public void run() throws SaxonApiException {
		super.run();
		try {
			File odtFile = getFileOption(_SOURCE);
			if (!odtFile.exists())
				throw new FileNotFoundException("File does not exist: " + odtFile);
			File docxFile = getFileOption(_HREF);
			if (docxFile.exists())
				throw new IllegalArgumentException("File already exists: " + docxFile);
			File tmpDir = getFileOption(_TEMP_DIR);
			File tmpDocxFile = new File(tmpDir, odtFile.getName().replaceAll("\\.odt$", ".docx"));
			boolean tmpDirEmpty = true;
			if (tmpDir.exists()) {
				if (!tmpDir.isDirectory())
					throw new IllegalArgumentException("Not a directory: " + docxFile);
				else {
					if (tmpDocxFile.exists())
						throw new IllegalArgumentException("File already exists: " + tmpDocxFile);
					tmpDirEmpty = tmpDir.listFiles((_dir, name) -> name.endsWith(".docx")).length == 0;
				}
			} else
				tmpDir.mkdirs();
			int rv = libreoffice.newCommand()
			                    .withInput(odtFile)
			                    .withOutputDir(tmpDir)
			                    .withOutputFormat(LibreOfficeConverter.Format.DOCX)
			                    .runner().run();
			if (rv != 0)
				throw new RuntimeException("LibreOffice failed with exit code " + rv);

			if (!tmpDocxFile.exists()) {
				if (tmpDirEmpty) {
					File[] result = tmpDir.listFiles((_dir, name) -> name.endsWith(".docx"));
					if (result.length == 0)
						throw new IllegalStateException("No result produced");
					else if (result.length > 1)
						throw new IllegalStateException("Could not find result");
					else
						tmpDocxFile = result[0];
				} else
					throw new IllegalStateException("Could not find result");
			}
			Files.copy(tmpDocxFile.toPath(), docxFile.toPath());
			result.write(
				runtime.getProcessor().newDocumentBuilder().build(
					new StreamSource(cResultDocument(docxFile.toURI().toString()))));
		} catch (Throwable e) {
			throw XProcStep.raiseError(e, step);
		}
	}

	private File getFileOption(QName optName) throws IllegalArgumentException {
		URI uri = null; {
			try {
				uri = URI.create(getOption(optName).getString());
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("Not a valid URI: " + getOption(optName).getString());
			}
		}
		try {
			return new File(uri);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException("Not a valid file URI: " + uri);
		}
	}

	@Component(
		name = "pxi:odt-to-docx",
		service = { XProcStepProvider.class },
		property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}odt-to-docx" }
	)
	public static class Provider implements XProcStepProvider {

		private LibreOfficeConverter libreoffice;

		@Activate
		protected void findLibreOffice() {
			try {
				libreoffice = LibreOfficeFinder.get();
			} catch (NoSuchElementException e) {
				throw new ActivationException("LibreOffice can not be found");
			}
		}

		@Override
		public XProcStep newStep(XProcRuntime runtime, XAtomicStep step, XProcMonitor monitor, Map<String,String> properties) {
			return new OdtToDocxStep(runtime, step, libreoffice);
		}
	}
}
