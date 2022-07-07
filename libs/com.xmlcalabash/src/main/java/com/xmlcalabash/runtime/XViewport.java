package com.xmlcalabash.runtime;

import java.math.BigDecimal;
import java.math.MathContext;

import com.xmlcalabash.io.Pipe;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.io.ReadOnlyPipe;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcData;
import com.xmlcalabash.util.MessageFormatter;
import com.xmlcalabash.util.ProcessMatchingNodes;
import com.xmlcalabash.util.ProcessMatch;
import com.xmlcalabash.util.TreeWriter;
import com.xmlcalabash.util.XProcMessageListenerHelper;
import com.xmlcalabash.model.*;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 15, 2008
 * Time: 7:03:35 AM
 * To change this template use File | Settings | File Templates.
 */
public class XViewport extends XCompoundStep implements ProcessMatchingNodes {
    private Pipe current = null;
    private ProcessMatch matcher = null;
    private int sequencePosition = 0;
    private int sequenceLength = 0;

    public XViewport(XProcRuntime runtime, Step step, XCompoundStep parent) {
        super(runtime, step, parent);
    }

    public ReadablePipe getBinding(String stepName, String portName) {
        if (name.equals(stepName) && ("#current".equals(portName) || "current".equals(portName))) {
            if (current == null) {
                current = new Pipe(runtime);
            }
            return new ReadOnlyPipe(runtime, current.documents());
        } else {
            return super.getBinding(stepName, portName);
        }
    }

    protected void copyInputs() throws SaxonApiException {
        // nop;
    }

    public void reset() {
        super.reset();
        sequenceLength = 0;
        sequencePosition = 0;
    }

    protected void doRun() throws SaxonApiException {
        logger.trace("Running p:viewport " + step.getName());

        XProcData data = runtime.getXProcData();
        data.openFrame(this);

        if (current == null) {
            current = new Pipe(runtime);
        }

        RuntimeValue match = ((Viewport) step).getMatch();

        String iport = "#viewport-source";
        ReadablePipe vsource = null;

        if (inputs.get(iport).size() != 1) {
            throw XProcException.dynamicError(3);
        } else {
            vsource = inputs.get(iport).get(0);
        }

        XdmNode doc = vsource.read();
        if (doc == null || vsource.moreDocuments()) {
            throw XProcException.dynamicError(3);
        }
        
        matcher = new ProcessMatch(runtime, this);

        // FIXME: Only do this if we really need to!
        sequenceLength = matcher.count(doc, match, false);

        runtime.getXProcData().setIterationSize(sequenceLength);

        runtime.start(this);
        XProcMessageListenerHelper.openStep(runtime, this);
        try {
            matcher.match(doc, match);
        } finally {
            runtime.getMessageListener().closeStep();
        }
        runtime.finish(this);

        for (String port : inputs.keySet()) {
            if (port.startsWith("|")) {
                String wport = port.substring(1);
                WritablePipe pipe = outputs.get(wport);
                XdmNode result = matcher.getResult();
                pipe.write(result);
                logger.trace(MessageFormatter.nodeMessage(step.getNode(), "Viewport output copy from matcher to " + pipe));
            }
        }
    }

    public boolean processStartDocument(XdmNode node) {
        return true;
    }

    public void processEndDocument(XdmNode node) {
        // nop
    }

    public boolean processStartElement(XdmNode node) throws SaxonApiException {
        runtime.getMessageListener().openStep(this, getNode(), null, null, BigDecimal.ONE.divide(new BigDecimal(sequenceLength), MathContext.DECIMAL128));
        try {

        // Use a TreeWriter to make the matching node into a proper document
        TreeWriter treeWriter = new TreeWriter(runtime);
        treeWriter.startDocument(node.getBaseURI());
        treeWriter.addSubtree(node);
        treeWriter.endDocument();

        current.resetWriter();
        current.write(treeWriter.getResult());

        logger.trace(MessageFormatter.nodeMessage(step.getNode(), "Viewport copy matching node to " + current));

        sequencePosition++;
        runtime.getXProcData().setIterationPosition(sequencePosition);

        // Calculate all the variables
        inScopeOptions = parent.getInScopeOptions();
        for (Variable var : step.getVariables()) {
            RuntimeValue value = computeValue(var);
            inScopeOptions.put(var.getName(), value);
        }

        for (XStep step : subpipeline) {
            step.reset();
            try {
                step.run();
            } catch (SaxonApiException sae) {
                throw new XProcException(sae);
            }
        }


        try {
            int count = 0;
            for (String port : inputs.keySet()) {
                if (port.startsWith("|")) {
                    for (ReadablePipe reader : inputs.get(port)) {
                        while (reader.moreDocuments()) {
                            count++;

                            if (count > 1) {
                                XOutput output = getOutput(port.substring(1));
                                if (!output.getSequence()) {
                                    throw XProcException.dynamicError(7);
                                }
                            }

                            XdmNode doc = reader.read();
                            matcher.addSubtree(doc);
                        }
                        reader.resetReader();
                    }
                }
            }
        } catch (SaxonApiException sae) {
            throw new XProcException(sae);
        }

        } finally {
            runtime.getMessageListener().closeStep();
        }

        return false;
    }

    public void processEndElement(XdmNode node) {
    }

    public void processText(XdmNode node) {
        throw new UnsupportedOperationException("Can't run a viewport over text, PI, or comments");
    }

    public void processComment(XdmNode node) {
        throw new UnsupportedOperationException("Can't run a viewport over text, PI, or comments");
    }

    public void processPI(XdmNode node) {
        throw new UnsupportedOperationException("Can't run a viewport over text, PI, or comments");
    }

    public void processAttribute(XdmNode node) {
        throw new UnsupportedOperationException("Can't run a viewport over attributes");
    }
}
