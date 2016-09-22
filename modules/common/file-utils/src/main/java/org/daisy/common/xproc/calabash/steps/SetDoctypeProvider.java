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

@SuppressWarnings("serial")
public class SetDoctypeProvider implements XProcStepProvider {

	@Override
	public XProcStep newStep(XProcRuntime runtime, XAtomicStep step) {
		return new SetDoctype(runtime, step);
	}

	public static class SetDoctype extends DefaultStep {
		private static final QName _href = new QName("href");
		private static final QName _doctype = new QName("doctype");
		private static final QName _encoding = new QName("encoding");

		private WritablePipe result = null;

		public SetDoctype(XProcRuntime runtime, XAtomicStep step) {
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

			String doctype = getOption(_doctype, "");
			String encoding = getOption(_encoding, "utf-8");

			RuntimeValue href = getOption(_href);
			URI sourceUri = href.getBaseURI().resolve(href.getString());
			File file = new File(sourceUri.getPath());
			
			if (file.isDirectory()) {
				throw new XProcException(step.getNode(), "Cannot set doctype of file: file is a directory: " + file.getAbsolutePath());
			}
			
			boolean ok = false;
			ok = SetDoctype.setDoctype(file, encoding, doctype, logger);


			TreeWriter tree = new TreeWriter(runtime);
			tree.startDocument(step.getNode().getBaseURI());
			tree.addStartElement(XProcConstants.c_result);
			tree.startContent();
			
			if (ok) {
				logger.debug("SetDoctype: successfully set the doctype");
				tree.addText(file.toURI().toString());
			} else {
				String errorMessage = "px:set-doctype failed to read from "+file+" (doctype: "+doctype+", filesize: "+(file==null?'?':file.length())+")";
				logger.warn("SetDoctype: "+errorMessage);
				tree.addAttribute(new QName("error"), errorMessage);
			}

			tree.addEndElement();
			tree.endDocument();

			result.write(tree.getResult());
		}
		
		public static boolean setDoctype(File file, String encoding, String doctype, Logger logger) {
			
			logger.debug("SetDoctype: file=["+file+"]");
			logger.debug("SetDoctype: encoding=["+encoding+"]");
			logger.debug("SetDoctype: doctype=["+doctype+"]");
			
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
				
				tempFile = File.createTempFile("setDoctype", "");
				writer = new OutputStreamWriter(new FileOutputStream(tempFile), encoding);
				
				success = setDoctypeOnStream(reader, writer, doctype, file.toString(), logger);
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("SetDoctype: unable to close OutputStreamWriter", e);
				}
				
