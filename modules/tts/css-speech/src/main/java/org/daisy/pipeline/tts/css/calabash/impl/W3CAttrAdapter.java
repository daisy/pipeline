package org.daisy.pipeline.tts.css.calabash.impl;

import net.sf.saxon.dom.AttrOverNodeInfo;
import net.sf.saxon.om.NodeInfo;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class W3CAttrAdapter extends AttrOverNodeInfo implements Adaptable {
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
	public Element getOwnerElement() {
		return (Element) WrapperOverWrapper.wrap(super.getOwnerElement());
	}

}
