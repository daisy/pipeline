package org.daisy.dotify.formatter.impl.writer;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.daisy.dotify.api.writer.AttributeItem;
import org.daisy.dotify.api.writer.MetaDataItem;
import org.daisy.dotify.api.writer.PagedMediaWriterException;
import org.daisy.dotify.formatter.impl.writer.PEFMediaWriter;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class PEFMediaWriterTest {

	
	@Test
	public void testMetadata() throws PagedMediaWriterException {
		PEFMediaWriter p = new PEFMediaWriter(new Properties());
		ArrayList<MetaDataItem> meta = new ArrayList<>();
		meta.add(new MetaDataItem(new QName("http://purl.org/dc/elements/1.1/", "identifier"),  "12345"));
		meta.add(new MetaDataItem(new QName("http://purl.org/dc/elements/1.1/", "date"),  "2015-09-30"));
		meta.add(new MetaDataItem.Builder(new QName("http://www.example.org/ns/mine/", "entry", "generator"),  "sunny").attribute(new AttributeItem("key", "weather")).build());
		meta.add(new MetaDataItem(new QName("http://purl.org/dc/elements/1.1/", "publisher"),  "publisher"));
		p.prepare(meta);
		final StringWriter w = new StringWriter();
		p.open(new OutputStream(){
			@Override
			public void write(int b) throws IOException {
				w.write(b);
			}});
		p.close();
		String exp = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<pef version=\"2008-1\" xmlns=\"http://www.daisy.org/ns/2008/pef\">"
				+ "<head>"
				+ "<meta xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:generator=\"http://www.example.org/ns/mine/\">"
				+ "<dc:format>application/x-pef+xml</dc:format>"
				+ "<dc:identifier>12345</dc:identifier>"
				+ "<dc:date>2015-09-30</dc:date>"
				+ "<dc:publisher>publisher</dc:publisher>"
				+ "<generator:entry key=\"weather\">sunny</generator:entry>"
				+ "</meta>"
				+ "</head>"
				+ "<body>"
				+ "</body>"
				+ "</pef>";
		assertEquals(exp, w.toString().replaceAll("[\\r\\n]+", ""));
	}
}
