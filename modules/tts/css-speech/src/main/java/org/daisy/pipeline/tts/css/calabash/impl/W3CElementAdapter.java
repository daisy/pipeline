package org.daisy.pipeline.tts.css.calabash.impl;

import net.sf.saxon.dom.ElementOverNodeInfo;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.tree.iter.AxisIterator;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class W3CElementAdapter extends ElementOverNodeInfo implements Adaptable {

	// OVERRIDING OF NodeOverNodeINFO //

	@Override
	public Node getParentNode() {
		return WrapperOverWrapper.wrap(super.getParentNode());
	}

	@Override
	public Node getPreviousSibling() {
		return WrapperOverWrapper.wrap(super.getPreviousSibling());
	}

	@Override
	public Node getNextSibling() {
		return WrapperOverWrapper.wrap(super.getNextSibling());
	}

	@Override
	public Node getFirstChild() {
		return WrapperOverWrapper.wrap(super.getFirstChild());
	}

	@Override
	public Node getLastChild() {
		return WrapperOverWrapper.wrap(super.getLastChild());
	}

	@Override
	public NodeList getChildNodes() {
		return WrapperOverWrapper.wrap(super.getChildNodes());
	}

	@Override
	public Document getOwnerDocument() {
		return (Document) WrapperOverWrapper.wrap(super.getOwnerDocument());
	}

	public void setUnderlyingNode(NodeInfo n) {
		this.node = n;
	}

	// ///////////////////////////////////////

	@Override
	public Attr getAttributeNode(String name) {
		if (name == null || name.equals(""))
			return null;

		AxisIterator atts = node.iterateAxis(AxisInfo.ATTRIBUTE);
		while (true) {
			NodeInfo att = (NodeInfo) atts.next();
			if (att == null) {
				return null;
			}
			// the only difference is here: local name is used instead of
			// the full QName
			if (att.getLocalPart().equals(name)) {
				return (Attr) att;
			}
		}
	}

	@Override
	public String getAttribute(String name) {
		AxisIterator atts = node.iterateAxis(AxisInfo.ATTRIBUTE);
		while (true) {
			NodeInfo att = atts.next();
			if (att == null) {
				return "";
			}
			if (att.getLocalPart().equals(name)) {
				String val = att.getStringValue();
				if (val == null)
					return "";
				return val;
			}
		}
	}

	@Override
	public Attr getAttributeNodeNS(String namespaceURI, String localName) {
		return (Attr) WrapperOverWrapper.wrap(super.getAttributeNodeNS(
		        namespaceURI, localName));
	}
}
