/*
 * XProcException.java
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

package com.xmlcalabash.core;

import com.xmlcalabash.model.Step;
import com.xmlcalabash.runtime.XStep;
import com.xmlcalabash.util.S9apiUtils;
import com.xmlcalabash.util.TreeWriter;
import com.xmlcalabash.util.URIUtils;

import net.sf.saxon.expr.instruct.Actor;
import net.sf.saxon.expr.instruct.AttributeSet;
import net.sf.saxon.expr.instruct.GlobalVariable;
import net.sf.saxon.expr.instruct.Instruction;
import net.sf.saxon.expr.instruct.NamedTemplate;
import net.sf.saxon.expr.instruct.TemplateRule;
import net.sf.saxon.expr.instruct.TerminationException;
import net.sf.saxon.expr.instruct.UserFunction;
import net.sf.saxon.expr.parser.ExplicitLocation;
import net.sf.saxon.expr.parser.XPathParser;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.lib.StandardErrorListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trace.ContextStackFrame;
import net.sf.saxon.trace.ContextStackIterator;
import net.sf.saxon.trans.KeyDefinition;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.AttributeLocation;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.type.ValidationException;

import javax.xml.transform.dom.DOMLocator;
import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author ndw
 * @author bertfrees
 */
public class XProcException extends RuntimeException {

    public static final QName err_E0001 = new QName(XProcConstants.NS_XPROC_ERROR_EX, "XE0001"); // invalid pipeline
    public static final QName err_E0002 = new QName(XProcConstants.NS_XPROC_ERROR_EX, "XE0002"); // invalid configuration

    private static final String NS_DAISY_PIPELINE_XPROC = "http://www.daisy.org/ns/pipeline/xproc";
    private static final QName c_error = new QName("c", XProcConstants.NS_XPROC_STEP, "error");
    private static final QName _href = new QName("", "href");
    private static final QName _line = new QName("", "line");
    private static final QName _column = new QName("", "column");
    private static final QName _code = new QName("", "code");
    private static final QName _type = new QName("", "type");
    private static final QName _name = new QName("", "name");
    private static final QName px_cause = new QName("px", NS_DAISY_PIPELINE_XPROC, "cause");
    private static final QName px_location = new QName("px", NS_DAISY_PIPELINE_XPROC, "location");
    private static final QName px_file = new QName("px", NS_DAISY_PIPELINE_XPROC, "file");

    private final QName errorCode;
    private final XdmNode errorContent;
    private final XProcException errorCause;
    private final SourceLocator[] location;

    private static final SourceLocator NO_LOCATOR = new SourceLocator() {
            public String getPublicId() { return null; }
            public String getSystemId() { return null; }
            public int getLineNumber() { return -1; }
            public int getColumnNumber() { return -1; }
        };
    private static final SourceLocator[] NO_LOCATION = new SourceLocator[]{NO_LOCATOR};

    /**
     * Create an XProc error
     *
     * @param code     The type of the error, or <code>null</code> if untyped.
     * @param location The location of the error, or <code>null</code> if unknown or not
     *                 applicable. Can be a {@link SourceLocator[]}, {@link XStep}, {@link Step},
     *                 {@link XdmNode}, {@link TransformerException} or {@link Throwable}.
     * @param message  The content of the error. Can be a {@link String}, {@link XdmNode}, {@link
     *                 Throwable} or <code>null</code> if absent. In case of {@link Throwable}, this
     *                 argument determines both the content of the error (through its {@link
     *                 XProcException#getErrorContent()} or {@link Throwable#getMessage()} method)
     *                 and also the cause of the Java exception. Note that the latter is different
     *                 from the <code>cause</code> argument (see below).
     * @param cause    The XProc error that caused this XProc error to be created. Note that this
     *                 is not the same as the cause of the Java exception.
     */
    protected XProcException(QName code, Object location, Object message, XProcException cause) {
        super(
            message instanceof String
                ? (String)message
                : message instanceof XdmNode
                    ? ((XdmNode)message).getStringValue()
                    : message instanceof Throwable
                        ? ((Throwable)message).getMessage()
                        : null,
            message instanceof Throwable
                ? (Throwable)message
                : null);
        errorCode = code;
        if (!(message == null ||
              message instanceof String ||
              message instanceof XdmNode ||
              message instanceof Throwable))
            throw new IllegalStateException("coding error");
        errorContent = message instanceof XdmNode
            ? (XdmNode)message
            : message instanceof XProcException
                ? ((XProcException)message).getErrorContent()
                : null;
        this.location = getLocation(location);
        this.errorCause = cause;
    }

