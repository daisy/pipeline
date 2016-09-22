<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="px:html-to-fileset" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" version="1.0">

    <p:documentation>
        <p px:role="desc">Creates a fileset document for an XHTML document.</p>
        <p>The fileset entries are ordered as the resources appear in the input document</p>
    </p:documentation>

    <p:input port="source" primary="true" sequence="false">
        <p:documentation>
            <h2 px:role="name">Input XHTML</h2>
            <p px:role="desc">An XHTML document.</p>
        </p:documentation>
    </p:input>
    
    <p:output port="fileset.out" primary="true" sequence="false"/>

    <p:serialization port="fileset.out" indent="true"/>

    <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="base-uri(/*)"/>
    </p:add-attribute>
    <p:xslt name="fileset">
        <p:input port="stylesheet">
            <p:document href="../xslt/html-to-fileset.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>

</p:declare-step>
