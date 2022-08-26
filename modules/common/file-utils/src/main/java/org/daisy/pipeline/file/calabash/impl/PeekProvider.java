package org.daisy.pipeline.file.calabash.impl;

import java.io.InputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Files;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStep;
import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.daisy.pipeline.file.FileUtils;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.Base64;
import com.xmlcalabash.util.TreeWriter;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "pxi:file-peek",
	service = { XProcStepProvider.class },
	property = { "type:String={http://www.daisy.org/ns/pipeline/xproc/internal}file-peek" }
)
public class PeekProvider implements XProcStepProvider {

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new Peek(runtime, step);
	}

	/**
	 * Returns a base64-encoded string, skipping the first `offset` bytes, up to a number of `length` bytes, from the file `href`.
	 */
	public static class Peek extends DefaultStep implements XProcStep {
		private static final QName _href = new QName("href");
		private static final QName _offset = new QName("offset");
		private static final QName _length = new QName("length");

		private WritablePipe result = null;

		public Peek(XProcRuntime runtime, XAtomicStep step) {
			super(runtime,step);
		}

		public void setOutput(String port, WritablePipe pipe) {
			result = pipe;
		}

		public void reset() {
			result.resetWriter();
		}

		public void run() throws SaxonApiException {
			super.run();

			int offset = getOption(_offset, 0);
			int length = getOption(_length, 0);

			RuntimeValue href = getOption(_href);
			URI sourceUri = href.getBaseURI().resolve(href.getString());
			Path file; {
				try {
					file = FileUtils.asPath(sourceUri);
				} catch (IllegalArgumentException|URISyntaxException e) {
					throw XProcStep.raiseError(new IllegalArgumentException("Illegal value for href option: " + href.getString(), e), step);
				}
			}

			if (Files.isDirectory(file)) {
				throw XProcStep.raiseError(new IllegalArgumentException("Cannot peek into file: file is a directory: " + file), step);
			}

			byte[] resultBytes = null;
			try {
				resultBytes = Peek.read(file, offset, length);
			} catch (IOException | IndexOutOfBoundsException e) {
				String fileSize; {
					try {
						fileSize = "" + Files.size(file);
					} catch (IOException ee) {
						fileSize = "?";
					}
				}
				logger.error("px:file-peek failed to read from "+file+" (offset: "+offset+", length: "+length+", filesize: "+fileSize+")", e);
				e.printStackTrace();
			}

			TreeWriter tree = new TreeWriter(runtime);
			tree.startDocument(step.getNode().getBaseURI());
			tree.addStartElement(XProcConstants.c_result);
			tree.addAttribute(new QName("content-type"), "binary/octet-stream");
			tree.addAttribute(new QName("encoding"), "base64");
			tree.startContent();
			
			if (resultBytes == null) {
				String fileSize; {
					try {
						fileSize = "" + Files.size(file);
					} catch (IOException ee) { // should not happen
						fileSize = "?";
					}
				}
				tree.addAttribute(new QName("error"), "px:file-peek failed to read from "+file+" (offset: "+offset+", length: "+length+", filesize: "+fileSize+")");
			} else {
				tree.addText(Peek.encodeBase64(resultBytes));
			}

			tree.addEndElement();
			tree.endDocument();

			result.write(tree.getResult());
		}
		
		public static byte[] read(Path file, int offset, int length) throws IOException, IndexOutOfBoundsException {
			long fileSize = Files.size(file);
			if (offset > fileSize) {
				offset = 0;
				length = 0;
			}
			if (length + offset > fileSize) {
				length = (int) (fileSize - offset);
			}

			InputStream fis = null;
			byte[] resultBytes = new byte[length];
			if (length > 0) {
				try {
					if (Files.exists(file) && Files.isReadable(file)) {
						fis = Files.newInputStream(file);
						fis.skip(offset);
						fis.read(resultBytes, 0, length);
					}

				} finally {
					try {
						fis.close();
					} catch (IOException e) { // Ignore any exceptions here
					}
				}
			}
			
			return resultBytes;
		}

		public static String encodeBase64(byte[] bytes) {
			return Base64.encodeBytes(bytes);
		}
	}

}
