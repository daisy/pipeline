package org.daisy.common.xproc.calabash.steps;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.daisy.common.xproc.calabash.steps.SetDoctypeProvider.SetDoctype;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetDoctypeProviderTest {
	
	Reader reader = null;
	Writer writer = null;
	
	String input = null, doctype = null, expected = null, actual = null;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void test() {
		try {
			input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			      + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			doctype = "<!DOCTYPE html>";
			expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				      + "<!DOCTYPE html><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, doctype, null, logger);
			
			actual = writer.toString();
			
			Assert.assertEquals(expected, actual);
			
		} catch (IOException e) {
			Assert.fail("An exception occured while testing setDoctypeOnStream: "+e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Test
	public void testRemoveDoctype() {
		try {
			input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			      + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			doctype = "";
			expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
				      + "<html xmlns=\"http://www.w3.org/1999/xhtml\">";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, doctype, null, logger);
			
			actual = writer.toString();
			
			Assert.assertEquals(expected, actual);
			
		} catch (IOException e) {
			Assert.fail("An exception occured while testing setDoctypeOnStream: "+e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Test
	public void testNoDoctypeInInputNoXmlDeclaration() {
		try {
			input = "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
			      + "    <head></head>\n"
				  + "    <body></body>\n</html>\n";
			doctype = "<!DOCTYPE html>";
			expected = "<!DOCTYPE html>\n"
					 + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\n"
					 + "    <head></head>\n"
				     + "    <body></body>\n</html>\n";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, doctype, null, logger);
			
			actual = writer.toString();
			
			Assert.assertEquals(expected, actual);
			
		} catch (IOException e) {
			Assert.fail("An exception occured while testing setDoctypeOnStream: "+e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Test
	public void testNoDoctypeInInput() {
		try {
			input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			      + "<html xmlns=\"http://www.w3.org/1999/xhtml\"/>";
			doctype = "<!DOCTYPE html>";
			expected = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					 + "<!DOCTYPE html>\n"
				     + "<html xmlns=\"http://www.w3.org/1999/xhtml\"/>";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, doctype, null, logger);
			
			actual = writer.toString();
			
			Assert.assertEquals(expected, actual);
			
		} catch (IOException e) {
			Assert.fail("An exception occured while testing setDoctypeOnStream: "+e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Test
	public void testComplexProlog() {
		try {
			input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				  + "<?xml-model href=\"http://www.daisy.org/pipeline/modules/nordic/nordic-html5.rng\"?>\n"
				  + "<?xml-model href=\"http://www.daisy.org/pipeline/modules/nordic/nordic2015-1.sch\"?>\n"
				  + "<!-- comment with <tag/> in it -->\n"
				  + "<?xml-stylesheet href=\"dtbook.2005.basic.css\" type=\"text/css\"?>\n"
				  + "<!DOCTYPE test [\n"
				  + " <!ELEMENT test (type*)>\n"
				  + "  <!ATTLIST test\n"
				  + "  id ID #REQUIRED\n"
				  + "  name CDATA #FIXED \"--> ' > ?> &gt;\"\n"
				  + "  >\n"
				  + "]>\n"
				  + "<!-- comment -->\n"
				  + "<p:declare-step xmlns:p=\"http://www.w3.org/ns/xproc\" problematic-attribute-1 = 'contains &quot; >' xmlns:c=\"http://www.w3.org/ns/xproc-step\" xmlns:px=\"http://www.daisy.org/ns/pipeline/xproc\" xmlns:d=\"http://www.daisy.org/ns/pipeline/data\"\n"
				  + "    type=\"px:file-utils-test\" name=\"main\" version=\"1.0\" xmlns:epub=\"http://www.idpf.org/2007/ops\" xmlns:l=\"http://xproc.org/library\" xmlns:dtbook=\"http://www.daisy.org/z3986/2005/dtbook/\"\n"
				  + "    xmlns:html=\"http://www.w3.org/1999/xhtml\" xmlns:cx=\"http://xmlcalabash.com/ns/extensions\" xmlns:pxi=\"http://www.daisy.org/ns/pipeline/xproc/internal\"/>\n";
			doctype = "<!DOCTYPE html>";
			expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
					 + "<?xml-model href=\"http://www.daisy.org/pipeline/modules/nordic/nordic-html5.rng\"?>\n"
					 + "<?xml-model href=\"http://www.daisy.org/pipeline/modules/nordic/nordic2015-1.sch\"?>\n"
					 + "<!-- comment with <tag/> in it -->\n"
					 + "<?xml-stylesheet href=\"dtbook.2005.basic.css\" type=\"text/css\"?>\n"
					 + "<!DOCTYPE html>\n"
					 + "<!-- comment -->\n"
					 + "<p:declare-step xmlns:p=\"http://www.w3.org/ns/xproc\" problematic-attribute-1 = 'contains &quot; >' xmlns:c=\"http://www.w3.org/ns/xproc-step\" xmlns:px=\"http://www.daisy.org/ns/pipeline/xproc\" xmlns:d=\"http://www.daisy.org/ns/pipeline/data\"\n"
					 + "    type=\"px:file-utils-test\" name=\"main\" version=\"1.0\" xmlns:epub=\"http://www.idpf.org/2007/ops\" xmlns:l=\"http://xproc.org/library\" xmlns:dtbook=\"http://www.daisy.org/z3986/2005/dtbook/\"\n"
					 + "    xmlns:html=\"http://www.w3.org/1999/xhtml\" xmlns:cx=\"http://xmlcalabash.com/ns/extensions\" xmlns:pxi=\"http://www.daisy.org/ns/pipeline/xproc/internal\"/>\n";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, doctype, null, logger);
			
			actual = writer.toString();
			
			Assert.assertEquals(expected, actual);
			
		} catch (IOException e) {
			Assert.fail("An exception occured while testing setDoctypeOnStream: "+e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Test
	public void testShortDoc() {
		try {
			input = "<x/>";
			doctype = "<!DOCTYPE html>";
			expected = "<!DOCTYPE html>\n<x/>";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, doctype, null, logger);
			
			actual = writer.toString();
			
			Assert.assertEquals(expected, actual);
			
		} catch (IOException e) {
			Assert.fail("An exception occured while testing setDoctypeOnStream: "+e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Test
	public void testEmptyDoc() {
		try {
			input = "";
			doctype = "<!DOCTYPE html>";
			expected = "<!DOCTYPE html>\n";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, doctype, null, logger);
			
			actual = writer.toString();
			
			Assert.assertEquals(expected, actual);
			
		} catch (IOException e) {
			Assert.fail("An exception occured while testing setDoctypeOnStream: "+e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
	@Test
	public void testNonXmlDoc() {
		try {
			input = "123";
			doctype = "<!DOCTYPE html>";
			expected = "<!DOCTYPE html>\n123";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, doctype, null, logger);
			
			actual = writer.toString();
			
			Assert.assertEquals(expected, actual);
			
		} catch (IOException e) {
			Assert.fail("An exception occured while testing setDoctypeOnStream: "+e.getMessage());
			e.printStackTrace();
			
		} finally {
			try {
				reader.close();
			} catch (IOException e) {
				// ignore
			}
		}
	}
	
}
