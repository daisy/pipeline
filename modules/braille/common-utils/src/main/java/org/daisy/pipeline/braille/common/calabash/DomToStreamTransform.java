package org.daisy.pipeline.braille.common.calabash;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.collect.Iterables.toArray;
import static com.google.common.collect.Iterators.addAll;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.util.TreeWriter;

import net.sf.saxon.dom.DocumentOverNodeInfo;
import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NameOfNode;
import net.sf.saxon.om.NamespaceBinding;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.tree.util.NamespaceIterator;

import org.daisy.pipeline.braille.common.TransformationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public abstract class DomToStreamTransform {
	
	private final XProcRuntime runtime;
	
	public DomToStreamTransform(XProcRuntime runtime) {
		this.runtime = runtime;
	}
	
	protected abstract void _transform(Document document, Writer writer) throws TransformationException;
	
	public final XdmNode transform(XdmNode document) throws TransformationException {
		Document doc = (Document)DocumentOverNodeInfo.wrap(document.getUnderlyingNode());
		WriterImpl writer = new WriterImpl(runtime);
		_transform(doc, writer);
		return writer.getResult();
	}
	
	protected interface Writer {
		public void startDocument(URI base);
		public void startContent();
		public void addAttribute(QName name, String value);
		public void endElement();
		public void endDocument();
		public void copyStartElement(Element element);
		public void copyAttribute(Node attr);
		public void copyText(Node text);
		public void copyComment(Node node);
		public void copyPI(Node pi);
	}
	
	private static class WriterImpl extends TreeWriter implements Writer {
		
		WriterImpl(XProcRuntime runtime) {
			super(runtime);
		}
		
		public void endElement() {
			addEndElement();
		}
		
		public void copyStartElement(Element element) {
			NodeInfo inode = ((NodeOverNodeInfo)element).getUnderlyingNodeInfo();
			NamespaceBinding[] inscopeNS = null;
			if (seenRoot)
				inscopeNS = inode.getDeclaredNamespaces(null);
			else {
				List<NamespaceBinding> namespaces = new ArrayList<NamespaceBinding>();
				addAll(namespaces, NamespaceIterator.iterateNamespaces(inode));
				inscopeNS = toArray(namespaces, NamespaceBinding.class);
				seenRoot = true; }
			receiver.setSystemId(element.getBaseURI());
			addStartElement(new NameOfNode(inode), inode.getSchemaType(), inscopeNS);
		}
		
		public void copyAttribute(Node attr) {
			if ("http://www.w3.org/2000/xmlns/".equals(attr.getNamespaceURI())) {}
			else if (attr.getPrefix() != null)
				addAttribute(new QName(attr.getPrefix(), attr.getNamespaceURI(), attr.getLocalName()), attr.getNodeValue());
			else
				addAttribute(new QName(attr.getNamespaceURI(), attr.getLocalName()), attr.getNodeValue());
		}
		
		public void copyText(Node text) {
			addText(text.getNodeValue());
		}
	
		public void copyComment(Node node) {
			addComment(node.getNodeValue());
		}
	
		public void copyPI(Node pi) {
			addPI(pi.getLocalName(), pi.getNodeValue());
		}
	}
}
