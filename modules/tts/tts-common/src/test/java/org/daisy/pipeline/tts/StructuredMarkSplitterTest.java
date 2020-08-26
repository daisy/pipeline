package org.daisy.pipeline.tts;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

import org.custommonkey.xmlunit.Diff;
import org.daisy.pipeline.tts.SSMLMarkSplitter.Chunk;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.xmlcalabash.util.TreeWriter;

public class StructuredMarkSplitterTest {
	static Processor Proc = new Processor(false);
	static SSMLMarkSplitter Splitter = new StructuredSSMLSplitter(Proc);
	static String SsmlNs = "http://www.w3.org/2001/10/synthesis";

	private static TreeWriter newTreeWriter() throws URISyntaxException {
		TreeWriter tw = new TreeWriter(Proc);
		tw.startDocument(new URI("http://test"));
		tw.startContent();
		tw.addStartElement(new QName(SsmlNs, "s"));
		return tw;
	}

	private static XdmNode getSentence(TreeWriter tw){
		return (XdmNode) tw.getResult().axisIterator(Axis.CHILD).next();
	}
	
	@Test
	public void zeroMark() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		TreeWriter tw = newTreeWriter();
		tw.addStartElement(new QName("a"));
		tw.addText("text1");
		tw.addEndElement();
		tw.addStartElement(new QName("b"));
		tw.addText("text2");
		tw.addEndElement();
		tw.addEndElement();

		Collection<Chunk> chunks = Splitter.split(getSentence(tw));

		Assert.assertEquals("only one chunk must be found", 1, chunks.size());
		
		Diff d = new Diff(tw.getResult().toString(), chunks.iterator().next().ssml().toString());
		Assert.assertTrue("XML of the chunk must be correct", d.similar());
	}

	@Test
	public void oneMark() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String markname = "mark1";

		//input
		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName("a"));
		source.addText("text1");
		source.addEndElement();
		
		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName("name"), markname);
		source.addEndElement();
		
		source.addStartElement(new QName("b"));
		source.addText("text2");
		source.addEndElement();

		//expected chunk1
		TreeWriter chunk1 = newTreeWriter();
		chunk1.addStartElement(new QName("a"));
		chunk1.addText("text1");
		chunk1.addEndElement();

		//expected chunk2
		TreeWriter chunk2 = newTreeWriter();
		chunk2.addStartElement(new QName("b"));
		chunk2.addText("text2");
		chunk2.addEndElement();

		Collection<Chunk> chunks = Splitter.split(getSentence(source));

		Chunk[] chunkarr = chunks.toArray(new Chunk[5]);

		Assert.assertEquals("2 chunks must be found", 2, chunks.size());
		Assert.assertNull("there must be no mark for the first chunk", chunkarr[0].leftMark());
		Assert.assertEquals("second mark must be properly named", markname, chunkarr[1].leftMark());

		Assert.assertTrue("XML of the first chunk must be correct",
				new Diff(chunkarr[0].ssml().toString(), chunk1.getResult().toString()).similar());
		Assert.assertTrue("XML of the second chunk must be correct",
				new Diff(chunkarr[1].ssml().toString(), chunk2.getResult().toString()).similar());
	}
	
	@Test
	public void emptyChunks() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String markname = "mark1";

		//input
		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName("name"), markname);
		source.addEndElement();
	
		//expected chunks
		TreeWriter chunk = newTreeWriter();
		chunk.addEndElement();

		Collection<Chunk> chunks = Splitter.split(getSentence(source));

		Chunk[] chunkarr = chunks.toArray(new Chunk[5]);

		Assert.assertEquals("2 chunks must be found", 2, chunks.size());
		Assert.assertNull("there must be no mark for the furst chunk", chunkarr[0].leftMark());
		Assert.assertEquals("second mark must be properly named", markname, chunkarr[1].leftMark());

		Assert.assertTrue("XML of the first chunk must be correct",
				new Diff(chunkarr[0].ssml().toString(), chunk.getResult().toString()).similar());
		Assert.assertTrue("XML of the second chunk must be correct",
				new Diff(chunkarr[1].ssml().toString(), chunk.getResult().toString()).similar());
		
	}
	
	
	
	@Test
	public void oneMarkWithText() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String markname = "mark1";

		//input
		TreeWriter source = newTreeWriter();
		source.addText("text1");
		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName("name"), markname);
		source.addEndElement();
		source.addText("text2");

		//expected chunk1
		TreeWriter chunk1 = newTreeWriter();
		chunk1.addText("text1");

		//expected chunk2
		TreeWriter chunk2 = newTreeWriter();
		chunk2.addText("text2");

		Collection<Chunk> chunks = Splitter.split(getSentence(source));

		Chunk[] chunkarr = chunks.toArray(new Chunk[5]);

		Assert.assertEquals("2 chunks must be found", 2, chunks.size());
		Assert.assertNull("there must be no mark for the first chunk", chunkarr[0].leftMark());
		Assert.assertEquals("second mark must be properly named", markname, chunkarr[1].leftMark());

		Assert.assertTrue("XML of the first chunk must be correct",
				new Diff(chunkarr[0].ssml().toString(), chunk1.getResult().toString()).similar());
		Assert.assertTrue("XML of the second chunk must be correct",
				new Diff(chunkarr[1].ssml().toString(), chunk2.getResult().toString()).similar());
	}

	@Test
	public void twoMarks() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String markname1 = "mark1";
		String markname2 = "mark2";

		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName("a"));
		source.addText("text1");
		source.addEndElement();

		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName("name"), markname1);
		source.addEndElement();

		source.addStartElement(new QName("b"));
		source.addText("text2");
		source.addEndElement();

		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName("name"), markname2);
		source.addEndElement();

		source.addStartElement(new QName("c"));
		source.addText("text3");
		source.addEndElement();

		//////

		TreeWriter chunk1 = newTreeWriter();
		chunk1.addStartElement(new QName("a"));
		chunk1.addText("text1");
		chunk1.addEndElement();

		TreeWriter chunk2 = newTreeWriter();
		chunk2.addStartElement(new QName("b"));
		chunk2.addText("text2");
		chunk2.addEndElement();

		TreeWriter chunk3 = newTreeWriter();
		chunk3.addStartElement(new QName("c"));
		chunk3.addText("text3");
		chunk3.addEndElement();

		Collection<Chunk> chunks = Splitter.split(getSentence(source));

		Chunk[] chunkarr = chunks.toArray(new Chunk[5]);

		Assert.assertEquals("3 chunks must be found", 3, chunks.size());
		Assert.assertNull("there must be no mark for the first chunk", chunkarr[0].leftMark());
		Assert.assertEquals("second mark must be properly named",  markname1, chunkarr[1].leftMark());
		Assert.assertEquals("third mark must be properly named",  markname2, chunkarr[2].leftMark());

		Assert.assertTrue("XML of the first chunk must be correct",
				new Diff(chunkarr[0].ssml().toString(), chunk1.getResult().toString()).similar());
		Assert.assertTrue("XML of the second chunk must be correct",
				new Diff(chunkarr[1].ssml().toString(), chunk2.getResult().toString()).similar());
		Assert.assertTrue("XML of the third chunk must be correct",
				new Diff(chunkarr[2].ssml().toString(), chunk3.getResult().toString()).similar());
	}

	@Test
	public void notInDocument() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName("a"));
		source.addText("text0");
		source.addEndElement();

		//not raising any exception
		Splitter.split((XdmNode) source.getResult().axisIterator(Axis.CHILD).next());
	}
}
