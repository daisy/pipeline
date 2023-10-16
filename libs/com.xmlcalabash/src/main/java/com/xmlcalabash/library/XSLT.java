/*
 * XSLT.java
 *
 * Copyright 2008 Mark Logic Corporation.
 * Portions Copyright 2007 Sun Microsystems, Inc.
 * All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * https://xproc.dev.java.net/public/CDDL+GPL.html or
 * docs/CDDL+GPL.txt in the distribution. See the License for the
 * specific language governing permissions and limitations under the
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at docs/CDDL+GPL.txt.
 */

package com.xmlcalabash.library;

import com.xmlcalabash.core.XMLCalabash;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.runtime.XAtomicStep;
import com.xmlcalabash.util.MessageFormatter;
import com.xmlcalabash.util.S9apiUtils;
import com.xmlcalabash.util.TreeWriter;
import com.xmlcalabash.util.XProcCollectionFinder;
import net.sf.saxon.Configuration;
import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.parser.Location;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.lib.OutputURIResolver;
import net.sf.saxon.lib.UnparsedTextURIResolver;
import net.sf.saxon.om.NamespaceBindingSet;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.om.TreeModel;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.MessageListener;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.Serializer;
import net.sf.saxon.s9api.ValidationMode;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltExecutable;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import net.sf.saxon.type.SimpleType;
import org.xml.sax.InputSource;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Vector;

/**
 *
 * @author ndw
 */

@XMLCalabash(
        name = "p:xslt",
        type = "{http://www.w3.org/ns/xproc}xslt")

public class XSLT extends DefaultStep {
    private static final StructuredQName TERMINATION_ERROR = new StructuredQName("err", NamespaceConstant.ERR, "XTMM9000");
    private static final QName _initial_mode = new QName("", "initial-mode");
    private static final QName _template_name = new QName("", "template-name");
    private static final QName _output_base_uri = new QName("", "output-base-uri");
    private static final QName _version = new QName("", "version");
    private static final QName _content_type = new QName("content-type");
    private static final QName cx_decode = new QName("cx", XProcConstants.NS_CALABASH_EX, "decode");
    private static final QName cx_serialize = new QName("cx", XProcConstants.NS_CALABASH_EX, "serialize");
    private ReadablePipe sourcePipe = null;
    private ReadablePipe stylesheetPipe = null;
    private WritablePipe resultPipe = null;
    private WritablePipe secondaryPipe = null;
    private Hashtable<QName,RuntimeValue> params = new Hashtable<QName,RuntimeValue> ();
    private Hashtable<String, XdmDestination> secondaryResults = new Hashtable<String, XdmDestination> ();

    /*
     * Creates a new instance of XSLT
     */
    public XSLT(XProcRuntime runtime, XAtomicStep step) {
        super(runtime,step);
    }

    public void setInput(String port, ReadablePipe pipe) {
        if ("source".equals(port)) {
            sourcePipe = pipe;
        } else {
            stylesheetPipe = pipe;
        }
    }

    public void setOutput(String port, WritablePipe pipe) {
        if ("result".equals(port)) {
            resultPipe = pipe;
        } else {
            secondaryPipe = pipe;
        }
    }

    public void setParameter(QName name, RuntimeValue value) {
        params.put(name, value);
    }
    
    public void reset() {
        sourcePipe.resetReader();
        stylesheetPipe.resetReader();
        resultPipe.resetWriter();
        secondaryPipe.resetWriter();
    }

