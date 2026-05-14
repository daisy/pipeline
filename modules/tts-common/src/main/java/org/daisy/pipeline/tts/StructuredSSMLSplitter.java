package org.daisy.pipeline.tts;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import org.daisy.pipeline.tts.SSMLMarkSplitter.Chunk;

import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

/**
 * This splitter assumes that the input SSML is built in such a way that it is
 * easy to be split, i.e. the marks will always be located right under the
 * sentence to split.
 */
public class StructuredSSMLSplitter implements SSMLMarkSplitter {

	private Processor mProc;
	private static final QName sNode = new QName("http://www.w3.org/2001/10/synthesis", "s");

	public StructuredSSMLSplitter(Processor proc) {
		mProc = proc;
	}
	
	@Override
	public Collection<Chunk> split(XdmNode sentence) {
		ArrayList<Chunk> chunks = new ArrayList<Chunk>();

		URI docURI = null;
		try {
			docURI = new URI("http://tmp");
		} catch (URISyntaxException e) {
		}
		
		String leftMarkName = null;
		String rightMarkName = null;
		XdmSequenceIterator iter = sentence.axisIterator(Axis.CHILD);
		boolean mark = false;
		while (iter.hasNext()){
			TreeWriter tw = new TreeWriter(mProc);
			tw.startDocument(docURI);
			tw.startContent();
			tw.addStartElement(sNode);
			mark = false;
			while (iter.hasNext()) {
				XdmNode elt = (XdmNode) iter.next();
				if (elt.getNodeKind() == XdmNodeKind.ELEMENT && "mark".equals(elt.getNodeName().getLocalName())){
					leftMarkName = rightMarkName;
					rightMarkName = elt.getAttributeValue(new QName("name"));
					mark = true;
					break;
				}
				else
					tw.addSubtree(elt);
			}
			if (!iter.hasNext() && !mark){
				leftMarkName = rightMarkName;
			}
			tw.addEndElement();
			tw.endDocument();
			chunks.add(new Chunk(tw.getResult(), leftMarkName));
		}
		if (mark){
			//last node is a mark
			TreeWriter tw = new TreeWriter(mProc);
			tw.startDocument(docURI);
			tw.startContent();
			tw.addStartElement(sNode);
			tw.addEndElement();
			tw.endDocument();
			chunks.add(new Chunk(tw.getResult(), rightMarkName));
		}
		
		
		return chunks;
	}
}
