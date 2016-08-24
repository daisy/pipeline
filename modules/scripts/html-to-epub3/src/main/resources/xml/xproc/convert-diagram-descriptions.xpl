<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" type="pxi:convert-diagram-descriptions" name="main"
    version="1.0">

    <p:documentation><![CDATA[
        
Converts any DIAGRAM descriptions in the input fileset into HTML.

The primary port returns a fileset where old DIAGRAM entries have been replaced by
entries representing the newly produced HTML documents.

The secondary port returns the sequence of newly produced HTML documents.
        
]]></p:documentation>

    <p:input port="fileset.in"/>
    <p:option name="content-dir" required="true"/>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="convert"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="secondary" step="convert"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>

    <p:for-each name="descriptions">
        <p:output port="result"/>
        <p:iteration-source
            select="/d:fileset/d:file
            [ tokenize(@kind,'\s+') = 'description' 
            and @media-type=('application/xml','application/z3998-auth-diagram+xml')]"/>
        <p:load>
            <p:with-option name="href" select="/*/@original-href"/>
        </p:load>
    </p:for-each>
    <p:xslt name="convert" initial-mode="fileset">
        <p:input port="source">
            <p:pipe port="fileset.in" step="main"/>
            <p:pipe port="result" step="descriptions"/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/fileset-convert-diagram.xsl"/>
        </p:input>
        <p:with-param name="content-dir" select="$content-dir">
            <p:empty/>
        </p:with-param>
    </p:xslt>

</p:declare-step>