    public void run() throws SaxonApiException {
        super.run();

        XdmNode stylesheet = stylesheetPipe.read();
        if (stylesheet == null) {
            throw XProcException.dynamicError(6, step, "No stylesheet provided.");
        }

        Vector<XdmNode> defaultCollection = new Vector<XdmNode> ();

        while (sourcePipe.moreDocuments()) {
            defaultCollection.add(sourcePipe.read());
        }

        XdmNode document = null;
        if (defaultCollection.size() > 0) {
            document = defaultCollection.firstElement();
        }

        String version = null;
        if (getOption(_version) == null) {
            XdmNode ssroot = S9apiUtils.getDocumentElement(stylesheet);
            if (ssroot != null) {
                version = ssroot.getAttributeValue(new QName("","version"));
                if (version == null) {
                    version = ssroot.getAttributeValue(new QName("http://www.w3.org/1999/XSL/Transform","version"));
                }
            }
            if (version == null) {
                version = "2.0"; // WTF?
            }
        } else {
            version = getOption(_version).getString();
        }
        
        // We used to check if the XSLT version was supported, but I've removed that check.
        // If it's not supported by Saxon, we'll get an error from Saxon. Otherwise, we'll
        // get the results we get.

        if ("1.0".equals(version) && defaultCollection.size() > 1) {
            throw XProcException.stepError(39);
        }
        
        if ("1.0".equals(version) && runtime.getUseXslt10Processor()) {
            run10(stylesheet, document);
            return;
        }

        QName initialMode = null;
        QName templateName = null;
        URI outputBaseURI = null;

        RuntimeValue opt = getOption(_initial_mode);
        if (opt != null) {
            initialMode = opt.getQName();
        }

        opt = getOption(_template_name);
        if (opt != null) {
            templateName = opt.getQName();
        }

        opt = getOption(_output_base_uri);
        if (opt != null) {
            outputBaseURI = opt.getBaseURI().resolve(opt.getString());
        }

        Processor processor = runtime.getProcessor();
        Configuration config = processor.getUnderlyingConfiguration();

        runtime.getConfigurer().getSaxonConfigurer().configXSLT(config);

        OutputURIResolver uriResolver = config.getOutputURIResolver();
        CollectionFinder collectionFinder = config.getCollectionFinder();
        UnparsedTextURIResolver unparsedTextURIResolver = runtime.getResolver();

        config.setOutputURIResolver(new OutputResolver());
        config.setDefaultCollection(XProcCollectionFinder.DEFAULT);
        config.setCollectionFinder(new XProcCollectionFinder(runtime, defaultCollection, collectionFinder));

        XdmDestination result = null;
        ByteArrayOutputStream outputStream = null;

        try {
            XsltCompiler compiler = runtime.getProcessor().newXsltCompiler();
            compiler.setSchemaAware(processor.isSchemaAware());
            compiler.setErrorListener(new ReportCompileErrors());
            XsltExecutable exec;
            try {
                exec = compiler.compile(stylesheet.asSource());
            } catch (SaxonApiException sae) {
                // catch compilation errors
                Throwable e = sae.getCause();
                if (e instanceof TransformerException) {
                    // Actually this exception does not contain location info (but we pass it
                    // anyway) and the message is always "Errors were reported during stylesheet
                    // compilation". More info including location of the compilation errors are
                    // contained in the TransformerException that are passed to ReportCompileErrors.
                    TransformerException location = (TransformerException)e;
                    Throwable cause = e.getCause();
                    if (cause != null)
                        throw new XProcException(location, e, XProcException.fromException(cause));
                    else
                        throw new XProcException(location, e);
                }
                throw XProcException.fromException(sae);
            }
            XsltTransformer transformer = exec.load();

            for (QName name : params.keySet()) {
                RuntimeValue v = params.get(name);
                if (runtime.getAllowGeneralExpressions()) {
                    transformer.setParameter(name, v.getValue());
                } else {
                    transformer.setParameter(name, v.getUntypedAtomic(runtime));
                }
            }

            if (document != null) {
                transformer.setInitialContextNode(document);
            }
            CatchMessages catchMessages = new CatchMessages();
            transformer.setMessageListener(catchMessages);
            if (Boolean.parseBoolean(step.getExtensionAttribute(cx_serialize))) {
                Serializer serializer = makeSerializer();
                outputStream = new ByteArrayOutputStream();
                serializer.setOutputStream(outputStream);
                transformer.setDestination(serializer);
            } else {
                result = new XdmDestination();
                result.setTreeModel(TreeModel.getTreeModel(runtime.getProcessor().getUnderlyingConfiguration().getTreeModel()));
                transformer.setDestination(result);
            }

            if (initialMode != null) {
                transformer.setInitialMode(initialMode);
            }

            if (templateName != null) {
                transformer.setInitialTemplate(templateName);
            }

            if (outputBaseURI != null) {
                transformer.setBaseOutputURI(outputBaseURI.toASCIIString());
                if (result != null) {
                    // The following hack works around https://saxonica.plan.io/issues/1724
                    result.setBaseURI(outputBaseURI);
                }
            }

            transformer.setSchemaValidationMode(ValidationMode.DEFAULT);
            transformer.getUnderlyingController().setUnparsedTextURIResolver(unparsedTextURIResolver);
            try {
                transformer.transform();
            } catch (SaxonApiException sae) {
                if (sae.getCause() instanceof TransformerException) {
                    TransformerException e = (TransformerException)sae.getCause();
                    QName code = null; {
                        if (e instanceof XPathException) {
                            StructuredQName qn = ((XPathException)e).getErrorCodeQName();
                            if (qn != null && !TERMINATION_ERROR.equals(qn)) code = new QName(qn);
                        } else
                            code = sae.getErrorCode();
                    }
                    XdmNode message = null;
                    if (e instanceof TerminationException) {
                        message = catchMessages.getTerminatingMessage();
                    }
                    TransformerException location = e;
                    Throwable cause = e.getCause();
                    if (cause != null) {
                        XProcException errorCause = XProcException.fromException(cause)
                                                                  .rebase(null, new RuntimeException().getStackTrace());
                        if (message != null)
                            throw new XProcException(code, location, message, errorCause);
                        else
                            throw new XProcException(code, location, e, errorCause);
                    } else if (message != null)
                        throw new XProcException(code, location, message);
                    else
                        throw new XProcException(code, location, e);
                } else
                    throw XProcException.fromException(sae);
            }
        } catch (XProcException e) {
            e = e.rebase(step);
            step.reportError(e);
            throw e;
        } finally {
            config.setOutputURIResolver(uriResolver);
            config.setCollectionFinder(collectionFinder);
        }

        XdmNode xformed = result != null ? result.getXdmNode() : null;

        // Is null when cx:serialize attribute was specified or when nothing is written to the
        // principle result tree
        if (xformed != null) {
            if (getOption(_output_base_uri) == null && document != null) {
                // Before Saxon 9.8, it was possible to simply set the base uri of the
                // output document. That became impossible in Saxon 9.8, but I still
                // think there might be XProc pipelines that rely on the fact that the
                // base URI doesn't change when processed by XSLT. So we're doing it
                // the hard way.
                TreeWriter fixbase = new TreeWriter(runtime);
                fixbase.startDocument(document.getBaseURI());
                fixbase.addSubtree(xformed);
                fixbase.endDocument();
                xformed = fixbase.getResult();
            }

            // If the document isn't well-formed XML, encode it as text
            try {
                S9apiUtils.assertDocument(xformed);
                resultPipe.write(xformed);
            } catch (XProcException e) {
                // If the document isn't well-formed XML, encode it as text
                if (runtime.getAllowTextResults()) {
                    // Document is apparently not well-formed XML.
                    TreeWriter tree = new TreeWriter(runtime);
                    tree.startDocument(xformed.getBaseURI());
                    tree.addStartElement(XProcConstants.c_result);
                    tree.addAttribute(_content_type, "text/plain");
                    tree.addAttribute(cx_decode,"true");
                    tree.startContent();

                    // Serialize the content as text so that we don't wind up with encoded XML characters
                    Serializer serializer = makeSerializer();
                    serializer.setOutputProperty(Serializer.Property.METHOD, "text");

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    serializer.setOutputStream(baos);
                    try {
                        S9apiUtils.serialize(runtime, xformed, serializer);
                    } catch (SaxonApiException e2) {
                        throw new XProcException(e2);
                    }

                    try {
                        tree.addText(baos.toString("UTF-8"));
                    } catch (UnsupportedEncodingException ee) {
                        throw new RuntimeException(ee); // can not happen
                    }
                    tree.addEndElement();
                    tree.endDocument();
                    resultPipe.write(tree.getResult());
                } else {
                    throw new XProcException(step, new RuntimeException("p:xslt returned non-XML result", e.getCause()));
                }
            }
        }

        if (outputStream != null) {
            TreeWriter tree = new TreeWriter(runtime);
            tree.startDocument(outputBaseURI != null ? outputBaseURI : document != null ? document.getBaseURI() : null);
            tree.addStartElement(XProcConstants.c_result);
            tree.addAttribute(_content_type, "text/plain");
            tree.addAttribute(cx_decode, "true");
            tree.startContent();
            try {
                tree.addText(outputStream.toString("UTF-8")); // because makeSerializer() sets encoding output property to UTF-8
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e); // can not happen
            }
            tree.addEndElement();
            tree.endDocument();
            resultPipe.write(tree.getResult());
        }
    }
    
    public void run10(XdmNode stylesheet, XdmNode document) {
        try {
            InputSource is = S9apiUtils.xdmToInputSource(runtime, stylesheet);
            
            TransformerFactory tfactory = TransformerFactory.newInstance();
            Transformer transformer = tfactory.newTransformer(new SAXSource(is));

            transformer.setURIResolver(runtime.getResolver());

            for (QName name : params.keySet()) {
                RuntimeValue v = params.get(name);
                transformer.setParameter(name.getClarkName(), v.getString());
            }

            DOMResult result = new DOMResult();
            is = S9apiUtils.xdmToInputSource(runtime, document);
            transformer.transform(new SAXSource(is), result);

            DocumentBuilder xdmBuilder = runtime.getConfiguration().getProcessor().newDocumentBuilder();
            XdmNode xformed = xdmBuilder.build(new DOMSource(result.getNode()));

            // Can be null when nothing is written to the principle result tree...
            if (xformed != null) {
                // There used to be an attempt to set the system identifier of the xformed
                // document, but that's not allowed in Saxon 9.8.
                resultPipe.write(xformed);
            }
        } catch (SaxonApiException sae) {
            throw new XProcException(sae);
        } catch (TransformerConfigurationException tce) {
            throw new XProcException(tce);
        } catch (TransformerException te) {
            throw new XProcException(te);
        }
    }

    class OutputResolver implements OutputURIResolver {
        public OutputResolver() {
        }

        @Override
        public OutputURIResolver newInstance() {
            return new OutputResolver();
        }

        public Result resolve(String href, String base) throws TransformerException {
            URI baseURI = null;
            try {
                baseURI = new URI(base);
                baseURI = baseURI.resolve(href);
            } catch (URISyntaxException use) {
                throw new XProcException(use);
            }

            logger.trace(MessageFormatter.nodeMessage(step.getNode(), "XSLT secondary result document: " + baseURI));

            try {
                XdmDestination xdmResult = new XdmDestination();
                xdmResult.setTreeModel(TreeModel.getTreeModel(runtime.getProcessor().getUnderlyingConfiguration().getTreeModel()));
                secondaryResults.put(baseURI.toASCIIString(), xdmResult);
                Receiver receiver = xdmResult.getReceiver(runtime.getProcessor().getUnderlyingConfiguration());
                return new FixedSysidReceiver(receiver, baseURI.toASCIIString());
            } catch (SaxonApiException sae) {
                throw new XProcException(sae);
            }
        }

        public void close(Result result) throws TransformerException {
            String href = result.getSystemId();
            XdmDestination xdmResult = secondaryResults.get(href);
            XdmNode doc = xdmResult.getXdmNode();

            try {
                S9apiUtils.assertDocument(doc);
                secondaryPipe.write(doc);
            } catch (XProcException e) {
                // If the document isn't well-formed XML, encode it as text
                if (runtime.getAllowTextResults()) {
                    // Document is apparently not well-formed XML.
                    TreeWriter tree = new TreeWriter(runtime);
                    tree.startDocument(doc.getBaseURI());
                    tree.addStartElement(XProcConstants.c_result);
                    tree.addAttribute(_content_type, "text/plain");
                    tree.addAttribute(cx_decode, "true");
                    tree.startContent();
                    tree.addText(doc.toString());
                    tree.addEndElement();
                    tree.endDocument();
                    secondaryPipe.write(tree.getResult());
                } else {
                    throw new XProcException(
                        step, new RuntimeException("p:xslt returned non-XML secondary result", e.getCause()));
                }
            }
        }
    }

    class CatchMessages implements MessageListener {
        
        XdmNode terminatingMessage = null;

        public void message(XdmNode content, boolean terminate, SourceLocator locator) {
            if (runtime.getShowMessages()) {
                System.err.println(content.toString());
            }

            TreeWriter treeWriter = new TreeWriter(runtime);
            treeWriter.startDocument(content.getBaseURI());
            treeWriter.addStartElement(XProcConstants.c_error);
            treeWriter.startContent();
            treeWriter.addSubtree(content);
            treeWriter.addEndElement();
            treeWriter.endDocument();

            if (!terminate)
                step.info(step.getNode(), content.toString());
            else
                terminatingMessage = content;
        }

        public XdmNode getTerminatingMessage() {
            return terminatingMessage;
        }
    }

    private static class FixedSysidReceiver implements Receiver
    {
        private final String   mySysid;
        private final Receiver myWrapped;

        public FixedSysidReceiver(Receiver wrapped, String sysid) {
            mySysid   = sysid;
            myWrapped = wrapped;
            myWrapped.setSystemId(sysid);
        }

        @Override
        public void open() throws XPathException {
            myWrapped.open();
        }

        @Override
        public void setUnparsedEntity(String name, String sysid, String pubid) throws XPathException {
            myWrapped.setUnparsedEntity(name, sysid, pubid);
        }

        @Override
        public String getSystemId() {
            return mySysid;
        }

        @Override
        public void setSystemId(String sysid) {
            // propagate it to the wrapped receiver, but do not take it into account here...
            myWrapped.setSystemId(sysid);
        }

        @Override
        public void setPipelineConfiguration(PipelineConfiguration conf) {
            myWrapped.setPipelineConfiguration(conf);
        }

        @Override
        public void startDocument(int i) throws XPathException {
            myWrapped.startDocument(i);
        }

        @Override
        public void endDocument() throws XPathException {
            myWrapped.endDocument();
        }

        @Override
        public void startElement(NodeName name, SchemaType st, Location loc, int i) throws XPathException {
            myWrapped.startElement(name, st, loc, i);
        }

        @Override
        public void namespace(NamespaceBindingSet namespaceBindings, int properties) throws XPathException {
            myWrapped.namespace(namespaceBindings, properties);
        }

        @Override
        public void attribute(NodeName name, SimpleType st, CharSequence cs, Location loc, int i) throws XPathException {
            myWrapped.attribute(name, st, cs, loc, i);
        }

        @Override
        public void startContent() throws XPathException {
            myWrapped.startContent();
        }

        @Override
        public void endElement() throws XPathException {
            myWrapped.endElement();
        }

        @Override
        public void characters(CharSequence cs, Location loc, int i) throws XPathException {
            myWrapped.characters(cs, loc, i);
        }

        @Override
        public void processingInstruction(String string, CharSequence cs, Location loc, int i) throws XPathException {
            myWrapped.processingInstruction(string, cs, loc, i);
        }

        @Override
        public void comment(CharSequence cs, Location loc, int i) throws XPathException {
            myWrapped.comment(cs, loc, i);
        }

        @Override
        public void close() throws XPathException {
            myWrapped.close();
        }

        @Override
        public boolean usesTypeAnnotations() {
            return myWrapped.usesTypeAnnotations();
        }

        @Override
        public PipelineConfiguration getPipelineConfiguration() {
            return myWrapped.getPipelineConfiguration();
        }
    }

    private class ReportCompileErrors implements ErrorListener {
        // log error
        public void error(TransformerException exception) {
            logger.error(exception.getMessage());
        }
        // report fatal error
        public void fatalError(TransformerException exception) {
            step.reportError(new XProcException(exception, exception));
        }
        // log warning
        public void warning(TransformerException exception) {
            logger.warn(exception.getMessage());
        }
    }
}
