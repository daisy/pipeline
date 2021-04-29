/*
 * RuntimeValue.java
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

package com.xmlcalabash.model;

import java.net.URI;
import java.util.Hashtable;

import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.ItemTypeFactory;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmAtomicValue;
import net.sf.saxon.s9api.XdmItem;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.s9api.XdmSequenceIterator;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.ItemType;
import net.sf.saxon.value.StringValue;
import com.xmlcalabash.core.XProcException;
import com.xmlcalabash.core.XProcRuntime;


/**
 *
 * @author ndw
 */
public class RuntimeValue {
    private XdmValue value = null;
    private String stringValue = null;
    private XdmNode node = null;
    private boolean initialized = false;
    private Hashtable<String,String> nsBindings = null;

    public RuntimeValue() {
        // nop; returns an uninitialized value
    }

    public RuntimeValue(String stringValue, XdmNode node) {
        this.stringValue = stringValue;
        this.node = node;
        initialized = true;

        nsBindings = new Hashtable<String,String> ();
        XdmSequenceIterator nsIter = node.axisIterator(Axis.NAMESPACE);
        while (nsIter.hasNext()) {
            XdmNode ns = (XdmNode) nsIter.next();
            QName nodeName = ns.getNodeName();
            String uri = ns.getStringValue();

            if (nodeName == null) {
                // Huh?
                nsBindings.put("", uri);
            } else {
                String localName = nodeName.getLocalName();
                nsBindings.put(localName,uri);
            }
        }
    }

    public RuntimeValue(String stringValue, XdmNode node, Hashtable<String,String> nsBindings) {
        this.stringValue = stringValue;
        this.node = node;
        this.nsBindings = nsBindings;
        initialized = true;
    }

    public RuntimeValue(String stringValue, XdmValue value, XdmNode node, Hashtable<String,String> nsBindings) {
        this.stringValue = stringValue;
        this.value = value;
        this.node = node;
        this.nsBindings = nsBindings;
        initialized = true;
    }

    public RuntimeValue(String stringValue) {
        this.stringValue = stringValue;
        initialized = true;
    }

    /*
    public void setComputableValue(ComputableValue value) {
        val = value;
        initialized = true;
    }
    */

    public boolean initialized() {
        return initialized;
    }

    public XdmAtomicValue getUntypedAtomic(XProcRuntime runtime) {
        try {
            ItemTypeFactory itf = new ItemTypeFactory(runtime.getProcessor());
            ItemType untypedAtomic = itf.getAtomicType(new QName(NamespaceConstant.SCHEMA, "xs:untypedAtomic"));
            XdmAtomicValue val = new XdmAtomicValue(stringValue, untypedAtomic);
            return val;
        } catch (SaxonApiException sae) {
            throw new XProcException(sae);
        }
    }

    public String getString() {
        return stringValue;
    }

    public boolean hasGeneralValue() {
        return value != null;
    }

    public XdmValue getValue() {
        if (value == null) {
            // Turn the string value into an XdmValue
            return new XdmAtomicValue(stringValue);
        } else {
            return value;
        }
    }

    public StringValue getStringValue() {
        return new StringValue(stringValue);
    }

    public QName getQName() {
        // FIXME: Check the type
        // TypeUtils.checkType(runtime, value, )
        if (stringValue.contains(":")) {
            return new QName(stringValue, node);
        } else {
            return new QName("", stringValue);
        }
    }

    public XdmNode getNode() {
        return node;
    }
    
    public URI getBaseURI() {
        return node.getBaseURI();
    }

    public Hashtable<String,String> getNamespaceBindings() {
        return nsBindings;
    }

    public boolean getBoolean() {
        if ("true".equals(stringValue) || "1".equals(stringValue)) {
            return true;
        } else if ("false".equals(stringValue) || "0".equals(stringValue)) {
            return false;
        } else {
            throw new XProcException(node, "Non boolean string: " + stringValue);
        }
    }

    public int getInt() {
        int result = Integer.parseInt(stringValue);
        return result;
    }

    public long getLong() {
        long result = Long.parseLong(stringValue);
        return result;
    }

    public XdmSequenceIterator getNamespaces() {
        return node.axisIterator(Axis.NAMESPACE);
    }
}
