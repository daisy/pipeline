<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                name="main"
                type="css:split"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Split a document into parts.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The input is a single document.
        </p:documentation>
    </p:input>
    
    <p:option name="split-before" required="true">
        <p:documentation>
            The 'split-before' option must be an XSLTMatchPattern that matches only elements.
        </p:documentation>
    </p:option>
    
    <p:option name="split-after" required="true">
        <p:documentation>
            The 'split-after' option must be an XSLTMatchPattern that matches only elements.
        </p:documentation>
    </p:option>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            The output is a sequence of one or more documents and is the result of splitting the
            input document before or after the split points specified with the 'split-before' and
            'split-after' options. Splitting before an element means duplicating the document,
            deleting the element and its following nodes from the first copy, and deleting the
            elements's preceding nodes from the second copy. Similarly, splitting after an element
            means deleting the element and its preceding nodes from the second copy and deleting the
            element's following nodes from the first copy. css:box elements that are split get a
            part attribute with value 'first', 'middle' or 'last'. The attributes css:id,
            css:string-set, css:string-entry, css:counter-set, css:counter-reset,
            css:counter-increment, css:counter-set-*, css:counter-reset-* and
            css:counter-increment-* are omitted on css:box elements with a part attribute equal to
            'middle' or 'last'.
        </p:documentation>
    </p:output>
    
    <p:identity>
        <p:input port="source">
            <p:document href="split.xsl"/>
        </p:input>
    </p:identity>
    <p:add-attribute match="/*/*[@name='split-before']" attribute-name="match">
        <p:with-option name="attribute-value" select="$split-before"/>
    </p:add-attribute>
    <p:add-attribute match="/*/*[@name='split-after']" attribute-name="match">
        <p:with-option name="attribute-value" select="$split-after"/>
    </p:add-attribute>
    <p:identity name="css-split.compiled-xslt"/>
    
    <p:xslt name="css-split.xslt">
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="source">
            <p:pipe port="source" step="main"/>
        </p:input>
        <p:input port="stylesheet">
            <p:pipe port="result" step="css-split.compiled-xslt"/>
        </p:input>
    </p:xslt>
    
    <p:filter select="/*/*"/>
    
</p:declare-step>