    private static class StaticXProcError extends XProcException {
        private StaticXProcError(Integer code, Object location, Object message) {
            super(code != null ? XProcConstants.staticError(code) : null,
                  location,
                  message,
                  null);
            if (code == null ||
                !(message == null ||
                  message instanceof String ||
                  message instanceof Throwable))
                throw new IllegalStateException("coding error");
        }
    }

    private static class DynamicXProcError extends XProcException {
        private DynamicXProcError(Object code, Object location, Object message, XProcException cause) {
            super(code instanceof QName
                      ? (QName)code
                      : code instanceof Integer
                          ? code != null
                              ? XProcConstants.dynamicError((Integer)code)
                              : null
                          : null,
                  location,
                  message,
                  cause);
            if (code == null ||
                !(code instanceof Integer ||
                  code instanceof QName))
                throw new IllegalStateException("coding error");
        }
    }

    public static XProcException staticError(int code) {
        return new StaticXProcError(code, null, null);
    }

    public static XProcException staticError(int code, String message) {
        return new StaticXProcError(code, null, message);
    }

    public static XProcException staticError(int code, XdmNode location, String message) {
        return new StaticXProcError(code, location, message);
    }

    public static XProcException staticError(int code, XdmNode location, Throwable message) {
        return new StaticXProcError(code, location, message);
    }

    public static XProcException dynamicError(int code) {
        return new DynamicXProcError(code, null, null, null);
    }

    public static XProcException dynamicError(int code, String message) {
        return new DynamicXProcError(code, null, message, null);
    }

    public static XProcException dynamicError(int code, Throwable message) {
        return new DynamicXProcError(code, null, message, null);
    }

    public static XProcException dynamicError(int code, XdmNode location, String message) {
        return new DynamicXProcError(code, location, message, null);
    }

    public static XProcException dynamicError(int code, XdmNode location, Throwable message) {
        return new DynamicXProcError(code, location, message, null);
    }

    public static XProcException dynamicError(int code, Step location) {
        return new DynamicXProcError(code, location, null, null);
    }

    public static XProcException dynamicError(int code, Step location, String message) {
        return new DynamicXProcError(code, location, message, null);
    }

    public static XProcException dynamicError(int code, XStep location, String message) {
        return new DynamicXProcError(code, location, message, null);
    }

    public static XProcException stepError(int code) {
        return new DynamicXProcError(XProcConstants.stepError(code), null, null, null);
    }

    public static XProcException stepError(int code, String message) {
        return new DynamicXProcError(XProcConstants.stepError(code), null, message, null);
    }

    public static XProcException stepError(int code, Throwable message) {
        return new DynamicXProcError(XProcConstants.stepError(code), null, message, null);
    }

    public static XProcException stepError(int code, XdmNode location, String message) {
        return new DynamicXProcError(XProcConstants.stepError(code), location, message, null);
    }

    public XProcException(QName code, XdmNode location, String message) {
        this(code, location, message, null);
    }

    public XProcException(QName code, XStep location, String message) {
        this(code, location, message, null);
    }

    public XProcException(QName code, XStep location, XdmNode message) {
        this(code, location, message, null);
    }

    public XProcException(QName code, XStep location, Throwable message) {
        this(code, location, message, null);
    }

    public XProcException(QName code, TransformerException location, XdmNode message) {
        this(code, location, message, null);
    }

    public XProcException(QName code, TransformerException location, Throwable message) {
        this(code, location, message, null);
    }

    public XProcException(QName code, TransformerException location, XdmNode message, XProcException cause) {
        this(code, (Object)location, (Object)message, cause);
    }

    public XProcException(QName code, TransformerException location, Throwable message, XProcException cause) {
        this(code, (Object)location, (Object)message, cause);
    }

	public XProcException(QName code, XStep location, String message, XProcException cause) {
        this(code, (Object)location, (Object)message, cause);
    }

    public XProcException(QName code, String message) {
        this(code, null, message, null);
    }

    public XProcException(QName code, Throwable message) {
        this(code, null, message, null);
    }

    public XProcException(String message) {
        this(null, null, message, null);
    }

    public XProcException(Throwable message) {
        this(null, null, message, null);
    }

    public XProcException(Throwable message, XProcException cause) {
        this(null, null, message, cause);
    }

    public XProcException(SourceLocator[] location, Throwable message) {
        this(null, location, message, null);
    }