				if (success) {
				    Files.move(tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
				}
				
			} catch (IOException e) {
				if (logger != null) {
					logger.error("SetDoctype: px:set-doctype failed to read from "+file, e);
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
		
		public static boolean setDoctypeOnStream(Reader reader, Writer writer, String doctype, String name, Logger logger) throws IOException {
			int offset = -1;
			int r;
			char currentCharacter = ' ';
			char nextCharacter = ' ';
			char nextNextCharacter = ' ';
			char nextNextNextCharacter = ' ';
			int isReadingDoctype = 0;
			boolean foundDoctype = false;
			boolean isReadingPI = false;
			boolean isReadingElement = false;
			boolean isReadingComment = false;
			char isReadingAttribute = ' ';
			int skipChars = 0;
			
			if ((r = reader.read()) != -1) {
				nextCharacter = (char) r;
				offset++;
				
				if ((r = reader.read()) != -1) {
					nextNextCharacter = (char) r;
					offset++;
					
					if ((r = reader.read()) != -1) {
						nextNextNextCharacter = (char) r;
						offset++;
					}
				}
			}
			
			while ((r = reader.read()) != -1) {
				offset++;
				
				// warn after 100K chars, break after 1M chars, we can possibly reduce these if this becomes a bottleneck
				if (offset == 100000) {
					if (logger != null) {
						logger.warn("SetDoctype: More than 100 000 characters in prolog; this is probably not a XML document but will keep looking for DOCTYPE a little while longer: "+name);
					}
				}
				if (offset > 1000000) {
					if (logger != null) {
						logger.warn("SetDoctype: More than 1 000 000 characters in prolog; this is extremely unlikely to be a XML document; will abort: "+name);
					}
					writer.close();
					writer = null; // clear result contents
					break;
				}

				currentCharacter = nextCharacter;
				nextCharacter = nextNextCharacter;
				nextNextCharacter = nextNextNextCharacter;
				nextNextNextCharacter = (char) r;
				
				if (skipChars > 0) {
					skipChars--;
					
				} else if (currentCharacter == '<' && nextCharacter == '!' && nextNextCharacter == '-' && nextNextNextCharacter == '-') {
					// "<!--"
					logger.debug("SetDoctype: \"<!--\"");
					isReadingComment = true;
					
				} else if (currentCharacter == '-' && nextCharacter == '-' && nextNextCharacter == '>' && isReadingComment) {
					// "-->" and is reading comment
					logger.debug("SetDoctype: \"-->\" and is not reading comment");
					isReadingComment = false;
					skipChars = 2;
					
				} else if (currentCharacter == '<' && nextCharacter == '?' && !isReadingComment) {
					// "<?" and is not reading comment
					logger.debug("SetDoctype: \"<?\" and is not reading comment");
					isReadingPI = true;

				} else if (currentCharacter == '<' && nextCharacter == '!' && "DdEeAaNn".indexOf(""+nextNextCharacter) != -1 && !isReadingComment) {
					// "<!D" or "<!E" etc. and is not reading comment
					logger.debug("SetDoctype: \"<!D\" or \"<!E\" etc. and is not reading comment");
					isReadingDoctype++;
					foundDoctype = true;

				} else if (currentCharacter == '?' && nextCharacter == '>' && !isReadingComment && isReadingAttribute == ' ') {
					// "?>" and is not reading comment or attribute
					logger.debug("SetDoctype: \"?>\" and is not reading comment or attribute");
					isReadingPI = false;
					skipChars = 1;

				} else if (currentCharacter == '>' && !isReadingComment && isReadingAttribute == ' ') {
					// ">" and is not reading comment or attribute
					logger.debug("SetDoctype: \">\" and is not reading comment or attribute");
					
					isReadingDoctype--;
					
					if (isReadingDoctype == 0) {
						logger.debug("SetDoctype: inserting doctype");
						writer.write(doctype);
						writer.write(nextCharacter);
						writer.write(nextNextCharacter);
						writer.write(nextNextNextCharacter);
						
						logger.debug("SetDoctype: done reading doctype");
						break; // done reading doctype
					}

				} else if ((currentCharacter == '\'' || currentCharacter == '"') && (isReadingElement || isReadingPI || isReadingDoctype > 0) && (isReadingAttribute == currentCharacter || isReadingAttribute == ' ')) {
					if (currentCharacter != isReadingAttribute) {
						// found attribute value start
						logger.debug("SetDoctype: found attribute value start");
						isReadingAttribute = currentCharacter;
					} else {
						// found attribute value end
						logger.debug("SetDoctype: found attribute value end");
						isReadingAttribute = ' ';
					}
				
				} else if (currentCharacter == '<' && (nextCharacter != '!' && nextCharacter != '?') && !isReadingComment) {
					// found root element
					
					logger.debug("SetDoctype: found root element, inserting doctype");
					writer.write(doctype);
					writer.write("\n");
					writer.write(currentCharacter);
					writer.write(nextCharacter);
					writer.write(nextNextCharacter);
					writer.write(nextNextNextCharacter);
					
					logger.debug("SetDoctype: found root element; stop searching for doctype");
					break; // found root element; stop searching for doctype
				}
				
				if (!foundDoctype) {
					logger.debug("SetDoctype: not found doctype yet, writing: \""+currentCharacter+"\"");
					writer.write(currentCharacter);
				}
			}
			
			if (offset < 3) {
				writer.write(doctype);
				writer.write("\n");
				if (offset >= 0) {
					writer.write(nextCharacter);
				}
				if (offset >= 1) {
					writer.write(nextNextCharacter);
				}
				if (offset >= 2) {
					writer.write(nextNextNextCharacter);
				}
			}
			
			// Write remaining characters from reader to writer
			logger.debug("SetDoctype: Write remaining characters from reader to writer");
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
