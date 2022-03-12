package org.daisy.pipeline.braille.css.saxon.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceType;

import org.daisy.common.saxon.SaxonInputValue;
import org.daisy.common.saxon.SaxonOutputValue;
import org.daisy.common.transform.TransformerException;

import org.osgi.service.component.annotations.Component;

@Component(
	name = "css:render-table-by",
	service = { ExtensionFunctionDefinition.class }
)
public class RenderTableByDefinition extends ExtensionFunctionDefinition {
	
	private static final String XMLNS_CSS = "http://www.daisy.org/ns/pipeline/braille-css";
	
	private static final StructuredQName funcname = new StructuredQName("css", XMLNS_CSS, "render-table-by");
	
	public StructuredQName getFunctionQName() {
		return funcname;
	}
	
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] {
			SequenceType.SINGLE_STRING,
			SequenceType.SINGLE_NODE // SINGLE_ELEMENT_NODE
		};
	}
	
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.SINGLE_NODE; // SINGLE_ELEMENT_NODE
	}
	
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {
			public Sequence call(XPathContext context, Sequence[] arguments) throws XPathException {
				try {
					String axes = arguments[0].head().getStringValue();
					NodeInfo tableElement = (NodeInfo)arguments[1].head();
					
					// FIXME: why does this not work?
					// URI base = new URI(tableElement.getBaseURI());
					List<XdmItem> result = new ArrayList<>();
					new TableAsList(axes)
					.transform(
						new SaxonInputValue(tableElement),
						new SaxonOutputValue(result::add, context.getConfiguration()))
					.run();
					if (result.size() != 1 || !(result.get(0) instanceof XdmNode))
						throw new RuntimeException(); // should not happen
					return ((XdmNode)result.get(0)).getUnderlyingNode();
				} catch (TransformerException e) {
					throw new XPathException(e.getMessage(), e.getCause());
				} catch (Throwable e) {
					throw new XPathException("Unexpected error in css:render-table-by", e);
				}
			}
		};
	}
}