    public XProcException(XdmNode location, String message) {
        this(null, location, message, null);
    }

    public XProcException(XdmNode location, Throwable message) {
        this(null, location, message, null);
    }

    public XProcException(Step location, String message) {
        this(null, location, message, null);
    }

    public XProcException(XStep location, String message) {
        this(null, location, message, null);
    }

    public XProcException(XStep location, Throwable message) {
        this(null, location, message, null);
    }

    public XProcException(XStep location, String message, XProcException cause) {
        this(null, location, message, cause);
    }

    public XProcException(XStep location, Throwable message, XProcException cause) {
        this(null, location, message, cause);
    }

    public XProcException(TransformerException location, XdmNode message) {
        this(null, location, message, null);
    }

    public XProcException(TransformerException location, Throwable message) {
        this(null, location, message, null);
    }

    public XProcException(TransformerException location, XdmNode message, XProcException cause) {
        this(null, location, message, cause);
    }

    public XProcException(TransformerException location, Throwable message, XProcException cause) {
        this(null, location, message, cause);
    }

    /**
     * Create a XProc error from a Java exception. The exception is used for both the message and
     * location or the XProc error. The exception's cause is also converted to an XProc error and
     * becomes the error cause (and this recursively).
     */
    public static XProcException fromException(Throwable throwable) {
        XProcException cause = throwable.getCause() != null
            ? fromException(throwable.getCause())
            : null;
        return new XProcException(null, throwable, throwable, cause);
    }

    /**
     * Create a new instance of the same XProc error, to allow to better track where exceptions are
     * thrown.
     */
    public XProcException copy() {
        return new XProcException(errorCode, location, this, errorCause);
    }

    public XProcException rebase(SourceLocator[] base) {
        return rebase(base, null);
    }

    public XProcException rebase(XStep base) {
        return rebase(base, null);
    }

    public XProcException rebase(TransformerException base) {
        return rebase(base, null);
    }

    public XProcException rebase(SourceLocator[] newBase, StackTraceElement[] oldBase) {
        return rebase((Object)newBase, (Object)oldBase);
    }

    private XProcException rebase(Object newBaseObject, Object oldBaseObject) {
        SourceLocator[] newBase = getLocation(newBaseObject);
        SourceLocator[] oldBase = getLocation(oldBaseObject);
        SourceLocator[] newLocation; {
            int newLength = 0;
            if (location != NO_LOCATION) {
                if (oldBase != NO_LOCATION) {
                    int m = location.length - 1;
                    int n = oldBase.length - 1;
                    while (m >= 0 && n >= 0
                           && Objects.equals(location[m].getSystemId(), oldBase[n].getSystemId())
                           && Objects.equals(location[m].getLineNumber(), oldBase[n].getLineNumber())
                           && Objects.equals(location[m].getColumnNumber(), oldBase[n].getColumnNumber())) {
                        m--;
                        n--;
                    }
                    // allow top frame to differ in line number, as long as we're in the same method
                    if (m >= 0 && n >= 0
                        && location[m] instanceof JavaFrame
                        && oldBase[n] instanceof JavaFrame) {
                        StackTraceElement frame = ((JavaFrame)location[m]).frame;
                        StackTraceElement oldFrame = ((JavaFrame)oldBase[n]).frame;
                        if (frame.getClassName().equals(oldFrame.getClassName())
                            && Objects.equals(frame.getMethodName(), oldFrame.getMethodName())
                            && Objects.equals(frame.getFileName(), oldFrame.getFileName())) {
                            m--;
                            n--;
                        }
                    }
                    if (n < 0)
                        newLength += (m + 2);
                    else
                        newLength += location.length;
                } else
                    newLength += location.length;
            }
            if (newBase != NO_LOCATION) {
                newLength += newBase.length;
            }
            if (newLength == 0)
                newLocation = NO_LOCATION;
            else {
                newLocation = new SourceLocator[newLength];
                int i = 0;
                if (newBase != NO_LOCATION)
                    newLength -= newBase.length;
                while (i < newLength) {
                    newLocation[i] = location[i];
                    i++;
                }
                if (newBase != NO_LOCATION) {
                    for (SourceLocator l : newBase)
                        newLocation[i++] = l;
                }
            }
        }
        XProcException newErrorCause = errorCause != null
            ? errorCause.rebase(newBase, oldBase)
            : null;
        return new XProcException(errorCode, newLocation, this, newErrorCause);
    }

    public QName getErrorCode() {
        return errorCode;
    }

