<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:flow-from" name="main"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Channel elements out of named flows.</p>
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>flow() values in the input must be represented by css:flow elements with a 'from'
            attribute. Documents in the input sequence that represent a named flow must have this
            indicated with a css:flow attribute on the document element. All elements in named flows
            must have a css:anchor attribute that matches the css:id attribute of an element at the
            original position.</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>css:flow elements are substituted by a segment of the flow denoted by the 'from'
            attribute. Which segment of the flow is consumed is determined by the 'scope' attribute,
            the position of the css:flow element and the original positions of the elements in the
            named flow (denoted by their css:anchor attributes). With a 'scope' attribute of
            'document', all elements in the named flow are consumed. Without a 'scope' attribute,
            all elements in the named flow with an original position before the css:flow element but
            after the first preceding css:flow element with the same 'from' attribute, are
            consumed.</p>
        </p:documentation>
    </p:output>
    
    <p:for-each px:progress="1">
        <p:xslt name="result" px:progress="1">
            <p:input port="stylesheet">
                <p:document href="flow-from.xsl"/>
            </p:input>
            <p:with-param name="all-docs" select="collection()">
                <p:pipe step="main" port="source"/>
            </p:with-param>
        </p:xslt>
    </p:for-each>
    
</p:declare-step>
