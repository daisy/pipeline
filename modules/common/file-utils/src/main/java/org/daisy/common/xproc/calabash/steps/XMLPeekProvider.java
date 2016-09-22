package org.daisy.common.xproc.calabash.steps;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStepProvider;

import org.daisy.common.xproc.calabash.steps.PeekProvider.Peek;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

@SuppressWarnings("serial")
public class XMLPeekProvider implements XProcStepProvider {

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new XMLPeek(runtime, step);
	}

	/**
	 * Returns the root element of the XML document referenced in `href`, including its attributes and namespaces.
	 */
	public static class XMLPeek extends DefaultStep {
		private static final QName _href = new QName("href");

		private WritablePipe result = null;

		// XML bytes
		private byte byteLT = (byte) 60; // <
		private byte byteGT = (byte) 62; // >
		private byte byteQM = (byte) 63; // ?
		private byte byteEM = (byte) 33; // !

		// BOM bytes
		private byte byte00 = (byte) 0;
		private byte byteBB = (byte) 187;
		private byte byteBF = (byte) 191;
		private byte byteEF = (byte) 239;
		private byte byteFE = (byte) 254;
		private byte byteFF = (byte) 255;

		public XMLPeek(XProcRuntime runtime, XAtomicStep step) {
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

			RuntimeValue href = getOption(_href);
			URI sourceUri = href.getBaseURI().resolve(href.getString());
			File file = new File(sourceUri.getPath());

			if (file.isDirectory()) {
				throw new XProcException(step.getNode(), "Cannot peek into XML-file: file is a directory: " + file.getAbsolutePath());
			}

			String bomType = null;
			String bomBase64 = null;
			String bomHex = null;
			try {
				byte[] bomBytes = Peek.read(file, 0, 4);

				if (bomBytes.length >= 3 && bomBytes[0] == byteEF && bomBytes[1] == byteBB && bomBytes[2] == byteBF) {
					logger.warn("File contains UTF-8 BOM:"+file);
					bomType = "UTF-8";
					bomBase64 = Peek.encodeBase64(spliceBytes(bomBytes, 0, 2));
					bomHex = "EF BB BF";

				} else if (bomBytes.length >= 2 && bomBytes[0] == byteFE && bomBytes[1] == byteFF) {
					logger.warn("File contains UTF-16 (BE) BOM. Results may vary: "+file);
					bomType = "UTF-16 (BE)";
					bomBase64 = Peek.encodeBase64(spliceBytes(bomBytes, 0, 1));
					bomHex = "FE FF";

				} else if (bomBytes.length >= 2 && bomBytes[0] == byteFF && bomBytes[1] == byteFE) {
					logger.warn("File contains UTF-16 (LE) BOM. Results may vary: "+file);
					bomType = "UTF-16 (LE)";
					bomBase64 = Peek.encodeBase64(spliceBytes(bomBytes, 0, 1));
					bomHex = "FF FE";

				} else if (bomBytes.length >= 4 && bomBytes[0] == byte00 && bomBytes[1] == byte00 && bomBytes[2] == byteFE && bomBytes[3] == byteFF) {
					logger.warn("File contains UTF-32 (BE) BOM. Results may vary: "+file);
					bomType = "UTF-32 (BE)";
					bomBase64 = Peek.encodeBase64(spliceBytes(bomBytes, 0, 3));
					bomHex = "00 00 FE FF";

				} else if (bomBytes.length >= 4 && bomBytes[0] == byteFF && bomBytes[1] == byteFE && bomBytes[2] == byte00 && bomBytes[3] == byte00) {
					logger.warn("File contains UTF-32 (LE) BOM. Results may vary: "+file);
					bomType = "UTF-32 (LE)";
					bomBase64 = Peek.encodeBase64(spliceBytes(bomBytes, 0, 3));
					bomHex = "FF FE 00 00";
				}

			} catch (IOException | IndexOutOfBoundsException e) {
				logger.warn("px:file-xml-peek failed to read BOM from "+file, e);
			}

			StringBuilder resultBuilder = new StringBuilder();
			Reader reader = null, buffer = null;
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				try {
					reader = new InputStreamReader(in, "UTF-8");
				} catch (UnsupportedEncodingException e) {
					reader = new InputStreamReader(in);
				}
				buffer = new BufferedReader(reader);
				int characterCount = 0;
				int r;
				char previousPreviousPreviousCharacter = ' ';
				char previousPreviousCharacter = ' ';
				char previousCharacter = ' ';
				boolean isReadingElement = false;
				boolean isReadingComment = false;
				char isReadingAttribute = ' ';
				while ((r = reader.read()) != -1) {
					// warn after 100K chars, break after 1M chars, we can possibly reduce these if this becomes a bottleneck
					characterCount++;
					if (characterCount > 100000) {
						logger.warn("More than 100 000 characters in prolog; this is probably not a XML file but will keep looking for root element a little while longer: "+file);
					}
					if (characterCount > 1000000) {
						logger.warn("More than 1 000 000 characters in prolog; this is extremely unlikely to be a XML file; will abort: "+file);
						resultBuilder = new StringBuilder(); // clear result contents
						break;
					}

					char character = (char) r;

					if (previousPreviousPreviousCharacter == '<' && previousPreviousCharacter == '!' && previousCharacter == '-' && character == '-') {
						isReadingComment = true;
						
					} else if (isReadingAttribute == ' ' && previousPreviousCharacter == '-' && previousCharacter == '-' && character == '>') {
						isReadingComment = false;

					} else if (!isReadingComment && previousCharacter == '<' && (character != '!' && character != '?')) {
						isReadingElement = true;
						
					} else if (isReadingElement && (character == '\'' || character == '"') && (isReadingAttribute == character || isReadingAttribute == ' ')) {
						if (character != isReadingAttribute) {
							isReadingAttribute = character;
						} else {
							isReadingAttribute = ' ';
						}
					
					} else if (isReadingElement && isReadingAttribute == ' ' && character == '>') {
						if (previousCharacter != '/') {
							resultBuilder.append('/');
						}
						resultBuilder.append('>');
						break;
					}

					previousPreviousPreviousCharacter = previousPreviousCharacter;
					previousPreviousCharacter = previousCharacter;
					previousCharacter = character;
					resultBuilder.append(character);
				}
			} catch (IOException e) {
				logger.error("px:file-xml-peek failed to read from "+file, e);
				e.printStackTrace();

			} finally {
				if (buffer != null) {
					try {
						buffer.close();
					} catch (IOException e) {}
				}
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {}
				}
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {}
				}
			}
			String resultString = resultBuilder.toString();

			TreeWriter tree = new TreeWriter(runtime);
			tree.startDocument(step.getNode().getBaseURI());
			tree.addStartElement(XProcConstants.c_result);
			tree.addAttribute(new QName("content-type"), "text/plain; charset=utf-8");
			if (bomType != null) {
				tree.addAttribute(new QName("bom-type"), bomType);
			}
			if (bomBase64 != null) {
				tree.addAttribute(new QName("bom-base64"), bomBase64);
			}
			if (bomBase64 != null) {
				tree.addAttribute(new QName("bom-hex"), bomHex);
			}
			tree.startContent();

			tree.addText(resultString);

			tree.addEndElement();
			tree.endDocument();

			result.write(tree.getResult());

		}

		public static byte[] spliceBytes(byte[] bytes, int from, int to) {
			from = Math.max(0, from);
			to = Math.min(bytes.length-1, to);
			byte[] resultBytes = new byte[to - from + 1];
			for (int b = 0; b < resultBytes.length; b++) {
				resultBytes[b] = bytes[b + from];
			}
			return resultBytes;
		}
	}

}
