package org.daisy.pipeline.tts.css.calabash.impl;

import net.sf.saxon.om.NodeInfo;

/**
 * Common interface for the W3C adapters
 */
public interface Adaptable {
	public void setUnderlyingNode(NodeInfo n);
}
