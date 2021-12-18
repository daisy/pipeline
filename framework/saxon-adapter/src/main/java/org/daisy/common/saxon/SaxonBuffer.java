package org.daisy.common.saxon;

import org.daisy.common.transform.Buffer;
import org.daisy.common.transform.TransformerException;

import net.sf.saxon.Configuration;
import net.sf.saxon.s9api.XdmItem;

public class SaxonBuffer extends Buffer<Void,Void> {

	private final Configuration configuration;
	private SaxonInputValue input = null;
	private SaxonOutputValue output = null;
	private ItemBuffer<XdmItem> xdmItemBuffer = null;

	public SaxonBuffer(Configuration config) {
		configuration = config;
	}

	@Override
	public void done() {
		if (xdmItemBuffer == null)
			xdmItemBuffer = new ItemBuffer<>();
		xdmItemBuffer.done();
	}

	@Override
	public void error(TransformerException e) {
		if (xdmItemBuffer == null)
			xdmItemBuffer = new ItemBuffer<>();
		xdmItemBuffer.error(e);
	}

	@Override
	public SaxonInputValue asInput() {
		if (input == null) {
			if (xdmItemBuffer == null)
				xdmItemBuffer = new ItemBuffer<>();
			input = new SaxonInputValue(xdmItemBuffer);
		}
		return input;
	}

	@Override
	public SaxonOutputValue asOutput() {
		if (output == null) {
			if (xdmItemBuffer == null)
				xdmItemBuffer = new ItemBuffer<>();
			output = new SaxonOutputValue(xdmItemBuffer, configuration);
		}
		return output;
	}
}