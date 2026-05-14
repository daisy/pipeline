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

public class GenericMarkSplitterTest {
	static Processor Proc = new Processor(false);
	static SSMLMarkSplitter Splitter = new DefaultSSMLMarkSplitter(Proc);
	static String SsmlNs = "http://www.w3.org/2001/10/synthesis";

	private static TreeWriter newTreeWriter() throws URISyntaxException {
		TreeWriter tw = new TreeWriter(Proc);
		tw.startDocument(new URI("http://test"));
		tw.startContent();
		tw.addStartElement(new QName(SsmlNs, "speak"));
		return tw;
	}

	@Test
	public void zeroMark() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		TreeWriter tw = newTreeWriter();
		tw.addStartElement(new QName(null, "a"));
		tw.addText("text1");
		tw.addEndElement();
		tw.addStartElement(new QName(null, "b"));
		tw.addText("text2");
		tw.addEndElement();
		tw.addEndElement();

		Collection<Chunk> chunks = Splitter.split(tw.getResult());

		Assert.assertEquals(1, chunks.size());
		Diff d = new Diff(tw.getResult().toString(), chunks.iterator().next().ssml()
		        .toString());
		Assert.assertTrue(d.similar());
	}

	@Test
	public void oneMark() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String markname = "mark1";

		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName(null, "a"));
		source.addText("text1");
		source.addEndElement();
		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName(null, "name"), markname);
		source.addEndElement();
		source.addStartElement(new QName(null, "b"));
		source.addText("text2");
		source.addEndElement();

		TreeWriter chunk1 = newTreeWriter();
		chunk1.addStartElement(new QName(null, "a"));
		chunk1.addText("text1");
		chunk1.addEndElement();

		TreeWriter chunk2 = newTreeWriter();
		chunk2.addStartElement(new QName(null, "b"));
		chunk2.addText("text2");
		chunk2.addEndElement();

		Collection<Chunk> chunks = Splitter.split(source.getResult());

		Chunk[] chunkarr = chunks.toArray(new Chunk[5]);

		Assert.assertEquals(2, chunks.size());
		Assert.assertEquals(chunkarr[0].leftMark(), null);
		Assert.assertEquals(chunkarr[1].leftMark(), markname);

		Assert.assertTrue(new Diff(chunkarr[0].ssml().toString(), chunk1.getResult()
		        .toString()).similar());
		Assert.assertTrue(new Diff(chunkarr[1].ssml().toString(), chunk2.getResult()
		        .toString()).similar());
	}

	@Test
	public void twoMarks() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String markname1 = "mark1";
		String markname2 = "mark2";

		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName(null, "a"));
		source.addText("text1");
		source.addEndElement();

		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName(null, "name"), markname1);
		source.addEndElement();

		source.addStartElement(new QName(null, "b"));
		source.addText("text2");
		source.addEndElement();

		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName(null, "name"), markname2);
		source.addEndElement();

		source.addStartElement(new QName(null, "c"));
		source.addText("text3");
		source.addEndElement();

		//////

		TreeWriter chunk1 = newTreeWriter();
		chunk1.addStartElement(new QName(null, "a"));
		chunk1.addText("text1");
		chunk1.addEndElement();

		TreeWriter chunk2 = newTreeWriter();
		chunk2.addStartElement(new QName(null, "b"));
		chunk2.addText("text2");
		chunk2.addEndElement();

		TreeWriter chunk3 = newTreeWriter();
		chunk3.addStartElement(new QName(null, "c"));
		chunk3.addText("text3");
		chunk3.addEndElement();

		Collection<Chunk> chunks = Splitter.split(source.getResult());

		Chunk[] chunkarr = chunks.toArray(new Chunk[5]);

		Assert.assertEquals(3, chunks.size());
		Assert.assertEquals(chunkarr[0].leftMark(), null);
		Assert.assertEquals(chunkarr[1].leftMark(), markname1);
		Assert.assertEquals(chunkarr[2].leftMark(), markname2);

		Assert.assertTrue(new Diff(chunkarr[0].ssml().toString(), chunk1.getResult()
		        .toString()).similar());
		Assert.assertTrue(new Diff(chunkarr[1].ssml().toString(), chunk2.getResult()
		        .toString()).similar());
		Assert.assertTrue(new Diff(chunkarr[2].ssml().toString(), chunk3.getResult()
		        .toString()).similar());
	}

	@Test
	public void smartSplit() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		String markname1 = "mark1";
		String markname2 = "mark2";

		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName(null, "a"));
		source.addStartElement(new QName(null, "k"));
		source.addText("text1");
		source.addEndElement(); //</k>

		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName(null, "name"), markname1);
		source.addEndElement();

		source.addStartElement(new QName(null, "b"));
		source.addEndElement(); //empty <b/>

		source.addStartElement(new QName(null, "c"));
		source.addText("text2");
		source.addEndElement();

		source.addText("text3");

		source.addStartElement(new QName(SsmlNs, "mark"));
		source.addAttribute(new QName(null, "name"), markname2);
		source.addEndElement();

		source.addEndElement(); //</a>

		source.addText("text4");

		//////

		TreeWriter chunk1 = newTreeWriter();
		chunk1.addStartElement(new QName(null, "a"));
		chunk1.addStartElement(new QName(null, "k"));
		chunk1.addText("text1");
		chunk1.addEndElement();
		chunk1.addEndElement();

		TreeWriter chunk2 = newTreeWriter();
		chunk2.addStartElement(new QName(null, "a"));
		chunk2.addStartElement(new QName(null, "b"));
		chunk2.addEndElement(); //empty <b/>
		chunk2.addStartElement(new QName(null, "c"));
		chunk2.addText("text2");
		chunk2.addEndElement();
		chunk2.addText("text3");
		chunk2.addEndElement();

		TreeWriter chunk3 = newTreeWriter();
		chunk3.addText("text4");
		chunk3.addEndElement();

		Collection<Chunk> chunks = Splitter.split(source.getResult());

		Chunk[] chunkarr = chunks.toArray(new Chunk[5]);

		Assert.assertEquals(3, chunks.size());
		Assert.assertEquals(chunkarr[0].leftMark(), null);
		Assert.assertEquals(chunkarr[1].leftMark(), markname1);
		Assert.assertEquals(chunkarr[2].leftMark(), markname2);

		Assert.assertTrue(new Diff(chunkarr[0].ssml().toString(), chunk1.getResult()
		        .toString()).similar());

		Assert.assertTrue(new Diff(chunkarr[1].ssml().toString(), chunk2.getResult()
		        .toString()).similar());
		Assert.assertTrue(new Diff(chunkarr[2].ssml().toString(), chunk3.getResult()
		        .toString()).similar());
	}

	@Test
	public void manyMarks() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		int numMarks = 3000;
		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName(null, "a"));
		source.addText("text0");
		source.addEndElement();
		for (int i = 1; i <= numMarks; ++i) {
			source.addStartElement(new QName(SsmlNs, "mark"));
			source.addAttribute(new QName(null, "name"), "mark" + i);
			source.addEndElement();
			source.addStartElement(new QName(null, "a"));
			source.addText("text" + i);
			source.addEndElement();
		}

		Collection<Chunk> chunks = Splitter.split(source.getResult());

		Chunk[] chunkarr = chunks.toArray(new Chunk[numMarks + 1]);

		Assert.assertEquals(numMarks + 1, chunks.size());
		Assert.assertEquals(chunkarr[0].leftMark(), null);
		for (int i = 1; i <= numMarks; ++i) {
			Assert.assertEquals(chunkarr[i].leftMark(), "mark" + i);
		}
	}

	@Test
	public void notInDocument() throws URISyntaxException, SaxonApiException, SAXException,
	        IOException {
		TreeWriter source = newTreeWriter();
		source.addStartElement(new QName(null, "a"));
		source.addText("text0");
		source.addEndElement();

		Splitter.split((XdmNode) source.getResult().axisIterator(Axis.CHILD).next());
	}

}
