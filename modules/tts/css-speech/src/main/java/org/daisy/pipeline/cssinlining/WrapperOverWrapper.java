package org.daisy.pipeline.cssinlining;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.dom.AttrOverNodeInfo;
import net.sf.saxon.dom.DOMNodeList;
import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.dom.PIOverNodeInfo;
import net.sf.saxon.dom.TextOverNodeInfo;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This wrapper and the W3C adapters allow us to override getAttributeNode() of
 * ElementOverNodeInfo so it does not take into account the prefix when
 * attributes are compared.
 */
public class WrapperOverWrapper {

	public static Node wrap(Node n) {
		if (n instanceof ElementOverNodeInfo) {
			W3CElementAdapter res = new W3CElementAdapter();
			res.setUnderlyingNode(((ElementOverNodeInfo) n)
			        .getUnderlyingNodeInfo());
			return res;
		}
		if (n instanceof DocumentOverNodeInfo) {
			W3CDocumentAdapter res = new W3CDocumentAdapter();
			res.setUnderlyingNode(((DocumentOverNodeInfo) n)
			        .getUnderlyingNodeInfo());
			return res;
		}
		if (n instanceof AttrOverNodeInfo) {
			W3CAttrAdapter res = new W3CAttrAdapter();
			res.setUnderlyingNode(((AttrOverNodeInfo) n)
			        .getUnderlyingNodeInfo());
			return res;
		}
		if (n instanceof TextOverNodeInfo) {
			W3CTextAdapter res = new W3CTextAdapter();
			res.setUnderlyingNode(((TextOverNodeInfo) n)
			        .getUnderlyingNodeInfo());
			return res;
		}
		if (n instanceof PIOverNodeInfo) {
			W3CPiAdapter res = new W3CPiAdapter();
			res.setUnderlyingNode(((PIOverNodeInfo) n).getUnderlyingNodeInfo());
			return res;
		}
		return n;
	}

	public static NodeList wrap(NodeList l) {
		List<Node> res = new ArrayList<Node>(10);
		for (int i = 0; i < l.getLength(); ++i)
			res.add(wrap(l.item(i)));
		return new DOMNodeList(res);
	}

}
