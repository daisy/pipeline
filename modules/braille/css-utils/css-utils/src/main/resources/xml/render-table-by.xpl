<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:render-table-by"
                exclude-inline-prefixes="#all"
                version="1.0">
    
    <p:documentation>
        Layout tables as lists.
    </p:documentation>
    
    <p:input port="source">
        <p:documentation>
            The input document must be valid HTML (namespace "http://www.w3.org/1999/xhtml") or
            DTBook ("http://www.daisy.org/z3986/2005/dtbook/"). The 'display', 'render-table-by' and
            'table-header-policy' properties of elements in the input must be declared in
            css:display, css:render-table-by and css:table-header-policy attributes.
        </p:documentation>
    </p:input>
    
    <p:output port="result">
        <p:documentation>
            Each table element with a 'display' property not equal to 'table' and with a
            'render-table-by' property is rendered according to the axes specified in the
            'render-table-by' property.
        </p:documentation>
    </p:output>
    
    <p:xslt px:progress="1">
        <p:input port="stylesheet">
            <p:document href="render-table-by.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>
    
</p:declare-step>
