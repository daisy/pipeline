package org.daisy.pipeline.tts;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmSequenceIterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.xmlcalabash.util.TreeWriter;

public class DefaultSSMLMarkSplitter implements SSMLMarkSplitter {

	private static Logger ServerLogger = LoggerFactory
	        .getLogger(DefaultSSMLMarkSplitter.class);

	private Processor mProc;

	public DefaultSSMLMarkSplitter(Processor proc) {
		mProc = proc;
	}

	@Override
	public Collection<Chunk> split(XdmNode ssml) {
		List<String> sortedMarkNames = new ArrayList<String>();
		Multimap<NodeInfo, String> marksScope = HashMultimap.create();
		sortedMarkNames.add(null); //null means 'no mark'
		findMarks(ssml, marksScope, sortedMarkNames);

		Collection<Chunk> chunks = new ArrayList<Chunk>();

		if (ssml.getNodeKind() == XdmNodeKind.DOCUMENT) {
			XdmSequenceIterator iter = ssml.axisIterator(Axis.CHILD);
			while (ssml.getNodeKind() != XdmNodeKind.ELEMENT && iter.hasNext()) {
				ssml = (XdmNode) iter.next();
			}
		}
		URI docURI = null;
		try {
			docURI = new URI("http://tmp");
		} catch (URISyntaxException e) {
		}

		for (String markName : sortedMarkNames) {
			TreeWriter tw = new TreeWriter(mProc);
			tw.startDocument(docURI);
			tw.startContent();
			toXML(ssml, tw, markName, marksScope);
			tw.endDocument();
			chunks.add(new Chunk(tw.getResult(), markName));
		}

		return chunks;
	}

	private static void findMarks(XdmNode ssml, Multimap<NodeInfo, String> marksScope,
	        List<String> sortedMarkNames) {
		String markName = sortedMarkNames.get(sortedMarkNames.size() - 1);
		marksScope.put(ssml.getUnderlyingNode(), markName);
		XdmSequenceIterator iter = ssml.axisIterator(Axis.ANCESTOR);
		while (iter.hasNext()) {
			XdmNode parent = (XdmNode) iter.next();
			marksScope.put(parent.getUnderlyingNode(), markName);
		}
		if (ssml.getNodeName() != null
		        && markNode.getLocalName().equals(ssml.getNodeName().getLocalName())) {
			sortedMarkNames.add(ssml.getAttributeValue(markNameAttr));
		}
		iter = ssml.axisIterator(Axis.CHILD);
		while (iter.hasNext()) {
			findMarks((XdmNode) iter.next(), marksScope, sortedMarkNames);
		}
	}

	private static void toXML(XdmNode ssml, TreeWriter tw, String markName,
	        Multimap<NodeInfo, String> marksScope) {
		if (!marksScope.containsEntry(ssml.getUnderlyingNode(), markName)) {
			return;
		}
		if (ssml.getNodeKind() == XdmNodeKind.TEXT) {
			tw.addText(ssml.getStringValue());
		} else if (ssml.getNodeKind() == XdmNodeKind.ELEMENT
		        && !markNode.getLocalName().equals(ssml.getNodeName().getLocalName())) {
			QName elementName = ssml.getNodeName();
			if (elementName != null) {
				tw.addStartElement(ssml);
				tw.addAttributes(ssml);
			}

			XdmSequenceIterator iter = ssml.axisIterator(Axis.CHILD);
			while (iter.hasNext()) {
				toXML((XdmNode) iter.next(), tw, markName, marksScope);
			}

			if (elementName != null) {
				tw.addEndElement();
			}
		}
	}

	private static final QName markNode = new QName("mark");
	private static final QName markNameAttr = new QName("name");
}
