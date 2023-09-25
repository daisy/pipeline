package com.xmlcalabash.runtime;

import com.xmlcalabash.util.AxisNodes;
import com.xmlcalabash.util.MessageFormatter;
import com.xmlcalabash.util.S9apiUtils;
import com.xmlcalabash.util.TypeUtils;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcStep;
import com.xmlcalabash.core.XProcData;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.io.ReadableInline;
import com.xmlcalabash.io.ReadableDocument;
import com.xmlcalabash.io.ReadOnlyPipe;
import com.xmlcalabash.io.Pipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.model.Binding;
import com.xmlcalabash.model.PipeNameBinding;
import com.xmlcalabash.model.InlineBinding;
import com.xmlcalabash.model.DocumentBinding;
import com.xmlcalabash.model.DataBinding;
import com.xmlcalabash.model.Input;
import com.xmlcalabash.model.Output;
import com.xmlcalabash.model.Parameter;
import com.xmlcalabash.model.ComputableValue;
import com.xmlcalabash.model.NamespaceBinding;
import com.xmlcalabash.model.DeclareStep;
import com.xmlcalabash.model.Option;
import com.xmlcalabash.model.SequenceType;
import com.xmlcalabash.util.TreeWriter;
import com.xmlcalabash.util.XProcCollectionFinder;
import com.xmlcalabash.util.XProcMessageListenerHelper;
import net.sf.saxon.Configuration;
import net.sf.saxon.lib.CollectionFinder;
import net.sf.saxon.om.InscopeNamespaceResolver;
import net.sf.saxon.om.NameChecker;
import net.sf.saxon.om.NamePool;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.XdmNodeKind;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathExecutable;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmDestination;
import net.sf.saxon.s9api.SaxonApiUncheckedException;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.HashSet;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 8, 2008
 * Time: 5:25:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class XAtomicStep extends XStep {
    private final static QName _name = new QName("", "name");
    private final static QName _namespace = new QName("", "namespace");
    private final static QName _value = new QName("", "value");
    private final static QName _type = new QName("", "type");
    private final static QName cx_item = new QName("cx", XProcConstants.NS_CALABASH_EX, "item");

    public XAtomicStep(XProcRuntime runtime, Step step, XCompoundStep parent) {
        super(runtime, step);
        this.parent = parent;
        if (parent != null)
            this.parentLocation = parent.getLocation();
    }

    public XCompoundStep getParent() {
        return parent;
    }

    public boolean hasReadablePipes(String port) {
        if (inputs.containsKey(port)) {
            return inputs.get(port).size() > 0;
        } else {
            return false;
        }
    }

    public boolean hasWriteablePipe(String port) {
        return outputs.containsKey(port);
    }

    public RuntimeValue optionAvailable(QName optName) {
        if (!inScopeOptions.containsKey(optName)) {
            return null;
        }
        return inScopeOptions.get(optName);
    }

    protected ReadablePipe getPipeFromBinding(Binding binding) {
        ReadablePipe pipe = null;
        if (binding.getBindingType() == Binding.PIPE_NAME_BINDING) {
            PipeNameBinding pnbinding = (PipeNameBinding) binding;

            // Special case, if we're in a compound step (e.g., if we're getting a
            // binding for a variable in a pipeline), then we can read from ourself.
            XCompoundStep start = parent;
            if (this instanceof XCompoundStep) {
                start = (XCompoundStep) this;
            }

            pipe = start.getBinding(pnbinding.getStep(), pnbinding.getPort());
            pipe.setNames(pnbinding.getStep(), pnbinding.getPort());
        } else if (binding.getBindingType() == Binding.INLINE_BINDING) {
            InlineBinding ibinding = (InlineBinding) binding;
            pipe = new ReadableInline(runtime, ibinding.nodes(), ibinding.getExcludedNamespaces());
        } else if (binding.getBindingType() == Binding.EMPTY_BINDING) {
            pipe = new ReadableDocument(runtime);
        } else if (binding.getBindingType() == Binding.DOCUMENT_BINDING) {
            DocumentBinding dbinding = (DocumentBinding) binding;
            pipe = runtime.getConfigurer().getXMLCalabashConfigurer().makeReadableDocument(runtime, dbinding);
        } else if (binding.getBindingType() == Binding.DATA_BINDING) {
            DataBinding dbinding = (DataBinding) binding;
            pipe = runtime.getConfigurer().getXMLCalabashConfigurer().makeReadableData(runtime, dbinding);
        } else if (binding.getBindingType() == Binding.ERROR_BINDING) {
            XCompoundStep step = parent;
            while (! (step instanceof XCatch)) {
                step = step.getParent();
            }
            pipe = step.getBinding(step.getName(), "error");
        } else {
            throw new XProcException(binding.getNode(), "Unknown binding type: " + binding.getBindingType());
        }

        pipe.setReader(step);
        return pipe;
    }

    protected void instantiateReaders(Step step) {
        for (Input input : step.inputs()) {
            String port = input.getPort();
            if (!port.startsWith("|")) {
                Vector<ReadablePipe> readers = null;
                if (inputs.containsKey(port)) {
                    readers = inputs.get(port);
                } else {
                    readers = new Vector<ReadablePipe> ();
                    inputs.put(port, readers);
                }
                for (Binding binding : input.getBinding()) {
                    ReadablePipe pipe = getPipeFromBinding(binding);
                    pipe.canReadSequence(input.getSequence());

                    if (input.getSelect() != null) {
                        logger.trace(MessageFormatter.nodeMessage(step.getNode(),
                                step.getName() + " selects from " + pipe + " for " + port));
                        pipe = new XSelect(runtime, this, pipe, input.getSelect(), input.getNode());
                    }

                    readers.add(pipe);
                    logger.trace(MessageFormatter.nodeMessage(step.getNode(),
                            step.getName() + " reads from " + pipe + " for " + port));
                }

                XInput xinput = new XInput(runtime, input);
                addInput(xinput);
            }
        }
    }

    public void instantiate(Step step) {
        instantiateReaders(step);

        for (Output output : step.outputs()) {
            String port = output.getPort();
            XOutput xoutput = new XOutput(runtime, output);
            xoutput.setLogger(step.getLog(port));
            addOutput(xoutput);
            Pipe wpipe = xoutput.getWriter();
            wpipe.canWriteSequence(output.getSequence());
            outputs.put(port, wpipe);
            logger.trace(MessageFormatter.nodeMessage(step.getNode(), step.getName() + " writes to " + wpipe + " for " + port));
        }

        parent.addStep(this);
    }

    protected void computeParameters(XProcStep xstep) throws SaxonApiException {
        // N.B. At this time, there are no compound steps that accept parameters or options,
        // so the order in which we calculate them doesn't matter. That will change if/when
        // there are such compound steps.

        // Loop through all the with-params and parameter input ports, adding
        // them to the xstep..

        // First, are there any parameters at all?
        Vector<String> paramPorts = new Vector<String> ();
        boolean primaryParamPort = false;
        for (Input input : step.inputs()) {
            if (input.getParameterInput()) {
                primaryParamPort = primaryParamPort | input.getPrimary();
                paramPorts.add(input.getPort());
            }
        }

        int position = 0;
        boolean loopdone = false;
        while (!loopdone) {
            position++;
            loopdone = true;

            for (Parameter p : step.parameters()) {
                if (XProcConstants.NS_XPROC.equals(p.getName().getNamespaceURI())) {
                    throw XProcException.dynamicError(31);
                }

                loopdone = (p.getPosition() <= position);
                if (p.getPosition() == position) {
                    loopdone = false;
                    if (!primaryParamPort) {
                        String port = p.getPort();
                        if (port == null) {
                            throw XProcException.staticError(34, step.getNode(), "No parameter input port.");
                        }
                        xstep.setParameter(p.getPort(), p.getName(), computeValue(p));
                    } else {
                        xstep.setParameter(p.getName(), computeValue(p));
                    }
                }
            }

            for (String port : paramPorts) {
                Input input = step.getInput(port);
                loopdone = loopdone && (input.getPosition() <= position);
                if (input.getPosition() == position) {
                    for (ReadablePipe source : inputs.get(port)) {
                        while (source.moreDocuments()) {
                            XdmNode node = source.read();
                            XdmNode docelem = S9apiUtils.getDocumentElement(node);

                            if (XProcConstants.c_param_set.equals(docelem.getNodeName())) {
                                // Check the attributes...
                                for (XdmNode attr : new AxisNodes(docelem, Axis.ATTRIBUTE)) {
                                    QName aname = attr.getNodeName();
                                    if ("".equals(aname.getNamespaceURI())
                                        || XProcConstants.NS_XPROC.equals(aname.getNamespaceURI())) {
                                        throw XProcException.dynamicError(14, step.getNode(), "Attribute not allowed");
                                    }
                                }

                                for (XdmNode child : new AxisNodes(runtime, docelem, Axis.CHILD, AxisNodes.SIGNIFICANT)) {
                                    if (child.getNodeKind() == XdmNodeKind.ELEMENT) {
                                        if (!child.getNodeName().equals(XProcConstants.c_param)) {
                                            throw XProcException.dynamicError(18, step.getNode(), "Element not allowed: " + child.getNodeName());
                                        }
                                        parseParameterNode(xstep,child);
                                    }
                                }
                            } else if (XProcConstants.c_param.equals(docelem.getNodeName())) {
                                parseParameterNode(xstep,docelem);
                            } else {
                                throw new XProcException(step, docelem.getNodeName() + " found where c:param or c:param-set expected");
                            }
                        }
                    }
                }
            }
        }
    }

    public void reset() {
        for (String port : inputs.keySet()) {
            for (ReadablePipe rpipe : inputs.get(port)) {
                rpipe.resetReader();
            }
        }

        for (String port : outputs.keySet()) {
            WritablePipe wpipe = outputs.get(port);
            wpipe.resetWriter();
        }

        clearOptions();

        clearParameters();
    }

    protected void doRun() throws SaxonApiException {
        XProcStep xstep = runtime.getConfiguration().newStep(runtime, this);

        // If there's more than one reader, collapse them all into a single reader
        for (String port : inputs.keySet()) {
            int totalDocs = 0; // FIXME: this will be more complicated when multiple threads are involved
            Input input = step.getInput(port);
            if (!input.getParameterInput()) {
                int readerCount = inputs.get(port).size();
                if (readerCount > 1) {
                    Pipe pipe = new Pipe(runtime);
                    pipe.setWriter(step);
                    pipe.setReader(step);
                    pipe.canWriteSequence(true);
                    pipe.canReadSequence(input.getSequence());
                    for (ReadablePipe reader : inputs.get(port)) {
                        if (reader.moreDocuments()) {
                            while (reader.moreDocuments()) {
                                XdmNode doc = reader.read();
                                pipe.write(doc);
                                totalDocs++;
                            }
                        } else if (reader instanceof ReadableDocument) {
                            // HACK: We haven't necessarily read the document yet
                            totalDocs++;
                        }
                    }
                    xstep.setInput(port, pipe);
                } else if (readerCount == 1) {
                    ReadablePipe pipe = inputs.get(port).firstElement();
                    pipe.setReader(step);
                    if (pipe.moreDocuments()) {
                        totalDocs += pipe.documentCount();
                    } else if (pipe instanceof ReadableDocument) {
                        totalDocs++;
                    }
                    xstep.setInput(port, pipe);
                }
            }

            if (totalDocs != 1 && !input.getSequence()) {
                throw XProcException.dynamicError(6, step.getNode(), totalDocs + " documents appear on the '" + port + "' port.");
            }
        }

        for (String port : outputs.keySet()) {
            xstep.setOutput(port, outputs.get(port));
        }

        // N.B. At this time, there are no compound steps that accept parameters or options,
        // so the order in which we calculate them doesn't matter. That will change if/when
        // there are such compound steps.

        // Calculate all the options
        DeclareStep decl = step.getDeclaration();
        inScopeOptions = parent.getInScopeOptions();
        Hashtable<QName,RuntimeValue> futureOptions = new Hashtable<QName,RuntimeValue> ();
        for (QName name : step.getOptions()) {
            Option option = step.getOption(name);
            RuntimeValue value = computeValue(option);

            // Test to see if the option has a reasonable string value according to the declaration
            Option optionDecl = decl.getOption(name);
            if (optionDecl.getTypeAsQName() != null) {
                TypeUtils.checkType(runtime,
                                    value.hasGeneralValue() ? value.getValue() : null,
                                    value.getString(),
                                    optionDecl.getTypeAsQName(),
                                    option.getNode());
            } else if (optionDecl.getType() != null) {
                String type = optionDecl.getType();
                if (type.contains("|")) {
                    TypeUtils.checkLiteral(value.getString(), type);
                }
            }

            xstep.setOption(name, value);
            futureOptions.put(name, value);
        }

        for (QName opt : futureOptions.keySet()) {
            inScopeOptions.put(opt, futureOptions.get(opt));
        }

        xstep.reset();
        computeParameters(xstep);

        // HACK HACK HACK!
        if (XProcConstants.p_in_scope_names.equals(step.getType())) {
            for (QName name : inScopeOptions.keySet()) {
                xstep.setParameter(name, inScopeOptions.get(name));
            }
        }
        
        // Make sure we do this *after* calculating any option/parameter values...
        XProcData data = runtime.getXProcData();
        data.openFrame(this);

        runtime.start(this);
        try {
            XProcMessageListenerHelper.openStep(runtime, this);
            try {
                xstep.run();
            } catch (RuntimeException e) {
                // If an unexpected exception happens while running a step, log the XProc stack
                // trace in order to aid debugging. With "unexpected exception" we mean an exception
                // that is not a XProcException or SaxonApiException: these are not allowed to
                // happen (if they do it's due to a bug), and are not caught by p:try.
                if (!(e instanceof XProcException)) {
                    // creating XProcException only to get the nice XProc stack trace
                    logger.error("An unexpected runtime exception happened: "
                                 + XProcException.fromException(e)
                                                 .rebase(getLocation(), new RuntimeException().getStackTrace())
                                                 .toString());
                }
                throw e;
            } finally {
                runtime.getMessageListener().closeStep();
            }

            // FIXME: Is it sufficient to only do this for atomic steps?
            String cache = getInheritedExtensionAttribute(XProcConstants.cx_cache);
            if ("true".equals(cache)) {
                for (String port : outputs.keySet()) {
                    WritablePipe wpipe = outputs.get(port);
                    // FIXME: Hack. There should be a better way...
                    if (wpipe instanceof Pipe) {
                        ReadablePipe rpipe = new ReadOnlyPipe(runtime, ((Pipe) wpipe).documents());
                        rpipe.canReadSequence(true);
                        rpipe.setReader(step);
                        while (rpipe.moreDocuments()) {
                            XdmNode doc = rpipe.read();
                            runtime.cache(doc, step.getNode().getBaseURI());
                        }
                    }
                }
            } else if (!"false".equals(cache) && cache != null) {
                throw XProcException.dynamicError(19);
            }
        } finally {
            // Note that closing the output pipes is the responsibility of the XProcStep. It may
            // choose to write an output lazily, when the port is read. If the XProcStep never
            // closes a pipe, that is fine too. The pipe will automatically be closed when it is
            // read for the first time.
            runtime.finish(this);
            data.closeFrame();
        }
    }

    public void reportError(XdmNode doc) {
        parent.reportError(doc);
    }

    public void reportError(XProcException exception) {
        TreeWriter treeWriter = new TreeWriter(runtime);
        treeWriter.startDocument(getNode().getBaseURI());
        exception.serialize(treeWriter);
        treeWriter.endDocument();
        reportError(treeWriter.getResult());
    }
    
    private void parseParameterNode(XProcStep impl, XdmNode pnode) {
        String value = pnode.getAttributeValue(_value);

        if (value == null && runtime.getAllowGeneralExpressions()) {
            parseParameterValueNode(impl, pnode);
            return;
        }

        Parameter p = new Parameter(step.getXProc(),pnode);
        String port = p.getPort();
        String name = pnode.getAttributeValue(_name);
        String ns = pnode.getAttributeValue(_namespace);

        QName pname = null;
        if (ns == null) {
            if (name.contains(":")) {
                pname = new QName(name,pnode);
            } else {
                pname = new QName(name);
            }
        } else {
            int pos = name.indexOf(":");
            if (pos > 0) {
                name = name.substring(pos);

                QName testNode = new QName(name,pnode);
                if (!ns.equals(testNode.getNamespaceURI())) {
                    throw XProcException.dynamicError(25);
                }

            }
            pname = new QName(ns,name);
        }

        if (XProcConstants.NS_XPROC.equals(pname.getNamespaceURI())) {
            throw XProcException.dynamicError(31);
        }

        p.setName(pname);

        for (XdmNode attr : new AxisNodes(pnode, Axis.ATTRIBUTE)) {
            QName aname = attr.getNodeName();
            if ("".equals(aname.getNamespaceURI())) {
                if (!aname.equals(_name) && !aname.equals(_namespace) && !aname.equals(_value)) {
                    throw XProcException.dynamicError(14);
                }
            }
        }

        if (port != null) {
            impl.setParameter(port,pname,new RuntimeValue(value,pnode));
        } else {
            impl.setParameter(pname,new RuntimeValue(value,pnode));
        }
    }

    private void parseParameterValueNode(XProcStep impl, XdmNode pnode) {
        Parameter p = new Parameter(step.getXProc(),pnode);
        String port = p.getPort();
        String name = pnode.getAttributeValue(_name);
        String ns = pnode.getAttributeValue(_namespace);

        QName pname = null;
        if (ns == null) {
            pname = new QName(name,pnode);
        } else {
            int pos = name.indexOf(":");
            if (pos > 0) {
                name = name.substring(pos);

                QName testNode = new QName(name,pnode);
                if (!ns.equals(testNode.getNamespaceURI())) {
                    throw XProcException.dynamicError(25);
                }

            }
            pname = new QName(ns,name);
        }

        p.setName(pname);

        for (XdmNode attr : new AxisNodes(pnode, Axis.ATTRIBUTE)) {
            QName aname = attr.getNodeName();
            if ("".equals(aname.getNamespaceURI())) {
                if (!aname.equals(_name) && !aname.equals(_namespace)) {
                    throw XProcException.dynamicError(14);
                }
            }
        }

        String stringValue = "";
        Vector<XdmItem> items = new Vector<XdmItem> ();
        for (XdmNode child : new AxisNodes(runtime, pnode, Axis.CHILD, AxisNodes.PIPELINE)) {
            if (child.getNodeKind() == XdmNodeKind.ELEMENT) {
               if (!child.getNodeName().equals(cx_item)) {
                    throw XProcException.dynamicError(18, step.getNode(), "Element not allowed: " + child.getNodeName());
                }

                String type = child.getAttributeValue(_type);
                if (type == null) {
                    Vector<XdmValue> nodes = new Vector<XdmValue> ();
                    URI baseURI = null;

                    XdmSequenceIterator iter = child.axisIterator(Axis.CHILD);
                    while (iter.hasNext()) {
                        XdmNode gchild = (XdmNode) iter.next();

                        if (baseURI == null && gchild.getNodeKind() == XdmNodeKind.ELEMENT) {
                            baseURI = gchild.getBaseURI();
                        }

                        nodes.add(gchild);
                    }

                    XdmDestination dest = new XdmDestination();

                    try {
                        if (baseURI == null) {
                            baseURI = new URI("http://example.com/"); // FIXME: do I need this?
                        }
                        S9apiUtils.writeXdmValue(runtime.getProcessor(), nodes, dest, baseURI);
                        XdmNode doc = dest.getXdmNode();
                        stringValue += doc.getStringValue();
                        items.add(doc);
                    } catch (URISyntaxException use) {
                        throw new XProcException(use);
                    } catch (SaxonApiException sae) {
                        throw new XProcException(sae);
                    }
                } else {
                    stringValue += child.getStringValue();
                    items.add(new XdmAtomicValue(child.getStringValue()));
                }
            }
        }

        RuntimeValue value = new RuntimeValue(stringValue, new XdmValue(items), pnode, new Hashtable<String,String> ());

        if (port != null) {
            impl.setParameter(port,pname,value);
        } else {
            impl.setParameter(pname,value);
        }
    }

    protected RuntimeValue computeValue(ComputableValue var) throws SaxonApiException {
        Hashtable<String,String> nsBindings = new Hashtable<String,String> ();
        Hashtable<QName,RuntimeValue> globals = inScopeOptions;
        XdmNode doc = null;
        Vector<XdmNode> defaultCollection = null;
        if (runtime.getAllowSequenceAsContext()) {
            defaultCollection = new Vector<XdmNode>();
        }

        try {
            if (var.getBinding().size() > 0) {
                Binding binding = var.getBinding().firstElement();

                ReadablePipe pipe = null;
                if (binding.getBindingType() == Binding.ERROR_BINDING) {
                    XStep step = this;
                    while (!(step instanceof XCatch)) {
                        step = step.getParent();
                    }
                    pipe = ((XCatch)step).errorPipe;
                } else {
                    pipe = getPipeFromBinding(binding);
                    pipe.canReadSequence(runtime.getAllowSequenceAsContext());
                }
                if (pipe.readSequence()) {
                    while (pipe.moreDocuments()) {
                        if (defaultCollection != null) {
                            if (doc == null) {
                                doc = pipe.read();
                                defaultCollection.add(doc);
                            } else {
                                defaultCollection.add(pipe.read());
                            }
                        } else if (doc == null) {
                            doc = pipe.read();
                        } else {
                            pipe.read();
                        }
                    }
                } else {
                    doc = pipe.read();
                    if (pipe.moreDocuments()) {
                        throw XProcException.dynamicError(
                            8, this, "More than one document in context for parameter '" + var.getName() + "'");
                    }
                }
            }
        } catch (SaxonApiException sae) {
            throw new XProcException(sae);
        }

        for (NamespaceBinding nsbinding : var.getNamespaceBindings()) {
            Hashtable<String,String> localBindings = new Hashtable<String,String> ();

            // Compute the namespaces associated with this binding
            if (nsbinding.getBinding() != null) {
                QName binding = new QName(nsbinding.getBinding(), nsbinding.getNode());
                RuntimeValue nsv = globals.get(binding);
                if (nsv == null) {
                    throw new XProcException(var.getNode(), "No in-scope option or variable named: " + binding);
                }

                localBindings = nsv.getNamespaceBindings();
            } else if (nsbinding.getXPath() != null) {
                try {
                    XPathCompiler xcomp = runtime.getProcessor().newXPathCompiler();
                    xcomp.setBaseURI(step.getNode().getBaseURI());

                    for (QName varname : globals.keySet()) {
                        xcomp.declareVariable(varname);
                    }

                    // Make sure the namespace bindings for evaluating the XPath expr are correct
                    // FIXME: Surely there's a better way to do this?
                    Hashtable<String,String> lclnsBindings = new Hashtable<String, String>();
                    NodeInfo inode = nsbinding.getNode().getUnderlyingNode();
                    NamePool pool = inode.getConfiguration().getNamePool();
                    InscopeNamespaceResolver inscopeNS = new InscopeNamespaceResolver(inode);
                    Iterator<?> pfxiter = inscopeNS.iteratePrefixes();
                    while (pfxiter.hasNext()) {
                        String nspfx = (String)pfxiter.next();
                        String nsuri = inscopeNS.getURIForPrefix(nspfx, "".equals(nspfx));
                        lclnsBindings.put(nspfx, nsuri);
                    }

                    for (String prefix : lclnsBindings.keySet()) {
                        xcomp.declareNamespace(prefix, lclnsBindings.get(prefix));
                    }

                    XPathExecutable xexec = xcomp.compile(nsbinding.getXPath());
                    XPathSelector selector = xexec.load();

                    for (QName varname : globals.keySet()) {
                        XdmAtomicValue avalue = new XdmAtomicValue(globals.get(varname).getString());
                        selector.setVariable(varname,avalue);
                    }

                    if (doc != null) {
                        selector.setContextItem(doc);
                    }

                    XdmNode element = null;
                    Iterator<XdmItem> values = selector.iterator();
                    while (values.hasNext()) {
                        XdmItem item = values.next();
                        if (element != null || item.isAtomicValue()) {
                            throw XProcException.dynamicError(9);
                        }
                        element = (XdmNode) item;
                        if (element.getNodeKind() != XdmNodeKind.ELEMENT) {
                            throw XProcException.dynamicError(9);
                        }
                    }

                    if (element == null) {
                        throw XProcException.dynamicError(9);
                    }

                    XdmSequenceIterator nsIter = element.axisIterator(Axis.NAMESPACE);
                    while (nsIter.hasNext()) {
                        XdmNode ns = (XdmNode) nsIter.next();
                        QName prefix = ns.getNodeName();
                        localBindings.put(prefix == null ? "" : prefix.getLocalName(),ns.getStringValue());
                    }
                } catch (SaxonApiException sae) {
                    throw new XProcException(sae);
                }
            } else if (nsbinding.getNamespaceBindings() != null) {
                Hashtable<String,String> bindings = nsbinding.getNamespaceBindings();
                for (String prefix : bindings.keySet()) {
                    if ("".equals(prefix) || prefix == null) {
                        // nop; the default namespace never plays a role in XPath expression evaluation
                    } else {
                        localBindings.put(prefix,bindings.get(prefix));
                    }
                }
            }

            // Remove the excluded ones
            HashSet<String> prefixes = new HashSet<String> ();
            for (String uri : nsbinding.getExcludedNamespaces()) {
                for (String prefix : localBindings.keySet()) {
                    if (uri.equals(localBindings.get(prefix))) {
                        prefixes.add(prefix);
                    }
                }
            }
            for (String prefix : prefixes) {
                localBindings.remove(prefix);
            }

            // Add them to the bindings for this value, making sure there are no errors...
            for (String pfx : localBindings.keySet()) {
                if (nsBindings.containsKey(pfx) && !nsBindings.get(pfx).equals(localBindings.get(pfx))) {
                    throw XProcException.dynamicError(13);
                }
                nsBindings.put(pfx,localBindings.get(pfx));
            }
        }

        String select = var.getSelect();
        XdmValue value = new XdmValue(evaluateXPath(doc, defaultCollection, nsBindings, select, globals));
        String stringValue = "";

        try {
            for (XdmItem item : value) {
                if (item.isAtomicValue()) {
                    stringValue += item.getStringValue();
                } else if (item instanceof XdmNode) {
                    XdmNode node = (XdmNode) item;
                    if (node.getNodeKind() == XdmNodeKind.ATTRIBUTE
                            || node.getNodeKind() == XdmNodeKind.NAMESPACE) {
                        stringValue += node.getStringValue();
                    } else {
                        XdmDestination dest = new XdmDestination();
                        S9apiUtils.writeXdmValue(runtime,item,dest,null);
                        stringValue += dest.getXdmNode().getStringValue();
                    }
                } else {
                    // Don't know how to create string value from item. Take empty string, and raise
                    // an error if we're not in "general-values" mode.
                    if (!runtime.getAllowGeneralExpressions())
                        throw new XProcException("Can not evaluate expression when not in 'general-values' mode: " + select);
                }
            }
        } catch (SaxonApiUncheckedException saue) {
            Throwable sae = saue.getCause();
            if (sae instanceof XPathException) {
                XPathException xe = (XPathException) sae;
                if ("http://www.w3.org/2005/xqt-errors".equals(xe.getErrorCodeNamespace()) && "XPDY0002".equals(xe.getErrorCodeLocalPart())) {
                    throw XProcException.dynamicError(26, step.getNode(), "The expression for $" + var.getName() + " refers to the context item.");
                } else {
                    throw saue;
                }
            } else {
                throw saue;
            }
        } catch (SaxonApiException sae) {
            throw new XProcException(sae);
        }

        // Section 5.7.5 Namespaces on variables, options, and parameters
        //
        // If the select attribute was used to specify the value and it consisted of a single VariableReference
        // (per [XPath 1.0] or [XPath 2.0], as appropriate), then the namespace bindings from the referenced
        // option or variable are used.
        Pattern varrefpat = Pattern.compile("^\\s*\\$([^\\s=]+)\\s*$");
        Matcher varref = varrefpat.matcher(select);
        if (varref.matches()) {
            String varrefstr = varref.group(1);
            QName varname = null;
            String[] qname;
            try {
                qname = NameChecker.checkQNameParts(varrefstr);
                String vpfx = qname[0];
                String vlocal = qname[1];
                if (vpfx == null || "".equals(vpfx)) {
                    varname = new QName("", vlocal);
                } else {
                    String vns = nsBindings.get(vpfx);
                    varname = new QName(vpfx, vns, vlocal);
                }
                RuntimeValue val = globals.get(varname);
                nsBindings = val.getNamespaceBindings();
            } catch (XPathException e) {
                // not a variable name
            }
        }

        // Section 5.7.5 Namespaces on variables, options, and parameters
        //
        // If the select attribute was used to specify the value and it evaluated to a node-set, then the in-scope
        // namespaces from the first node in the selected node-set (or, if it's not an element, its parent) are used.
        if (value.size() > 0) {
            XdmItem first = value.iterator().next();
            if (first instanceof XdmNode) {
                XdmNode node = (XdmNode)first;
                nsBindings.clear();
                XdmSequenceIterator nsIter = node.axisIterator(Axis.NAMESPACE);
                while (nsIter.hasNext()) {
                    XdmNode ns = (XdmNode) nsIter.next();
                    nsBindings.put((ns.getNodeName()==null ? "" : ns.getNodeName().getLocalName()),ns.getStringValue());
                }
            }
        }

        // Cast the value if needed
        if (runtime.getAllowGeneralExpressions()) {
            if (var.getSequenceType() != null) {
                if (SequenceType.XS_STRING.equals(var.getSequenceType())) {
                    value = null;
                } else {
                    value = var.getSequenceType().cast(value, var.getNode());
                }
            }
        } else {
            value = null;
        }

        // Test to see if the option has a reasonable string value
        if (var.getTypeAsQName() != null) {
            try {
                TypeUtils.checkType(runtime, value, stringValue, var.getTypeAsQName(), var.getNode());
            } catch (XProcException e) {
                throw new XProcException(e.getErrorCode(), this, e);
            }
        } else if (var.getType() != null) {
            String type = var.getType();
            if (type.contains("|")) {
                TypeUtils.checkLiteral(stringValue, type);
            }
        }

        if (value != null) {
            return new RuntimeValue(stringValue, value, var.getNode(), nsBindings);
        } else {
            return new RuntimeValue(stringValue, var.getNode(), nsBindings);
        }
    }

    protected Vector<XdmItem> evaluateXPath(XdmNode doc, Vector<XdmNode> defaultCollection, Hashtable<String,String> nsBindings, String xpath, Hashtable<QName,RuntimeValue> globals) {
        Vector<XdmItem> results = new Vector<XdmItem> ();
        Hashtable<QName,RuntimeValue> boundOpts = new Hashtable<QName,RuntimeValue> ();

        for (QName name : globals.keySet()) {
            RuntimeValue v = globals.get(name);
            if (v.initialized()) {
                boundOpts.put(name, v);
            }
        }

        CollectionFinder collectionFinder = null;
        if (defaultCollection != null) {
            Configuration config = runtime.getProcessor().getUnderlyingConfiguration();
            collectionFinder = config.getCollectionFinder();
            config.setDefaultCollection(XProcCollectionFinder.DEFAULT);
            config.setCollectionFinder(new XProcCollectionFinder(runtime, defaultCollection, collectionFinder));
        }

        try {
            XPathCompiler xcomp = runtime.getProcessor().newXPathCompiler();
            URI baseURI = step.getNode().getBaseURI();
            if (baseURI == null || !baseURI.isAbsolute())  {
                if (runtime.getBaseURI() != null) {
                    xcomp.setBaseURI(runtime.getBaseURI().resolve(baseURI));
                }
            } else {
                xcomp.setBaseURI(baseURI);
            }

            for (QName varname : boundOpts.keySet()) {
                xcomp.declareVariable(varname);
            }

            for (String prefix : nsBindings.keySet()) {
                xcomp.declareNamespace(prefix, nsBindings.get(prefix));
            }
            XPathExecutable xexec = null;
            try {
                xexec = xcomp.compile(xpath);
            } catch (SaxonApiException sae) {
                Throwable t = sae.getCause();
                if (t instanceof XPathException) {
                    XPathException xe = (XPathException) t;
                    if (xe.getMessage().contains("Undeclared (or unbound?) variable")) {
                        throw XProcException.dynamicError(26, step.getNode(), xe.getMessage());
                    }
                }
                throw sae;
            }

            XPathSelector selector = xexec.load();

            for (QName varname : boundOpts.keySet()) {
                XdmValue value = null;
                RuntimeValue rval = boundOpts.get(varname);
                if (runtime.getAllowGeneralExpressions() && rval.hasGeneralValue()) {
                    value = rval.getValue();
                } else {
                    value = rval.getUntypedAtomic(runtime);
                }
                selector.setVariable(varname,value);
            }

            if (doc != null) {
                selector.setContextItem(doc);
            }

            try {
                Iterator<XdmItem> values = selector.iterator();
                while (values.hasNext()) {
                    results.add(values.next());
                }
            } catch (SaxonApiUncheckedException saue) {
                Throwable sae = saue.getCause();
                if (sae instanceof XPathException) {
                    XPathException xe = (XPathException) sae;
                    if ("http://www.w3.org/2005/xqt-errors".equals(xe.getErrorCodeNamespace()) && "XPDY0002".equals(xe.getErrorCodeLocalPart())) {
                        throw XProcException.dynamicError(26, step.getNode(), "Expression refers to context when none is available: " + xpath);
                    } else {
                        Throwable cause = sae.getCause();
                        if (cause != null)
                            throw new XProcException(
                                this,
                                sae,
                                XProcException.fromException(cause)
                                              .rebase(null, new RuntimeException().getStackTrace())
                                              .rebase(this));
                        else
                            throw saue;
                    }

                } else {
                    throw saue;
                }
            }
        } catch (SaxonApiException sae) {
            if (S9apiUtils.xpathSyntaxError(sae)) {
                throw XProcException.dynamicError(23, this, sae.getCause().getMessage());
            } else {
                throw new XProcException(this, sae);
            }
        } catch (SaxonApiUncheckedException saue) {
            throw new XProcException(this, saue);
        } finally {
            if (defaultCollection != null) {
                runtime.getProcessor().getUnderlyingConfiguration().setCollectionFinder(collectionFinder);
            }
        }

        return results;
    }
}
