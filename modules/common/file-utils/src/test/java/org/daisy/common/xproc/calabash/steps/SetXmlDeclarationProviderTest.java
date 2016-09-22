package org.daisy.common.xproc.calabash.steps;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.daisy.common.xproc.calabash.steps.SetDoctypeProvider.SetDoctype;
import org.daisy.common.xproc.calabash.steps.SetXmlDeclarationProvider.SetXmlDeclaration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetXmlDeclarationProviderTest {
	
	Reader reader = null;
	Writer writer = null;
	
	String input = null, xmlDeclaration = null, expected = null, actual = null;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Test
	public void testReplaceXmlDeclaration() {
		try {
			input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			      + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				  + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetXmlDeclaration.setXmlDeclarationOnStream(reader, writer, xmlDeclaration, null, logger);
			
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
	public void testAddXmlDeclaration() {
		try {
			input = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				  + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetXmlDeclaration.setXmlDeclarationOnStream(reader, writer, xmlDeclaration, null, logger);
			
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
	public void testRemoveXmlDeclaration() {
		try {
			input = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			      + "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			xmlDeclaration = "";
			expected = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\">";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetXmlDeclaration.setXmlDeclarationOnStream(reader, writer, xmlDeclaration, null, logger);
			
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
			xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<x/>";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, xmlDeclaration, null, logger);
			
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
			xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, xmlDeclaration, null, logger);
			
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
			xmlDeclaration = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
			expected = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n123";
			
			reader = new StringReader(input);
			writer = new StringWriter();
			SetDoctype.setDoctypeOnStream(reader, writer, xmlDeclaration, null, logger);
			
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
