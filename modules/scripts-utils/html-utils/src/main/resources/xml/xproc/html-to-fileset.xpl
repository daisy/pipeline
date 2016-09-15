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

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">For manipulating
            filesets.</p:documentation>
    </p:import>
    
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
    <!--<p:viewport match="/*/d:file">
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>
    </p:viewport>-->
    <!--<p:group name="fileset">
        <p:output port="result"/>
        <p:variable name="doc-uri" select="base-uri(/*)"/>
        <p:variable name="fileset-base" select="replace($doc-uri,'[^/]+$','')"/>
        
        <!-\- Create resource fileset -\->
        <p:for-each>
            <p:iteration-source select="//z:object[@src]"/>
            <p:variable name="href" select="/*/@src"/>
            <p:variable name="media-type" select="(/*/@srctype,'')[1]"/>
            <px:fileset-create>
                <p:with-option name="base" select="$fileset-base"/>
            </px:fileset-create>
            <px:fileset-add-entry>
                <p:with-option name="href" select="$href"/>
                <p:with-option name="media-type" select="$media-type"/>
            </px:fileset-add-entry>
            <p:add-attribute match="/*/*" attribute-name="original-href">
                <p:with-option name="attribute-value" select="resolve-uri($href,$doc-uri)"/>
            </p:add-attribute>
        </p:for-each>
        <px:fileset-join/>
        <px:mediatype-detect name="fileset.resources"/>

        <!-\-Create a fileset for the XHTML doc itself-\->
        <px:fileset-create>
            <p:with-option name="base" select="$fileset-base"/>
        </px:fileset-create>
        <px:fileset-add-entry>
            <p:with-option name="href" select="$doc-uri"/>
            <p:with-option name="media-type" select="'application/xhtml+xml'"/>
        </px:fileset-add-entry>
        <p:add-attribute match="/*/*" attribute-name="original-href">
            <p:with-option name="attribute-value" select="$doc-uri"/>
        </p:add-attribute>
        <p:identity name="fileset.zedai"/>
        
        <!-\-Join the filesets-\->
        <px:fileset-join>
            <p:input port="source">
                <p:pipe port="result" step="fileset.zedai"/>
                <p:pipe port="result" step="fileset.resources"/>
            </p:input>
        </px:fileset-join>
    </p:group>-->

</p:declare-step>