    public XdmNode getErrorContent() {
        return errorContent;
    }

    public XProcException getErrorCause() {
        return errorCause;
    }

    public SourceLocator[] getLocation() {
        return location;
    }

    public static SourceLocator prettyLocator(SourceLocator locator, final String instructionName) {
        return new SourceLocatorWithInstructionName(locator) {
            protected String getInstructionName() {
                return instructionName;
            }
        };
    }

    public static SourceLocator prettyLocator(final String systemId, final int lineNumber, final int columnNumber,
                                              final String instructionName) {
        return new SourceLocatorWithInstructionName(new SourceLocator() {
                public String getPublicId() { return null; }
                public String getSystemId() { return systemId; }
                public int getLineNumber() { return lineNumber; }
                public int getColumnNumber() { return columnNumber; }
            }) {
            protected String getInstructionName() {
                return instructionName;
            }
        };
    }

    private static SourceLocator[] getLocation(Object object) {
        SourceLocator[] location; {
            if (object == null)
                location = null;
            else if (object instanceof SourceLocator[])
                location = (SourceLocator[])object;
            else if (object instanceof XStep)
                location = ((XStep)object).getLocation();
            else if (object instanceof Step)
                location = new SourceLocator[]{getLocator(((Step)object))};
            else if (object instanceof XdmNode)
                location = new SourceLocator[]{getLocator((XdmNode)object)};
            else if (object instanceof XProcException)
                location = ((XProcException)object).getLocation();
            else if (object instanceof TransformerException)
                location = getLocation((TransformerException)object);
            else if (object instanceof StackTraceElement[])
                location = getLocation((StackTraceElement[])object);
            else if (object instanceof Throwable)
                location = getLocation(((Throwable)object).getStackTrace());
            else
                throw new IllegalStateException("coding error");
        }
        if (location == null || location.length == 0)
            return NO_LOCATION;
        else
            return location;
    }

    private static SourceLocator[] getLocation(TransformerException e) {

        // This code is inspired by StandardErrorListener
        List<SourceLocator> frames = new ArrayList<SourceLocator>();
        SourceLocator loc = e.getLocator();
        if (loc == null) {
            TransformerException err = e;
            while (loc == null) {
                if (err.getException() instanceof TransformerException) {
                    err = (TransformerException)err.getException();
                    loc = err.getLocator();
                } else if (err.getCause() instanceof TransformerException) {
                    err = (TransformerException)err.getCause();
                    loc = err.getLocator();
                } else {
                    break;
                }
            }
        }
        if (loc == null)
            loc = ExplicitLocation.UNKNOWN_LOCATION;
        if (loc instanceof XPathParser.NestedLocation)
            loc = ((XPathParser.NestedLocation)loc).getContainingLocation();
        String instructionName = getInstructionName(loc);
        if (instructionName == null && e instanceof TerminationException)
            instructionName = "xsl:message";
        loc = prettyLocator(loc, instructionName);
        frames.add(loc);

        // now add more frames based on XPathContext
        if (e instanceof XPathException) {
            XPathException xe = (XPathException)e;
            XPathContext ctxt = xe.getXPathContext();
            if (ctxt != null) {
                Iterator<ContextStackFrame> ff = new ContextStackIterator(xe.getXPathContext());
                while (ff.hasNext()) {
                    ContextStackFrame f = ff.next();
                    instructionName = getInstructionName(f);
                    if (instructionName != null)
                        frames.add(prettyLocator(f.getSystemId(), f.getLineNumber(), -1, instructionName));
                }
            }
        }
        return frames.toArray(new SourceLocator[frames.size()]);
    }

    private static SourceLocator[] getLocation(StackTraceElement[] trace) {
        SourceLocator[] location = new SourceLocator[trace.length];
        for (int i = 0; i < trace.length; i++) {
            location[i] = new JavaFrame(trace[i]);
        }
        return location;
    }

    private static SourceLocator getLocator(XdmNode node) {
        if (node == null)
            return NO_LOCATOR;
        final String systemId = URIUtils.cwdAsURI().relativize(node.getBaseURI()).toASCIIString();
        final int line = node.getLineNumber() > 0 ? node.getLineNumber() : S9apiUtils.getDocumentElement(node).getLineNumber();
        final int col = node.getColumnNumber();
        return new SourceLocator() {
            public String getPublicId() {
                return null;
            }
            public String getSystemId() {
                return systemId;
            }
            public int getLineNumber() {
                return line;
            }
            public int getColumnNumber() {
                return col;
            }
        };
    }

