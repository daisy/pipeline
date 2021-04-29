package com.xmlcalabash.runtime;

import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.util.MessageFormatter;
import com.xmlcalabash.util.TreeWriter;
import com.xmlcalabash.util.XProcMessageListenerHelper;
import com.xmlcalabash.io.WritablePipe;
import com.xmlcalabash.model.*;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.QName;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 13, 2008
 * Time: 7:40:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class XTry extends XCompoundStep {
    private static final QName c_errors = new QName("c", XProcConstants.NS_XPROC_STEP, "errors");
    private static final QName c_error = new QName("c", XProcConstants.NS_XPROC_STEP, "error");
    private static final QName _href = new QName("", "href");
    private static final QName _line = new QName("", "line");
    private static final QName _column = new QName("", "column");
    private static final QName _code = new QName("", "code");
    private boolean inCatch = false;
    private Vector<XdmNode> errors = new Vector<XdmNode> ();

    public XTry(XProcRuntime runtime, Step step, XCompoundStep parent) {
          super(runtime, step, parent);
    }

    public void instantiate(Step step) {
        parent.addStep(this);

        DeclareStep decl = step.getDeclaration();

        for (Step substep : decl.subpipeline()) {
            if (XProcConstants.p_group.equals(substep.getType())) {
                XGroup newstep = new XGroup(runtime, substep, this);
                newstep.instantiate(substep);
            } else if (XProcConstants.p_catch.equals(substep.getType())) {
                XCatch newstep = new XCatch(runtime, substep, this);
                newstep.instantiate(substep);
            } else {
                throw new XProcException(
                    step, "This can't happen, can it? try contains something that isn't a group or a catch?");
            }
        }

        for (Output output : step.outputs()) {
            String port = output.getPort();
            if (port.endsWith("|")) {
                String rport = port.substring(0,port.length()-1);
                XInput xinput = getInput(rport);
                WritablePipe wpipe = xinput.getWriter();
                outputs.put(port, wpipe);
                logger.trace(MessageFormatter.nodeMessage(step.getNode(), " writes to " + wpipe + " for " + port));
            } else {
                XOutput xoutput = new XOutput(runtime, output);
                addOutput(xoutput);
                WritablePipe wpipe = xoutput.getWriter();
                outputs.put(port, wpipe);
                logger.trace(MessageFormatter.nodeMessage(step.getNode(), " writes to " + wpipe + " for " + port));
            }
        }
    }

    public void run() throws SaxonApiException {

        inScopeOptions = parent.getInScopeOptions();
        for (Variable var : step.getVariables()) {
            RuntimeValue value = computeValue(var);
            inScopeOptions.put(var.getName(), value);
        }

        XGroup xgroup = (XGroup) subpipeline.get(0);

        for (String port : inputs.keySet()) {
            if (!port.startsWith("|")) {
                xgroup.inputs.put(port, inputs.get(port));
            }
        }

        for (String port : outputs.keySet()) {
            if (!port.endsWith("|")) {
                xgroup.outputs.put(port, outputs.get(port));
            }
        }

        XProcMessageListenerHelper.openStep(runtime, this);
        try {
            xgroup.run();
        } catch (XProcException xe) {
            
            logger.trace("p:try: caught error: " + xe.toString());
            logger.trace("", xe);
            
            TreeWriter treeWriter = new TreeWriter(runtime);
            treeWriter.startDocument(step.getNode().getBaseURI());
            treeWriter.addStartElement(c_errors);
            treeWriter.startContent();

            boolean reported = false;
            for (XdmNode doc : runtime.getXProcData().errors()) {
                treeWriter.addSubtree(doc);
                reported = true;
            }

            for (XdmNode doc : errors) {
                treeWriter.addSubtree(doc);
                reported = true;
            }

            if (!reported) {
                // Hey, no one reported this exception. We better do it.
                xe.serialize(treeWriter);
            }

            treeWriter.addEndElement();
            treeWriter.endDocument();

            XCatch xcatch = (XCatch) subpipeline.get(1);

            xcatch.writeError(treeWriter.getResult());

            for (String port : inputs.keySet()) {
                if (!port.startsWith("|")) {
                    xcatch.inputs.put(port, inputs.get(port));
                }
            }

            for (String port : outputs.keySet()) {
                if (!port.endsWith("|")) {
                    xcatch.outputs.put(port, outputs.get(port));
                }
            }

            inCatch = true;
            xcatch.run();
        } finally {
            inCatch = false;
            runtime.getMessageListener().closeStep();
        }
    }

    public void reportError(XdmNode doc) {
        if (inCatch) {
            // if the error is coming from the catch group, report to parent
            super.reportError(doc);
        } else {
            errors.add(doc);
        }
    }
}
