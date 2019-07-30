<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:flow-from"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Channel elements out of named flows.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            flow() values in the input must be represented by css:flow elements with a 'from'
            attribute. Documents in the input sequence that represent a named flow must have this
            indicated with a css:flow attribute on the document element. All elements in named flows
            must have a css:anchor attribute that matches the css:id attribute of an element in the
            normal flow, which represents the original position of the element in the DOM
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            css:flow elements in the normal flow are substituted by a segment of the flow denoted by
            the 'from' attribute. Which segment of the flow is consumed is determined by the
            position of the css:flow element and the original positions of the elements in the named
            flow (denoted by their css:anchor attributes). All elements in the named flow with an
            original position before the css:flow element but after the first preceding css:flow
            element with the same 'from' attribute, are consumed.
        </p:documentation>
    </p:output>
    
    <p:split-sequence test="/*[not(@css:flow)]" name="normal-flow"/>
    
    <p:for-each name="for-each" px:progress="1">
        <p:xslt name="result" px:progress="1">
            <p:input port="source">
                <p:pipe step="for-each" port="current"/>
                <p:pipe step="normal-flow" port="not-matched"/>
            </p:input>
            <p:input port="stylesheet">
                <p:document href="flow-from.xsl"/>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:for-each>
    <p:identity name="flowed"/>
    
    <p:identity>
        <p:input port="source">
            <p:pipe step="flowed" port="result"/>
            <p:pipe step="normal-flow" port="not-matched"/>
        </p:input>
    </p:identity>
    
</p:declare-step>