    public static SourceLocator getLocator(Step step) {
        return new XProcLocator(step);
    }

    private static String getInstructionName(SourceLocator loc) {
        if (loc instanceof AttributeLocation) {
            return ((AttributeLocation)loc).getElementName().getDisplayName() + "/@"
                + ((AttributeLocation)loc).getAttributeName();
        } else if (loc instanceof DOMLocator) {
            return ((DOMLocator)loc).getOriginatingNode().getNodeName();
        } else if (loc instanceof NodeInfo) {
            return ((NodeInfo)loc).getDisplayName();
        } else if (loc instanceof ValidationException && ((ValidationException)loc).getNode() != null) {
            return (((ValidationException)loc).getNode()).getDisplayName();
        } else if (loc instanceof Instruction) {
            return StandardErrorListener.getInstructionName((Instruction)loc);
        } else if (loc instanceof Actor) {
            return getInstructionName((Actor)loc);
        } else {
            return null;
        }
    }

    private static String getInstructionName(ContextStackFrame frame) {
        if (frame instanceof ContextStackFrame.FunctionCall) {
            StructuredQName name = ((ContextStackFrame.FunctionCall)frame).getFunctionName();
            if (name != null)
                return name.getClarkName() + "()";
        } else if (frame instanceof ContextStackFrame.ApplyTemplates) {
            String name = "xsl:apply-templates";
            Item node = frame.getContextItem();
            if (node instanceof NodeInfo)
                name += " processing " + Navigator.getPath((NodeInfo)node);
            return name;
        } else if (frame instanceof ContextStackFrame.CallTemplate) {
            return "xsl:call-template name=\""
                + ((ContextStackFrame.CallTemplate)frame).getTemplateName().getDisplayName() + "\"";
        } else if (frame instanceof ContextStackFrame.VariableEvaluation) {
            Object container = frame.getContainer();
            if (container instanceof Actor) {
                return getInstructionName((Actor)container);
            } else if (container instanceof TemplateRule) {
                return "xsl:template match=\"" + ((TemplateRule)container).getMatchPattern().toString() + "\"";
            }
        }
        return null;
    }

    private static String getInstructionName(Actor actor) {
        StructuredQName name = actor.getObjectName();
        String objectName = name == null ? "" : name.getDisplayName();
        if (actor instanceof UserFunction) {
            return "function " + objectName + "()";
        } else if (actor instanceof NamedTemplate) {
            return "template name=\"" + objectName + "\"";
        } else if (actor instanceof AttributeSet) {
            return "attribute-set " + objectName;
        } else if (actor instanceof KeyDefinition) {
            return "key " + objectName;
        } else if (actor instanceof GlobalVariable) {
            StructuredQName qName = ((GlobalVariable)actor).getVariableQName();
            if (qName.hasURI(NamespaceConstant.SAXON_GENERATED_VARIABLE)) {
                return "optimizer-created global variable";
            } else {
                return "global variable $" + qName.getDisplayName();
            }
        } else {
            return "procedure " + objectName;
        }
    }

    private static abstract class SourceLocatorWithInstructionName implements SourceLocator {

        private final SourceLocator locator;

        public SourceLocatorWithInstructionName(SourceLocator locator) {
            this.locator = locator;
        }

        public String getPublicId() {
            return locator != null ? locator.getPublicId() : null;
        }

        public String getSystemId() {
            return locator != null ? locator.getSystemId() : null;
        }

        public int getLineNumber() {
            return locator != null ? locator.getLineNumber() : -1;
        }

        public int getColumnNumber() {
            return locator != null ? locator.getColumnNumber() : -1;
        }

        protected abstract String getInstructionName();

        @Override
        public String toString() {
            StringBuilder s = new StringBuilder();
            String instructionName = getInstructionName();
            String fileName = getSystemId();
            if (fileName != null && !"".equals(fileName)) {
                if (fileName.lastIndexOf('/') >= 0)
                    fileName = fileName.substring(fileName.lastIndexOf('/') + 1);
                s.append(fileName);
                int line = getLineNumber();
                if (line > 0)
                    s.append(":" + getLineNumber());
            }
            if (instructionName != null && !"".equals(instructionName)) {
                if (s.length() > 0)
                    s.insert(0, instructionName + "(").append(")");
                else
                    s.append(instructionName);
            } else if (s.length() == 0)
                s.append("?");
            return s.toString();
        }
    }

