package com.xmlcalabash.runtime;

import com.xmlcalabash.core.XProcRunnable;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;

import org.slf4j.Logger;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Vector;
import java.util.Optional;
import javax.xml.transform.SourceLocator;

import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;
import com.xmlcalabash.core.XProcConstants;
import com.xmlcalabash.io.Pipe;
import com.xmlcalabash.io.ReadablePipe;
import com.xmlcalabash.model.RuntimeValue;
import com.xmlcalabash.model.Step;
import com.xmlcalabash.model.Input;
import com.xmlcalabash.model.DeclareStep;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: ndw
 * Date: Oct 7, 2008
 * Time: 8:02:28 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class XStep implements XProcRunnable {
    protected Logger logger = null;
    protected XProcRuntime runtime = null;
    protected Step step = null;
    protected String name = null;
    private Hashtable<String,XInput> xinputs = new Hashtable<String,XInput> ();
    private Hashtable<String,XOutput> xoutputs = new Hashtable<String,XOutput> ();
    protected Hashtable<String, Vector<ReadablePipe>> inputs = new Hashtable<String, Vector<ReadablePipe>> ();
    protected Hashtable<String, Pipe> outputs = new Hashtable<String, Pipe> ();
    private Hashtable<QName, RuntimeValue> options = new Hashtable<QName, RuntimeValue> ();
    private Hashtable<String, Hashtable<QName, RuntimeValue>> parameters = new Hashtable<String, Hashtable<QName, RuntimeValue>> ();
    protected XCompoundStep parent = null;
    protected Hashtable<QName,RuntimeValue> inScopeOptions = new Hashtable<QName,RuntimeValue> ();
    /* the next frames in the call stack */
    private static final SourceLocator[] EMPTY_LOCATION = new SourceLocator[]{};
    protected SourceLocator[] parentLocation = EMPTY_LOCATION;
    private Boolean runLazily = null;

    public XStep(XProcRuntime runtime, Step step) {
        this.runtime = runtime;
        this.step = step;
        if (step != null) {
            name = step.getName();
        }
        logger = LoggerFactory.getLogger(this.getClass());
    }

    public Step getStep() {
        return step;
    }

    public XdmNode getNode() {
        return step.getNode();
    }

    public QName getType() {
        return step.getNode().getNodeName();
    }

    public String getName() {
        return name;
    }

    public DeclareStep getDeclareStep() {
        return step.getDeclaration();
    }

    public XCompoundStep getParent() {
        return parent;
    }

    public void addInput(XInput input) {
        String port = input.getPort();
        if (xinputs.containsKey(port)) {
            throw new XProcException(input.getNode(), "Attempt to add output '" + port + "' port to the same step twice.");
        }
        xinputs.put(port, input);
    }

    public void addOutput(XOutput output) {
        String port = output.getPort();
        if (xoutputs.containsKey(port)) {
            throw new XProcException(output.getNode(), "Attempt to add output '" + port + "' port to the same step twice.");
        }
        xoutputs.put(port, output);
    }

    public XInput getInput(String port) {
        if (xinputs.containsKey(port)) {
            return xinputs.get(port);
        } else {
            throw new XProcException(step, "Attempt to get non-existant input '" + port + "' port from step.");
        }
    }

    public XOutput getOutput(String port) {
        if (xoutputs.containsKey(port)) {
            return xoutputs.get(port);
        } else {
            if (XProcConstants.NS_XPROC.equals(step.getType().getNamespaceURI())
                    && step.getStep().getVersion() > 1.0) {
                return null;
            } else {
                throw new XProcException(step, "Attempt to get non-existant output '" + port + "' port from step.");
            }
        }
    }

    public void setParameter(QName name, RuntimeValue value) {
        Set<String> ports = getParameterPorts();
        int pportCount = 0;
        String pport = null;
        String ppport = null;
        for (String port : ports) {
            pport = port;
            pportCount++;

            Input pin = getStep().getInput(port);
            if (pin.getPrimary()) {
                ppport = port;
            }
        }

        if (pportCount == 0) {
            throw new XProcException(step, "Attempt to set parameter but there's no parameter port.");
        }

        if (ppport != null) {
            pport = ppport;
        } else {
            if (pportCount > 1) {
                throw new XProcException(step, "Attempt to set parameter w/o specifying a port (and there's more than one)");
            }
        }

        setParameter(pport, name, value);
    }

    public void setParameter(String port, QName name, RuntimeValue value) {
        Hashtable<QName,RuntimeValue> pparams;
        if (parameters.containsKey(port)) {
            pparams = parameters.get(port);
        } else {
            XInput xinput = getInput(port); // Make sure there is one
            Input input = getDeclareStep().getInput(port);
            if (!input.getParameterInput()) {
                throw new XProcException(step, "Attempt to write parameters to non-parameter input port: " + port);
            }
            pparams = new Hashtable<QName,RuntimeValue> ();
            parameters.put(port, pparams);
        }

        if (pparams.containsKey(name)) {
            throw new XProcException(step, "Duplicate parameter: " + name);
        }

        if (XProcConstants.NS_XPROC.equals(name.getNamespaceURI())) {
            throw XProcException.dynamicError(31);
        }

        pparams.put(name, value);

    }

    public Set<QName> getOptions() {
        return options.keySet();
    }

    public RuntimeValue getOption(QName name) {
        if (options.containsKey(name)) {
            return options.get(name);
        } else {
            return null;
        }
    }

    public void setOption(QName name, RuntimeValue value) {
        /* this causes an attempt to run the same pipeline twice to fail because the passedInOptions get set twice...
        if (options.containsKey(name)) {
            throw new XProcException(step.getNode(), "Duplicate option: " + name);
        }
        */
        options.put(name, value);
    }

    public void clearOptions() {
        options.clear();
    }

    public void clearParameters() {
        parameters.clear();
    }

    public Set<QName> getParameters() {
        return getParameters("*");
    }

    public RuntimeValue getParameter(QName name) {
        Set<String> ports = getParameterPorts();
        int pportCount = 0;
        String pport = null;
        for (String port : ports) {
            pport = port;
            pportCount++;
        }

        if (pportCount != 1) {
            return null;
        }

        return getParameter(pport, name);
    }

    public Set<String> getParameterPorts() {
        HashSet<String> ports = new HashSet<String> ();
        for (Input input : step.inputs()) {
            if (input.getParameterInput()) {
                ports.add(input.getPort());
            }

        }
        return ports;
    }

    public Set<QName> getParameters(String port) {
        if (parameters.containsKey(port)) {
            return parameters.get(port).keySet();
        } else {
            return new HashSet<QName> ();
        }
    }

    public RuntimeValue getParameter(String port, QName name) {
        if (parameters.containsKey(port)) {
            Hashtable<QName,RuntimeValue> pparams = parameters.get(port);
            if (pparams.containsKey(name)) {
                return pparams.get(name);
            }
        }
        return null;
    }

    public String getExtensionAttribute(QName name) {
        if (step != null) {
            return step.getExtensionAttribute(name);
        } else {
            return null;
        }
    }

    public String getInheritedExtensionAttribute(QName name) {
        if (getExtensionAttribute(name) != null) {
            return getExtensionAttribute(name);
        }
        if (parent != null) {
            return parent.getInheritedExtensionAttribute(name);
        }
        return null;
    }

    public boolean hasInScopeVariableBinding(QName name) {
        if (inScopeOptions.containsKey(name)) {
            return true;
        }

        return getParent() != null && getParent().hasInScopeVariableBinding(name);
    }

    public boolean hasInScopeVariableValue(QName name) {
        if (inScopeOptions.containsKey(name)) {
            RuntimeValue v = getOption(name);
            return v.initialized();
        }

        return getParent() != null && getParent().hasInScopeVariableBinding(name);
    }

    public Hashtable<QName,RuntimeValue> getInScopeOptions() {
        // We make a copy so that what our children do can't effect us
        Hashtable<QName,RuntimeValue> globals = new Hashtable<QName,RuntimeValue> ();
        if (inScopeOptions != null) {
            for (QName name : inScopeOptions.keySet()) {
                globals.put(name,inScopeOptions.get(name));
            }
        }
        return globals;
    }

    public abstract RuntimeValue optionAvailable(QName optName);
    public abstract void instantiate(Step step);
    public abstract void reset();
    public void run() throws SaxonApiException {
        if (runLazily == null) {
            runLazily = isPure().orElse(false);
        }
        if (runLazily) {
            XProcRunnable runIfNotRunYet = new XProcRunnable() {
                    private boolean done = false;
                    public void run() throws SaxonApiException {
                        if (done) return;
                        done = true;
                        doRun();
                        // next time XStep.run() is called don't run lazily, because we already know an
                        // output will be accessed so we might as well do it immediately
                        runLazily = false;
                    }
                };
            for (String port : outputs.keySet()) {
                outputs.get(port).onRead(runIfNotRunYet);
            }
        } else {
            doRun();
        }
    }
    protected abstract void doRun() throws SaxonApiException;

    private Optional<Boolean> isPure() {
        if (step != null) {
            Optional<Boolean> pure = step.isPure();
            DeclareStep decl = getDeclareStep();
            if (decl != null) {
                Optional<Boolean> declPure = decl.isPure();
                if (declPure.isPresent()) {
                    // cx:pure on step has precedence
                    if (!pure.isPresent())
                        return declPure;
                    else if (pure.get() && !declPure.get()) {
                        XProcException warning = new XProcException(
                            this,
                            "Step was marked with cx:pure=\"true\" but the corresponding declaration is impure");
                        logger.warn(warning.toString());
                    }
                }
            }
            return pure;
        } else {
            return Optional.empty();
        }
    }

    public void error(XProcException error) {
        runtime.error(this, error);
    }

    public void warning(XdmNode location, String message) {
        runtime.warning(this, location, message);
    }

    public void info(XdmNode location, String message) {
        runtime.info(this, location, message);
    }

    public SourceLocator[] getLocation() {
        if (step == null)
            return parentLocation;
        SourceLocator[] location = new SourceLocator[parentLocation.length + 1]; {
            location[0] = XProcException.getLocator(step);
            for (int i = 0; i < parentLocation.length; i++)
                location[i + 1] = parentLocation[i];
        }
        return location;
    }
}
