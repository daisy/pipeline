package org.daisy.common.xproc.calabash.steps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.daisy.common.xproc.calabash.XProcStepProvider;
import org.slf4j.Logger;

import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.library.DefaultStep;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.TreeWriter;

public class SetXmlDeclarationProvider implements XProcStepProvider {

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new SetXmlDeclaration(runtime, step);
	}

	public static class SetXmlDeclaration extends DefaultStep {
		private static final QName _href = new QName("href");
		private static final QName _xmlDeclaration = new QName("xml-declaration");
		private static final QName _encoding = new QName("encoding");

		private WritablePipe result = null;

		public SetXmlDeclaration(XProcRuntime runtime, XAtomicStep step) {
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

			String xmlDeclaration = getOption(_xmlDeclaration, "");
			String encoding = getOption(_encoding, "utf-8");

			RuntimeValue href = getOption(_href);
			URI sourceUri = href.getBaseURI().resolve(href.getString());
			File file = new File(sourceUri.getPath());
			
			if (file.isDirectory()) {
				throw new XProcException(step.getNode(), "Cannot set xml declaration of file: file is a directory: " + file.getAbsolutePath());
			}
			
			boolean ok = false;
			ok = SetXmlDeclaration.setXmlDeclaration(file, encoding, xmlDeclaration, logger);


			TreeWriter tree = new TreeWriter(runtime);
			tree.startDocument(step.getNode().getBaseURI());
			tree.addStartElement(XProcConstants.c_result);
			tree.startContent();
			
			if (ok) {
				logger.debug("SetXmlDeclaration: successfully set the XML declaration");
				tree.addText(file.toURI().toString());
			} else {
				String errorMessage = "px:set-xml-declaration failed to read from "+file+" (xml declaration: "+xmlDeclaration+", filesize: "+(file==null?'?':file.length())+")";
				logger.warn("SetXmlDeclaration: "+errorMessage);
				tree.addAttribute(new QName("error"), errorMessage);
			}

			tree.addEndElement();
			tree.endDocument();

			result.write(tree.getResult());
		}
		
		public static boolean setXmlDeclaration(File file, String encoding, String xmlDeclaration, Logger logger) {
			
			logger.debug("SetXmlDeclaration: file=["+file+"]");
			logger.debug("SetXmlDeclaration: encoding=["+encoding+"]");
			logger.debug("SetXmlDeclaration: xmlDeclaration=["+xmlDeclaration+"]");
			
			Reader reader = null;
			InputStream in = null;
			
			File tempFile = null;
			Writer writer = null;
			
			boolean success = false;
			
			try {
				in = new FileInputStream(file);
				try {
					reader = new InputStreamReader(in, encoding);
				} catch (UnsupportedEncodingException e) {
					reader = new InputStreamReader(in);
				}
				
				tempFile = File.createTempFile("setXmlDeclaration", "");
				writer = new OutputStreamWriter(new FileOutputStream(tempFile), encoding);
				
				success = setXmlDeclarationOnStream(reader, writer, xmlDeclaration, file.toString(), logger);
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("SetXmlDeclaration: unable to close OutputStreamWriter", e);
				}
				
				if (success) {
				    Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				
			} catch (IOException e) {
				if (logger != null) {
					logger.error("SetXmlDeclaration: px:set-xml-declaration failed to read from "+file, e);
				}
				e.printStackTrace();

			} finally {
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
				if (writer != null) {
					try {
						writer.close();
					} catch (IOException e) {}
				}
			}
			
			return success;
		}
		
		public static boolean setXmlDeclarationOnStream(Reader reader, Writer writer, String xmlDeclaration, String name, Logger logger) throws IOException {
			int offset = -1;
			int r;
			char currentCharacter = ' ';
			char nextCharacter = ' ';
			
			if ((r = reader.read()) != -1) {
				nextCharacter = (char) r;
				offset++;
			}
			
			while ((r = reader.read()) != -1) {
				offset++;
				
				currentCharacter = nextCharacter;
				nextCharacter = (char) r;
				
				if (offset == 1 && !(currentCharacter == '<' && nextCharacter == '?')) {
					// no existing xml declaration
					logger.debug("SetXmlDeclaration: no existing xml declaration");
					
					if (!"".equals(xmlDeclaration)) {
						writer.write(xmlDeclaration);
						writer.write("\n");
					}
					writer.write(currentCharacter);
					writer.write(nextCharacter);
					break;
				}
				
				if (offset > 1 && (currentCharacter == '?' && nextCharacter == '>')) {
					// found end of existing xml declaration
					logger.debug("SetXmlDeclaration: found end of existing xml declaration");
					
					if ("".equals(xmlDeclaration) && (r = reader.read()) != -1) {
						offset++;
						currentCharacter = nextCharacter;
						nextCharacter = (char) r;
						
						if (nextCharacter != '\n') {
							writer.write(nextCharacter);
						}
						
					} else {
						writer.write(xmlDeclaration);
					}
					
					break;
				}
			}
			
			if (offset < 1) {
				writer.write(xmlDeclaration);
				writer.write("\n");
				if (offset >= 0) {
					writer.write(nextCharacter);
				}
			}
			
			// Write remaining characters from reader to writer
			logger.debug("SetXmlDeclaration: Write remaining characters from reader to writer");
			int bufferSize = 1024;
			char[] buffer = new char[bufferSize];
			int length = 0;
			do {
				length = reader.read(buffer, 0, bufferSize);
				if (length > 0) {
					writer.write(buffer, 0, length);
				}
			} while (length > 0);
			
			return true;
			
		}
	}

}