    public static class XProcLocator extends SourceLocatorWithInstructionName {
        private final Step step;
        private final String instructionName;
        public XProcLocator(Step step) {
            super(getLocator(step != null ? step.getNode() : null));
            this.step = step;
            if (step == null || step.getNode() == null) {
                instructionName = null;
            } else {
                String name = step.getName();
                instructionName = step.getNode().getNodeName().getClarkName()
                + ((name.startsWith("#") || name.startsWith("!")) ? "" : (" name=\"" + name + "\""));
            }
        }
        public Step getStep() {
            return step;
        }
        protected String getInstructionName() {
            return instructionName;
        }
    }

    private static class JavaFrame implements SourceLocator {
        public final StackTraceElement frame;
        public JavaFrame(StackTraceElement frame) {
            this.frame = frame;
        }
        public String getPublicId() {
            return null;
        }
        public String getSystemId() {
            return frame.getFileName();
        }
        public int getLineNumber() {
            return frame.getLineNumber();
        }
        public int getColumnNumber() {
            return -1;
        }
        @Override
        public String toString() {
            return frame.toString();
        }
    }

    // adapted from java.lang.Throwable
    private String printEnclosedLocation(SourceLocator[] enclosingLocation) {
        StringBuilder s = new StringBuilder();
        String message = getMessage();
        if (errorCode != null) {
            s.append("[").append(errorCode).append("]");
            if (message != null)
                s.append(" ");
        }
        if (message != null)
            s.append(message);
        else if (errorCode == null)
            s.append((String)null);
        int m = location.length - 1;
        int n = enclosingLocation.length - 1;
        while (m >= 0 && n >=0 && location[m].equals(enclosingLocation[n])) {
            m--;
            n--;
        }
        int inCommon = location.length - 1 - m;
        for (int i = 0; i <= m; i++)
            if (location[i] != NO_LOCATOR)
                s.append("\n\tat " + location[i]);
        if (inCommon != 0)
            s.append("\n\t... " + inCommon + " more");
        if (errorCause != null) {
            s.append("\nCaused by: ");
            s.append(errorCause.printEnclosedLocation(location));
        }
        return s.toString();
    }

    @Override
    public String toString() {
        return printEnclosedLocation(new SourceLocator[]{});
    }

    private static void serializeLocation(SourceLocator[] location, TreeWriter writer) {
        boolean empty = true;
        for (SourceLocator l : location) {
            if (l.getSystemId() != null || l.getLineNumber() > 0) {
                empty = false;
                break;
            }
        }
        if (empty) return;
        writer.addStartElement(px_location);
        writer.startContent();
        for (SourceLocator l : location) {
            if (l.getSystemId() != null || l.getLineNumber() > 0) {
                writer.addStartElement(px_file);
                if (l.getSystemId() != null)
                    writer.addAttribute(_href, l.getSystemId());
                int line = l.getLineNumber();
                if (line > 0)
                    writer.addAttribute(_line, ""+line);
                int column = l.getColumnNumber();
                if (column > 0)
                    writer.addAttribute(_column, ""+column);
                writer.addEndElement();
            }
        }
        writer.addEndElement();
    }

    public void serialize(TreeWriter writer) {
        writer.addStartElement(c_error);
        if (errorCode != null) {
            StructuredQName qCode = new StructuredQName(errorCode.getPrefix(), errorCode.getNamespaceURI(), errorCode.getLocalName());
            writer.addNamespace(qCode.getPrefix(), qCode.getNamespaceBinding().getURI());
            writer.addAttribute(_code, qCode.getDisplayName());
        }
        if (location[0] instanceof XProcLocator) {
            Step step = ((XProcLocator)location[0]).step;
            if (step != null) {
                writer.addAttribute(_name, step.getName());
                writer.addAttribute(_type, step.getType().toString());
            }
        }
        if (location[0].getSystemId() != null)
            writer.addAttribute(_href, location[0].getSystemId());
        if (location[0].getLineNumber() > 0)
            writer.addAttribute(_line, ""+location[0].getLineNumber());
        if (location[0].getColumnNumber() > 0)
            writer.addAttribute(_column, ""+location[0].getColumnNumber());
        writer.startContent();
        if (errorContent != null)
            writer.addSubtree(errorContent);
        else {
            String message = getMessage();
            if (message != null)
                writer.addText(message);
        }
        serializeLocation(location, writer);
        if (errorCause != null) {
            writer.addStartElement(px_cause);
            writer.startContent();
            errorCause.serialize(writer);
            writer.addEndElement();
        }
        writer.addEndElement();
    }
}
