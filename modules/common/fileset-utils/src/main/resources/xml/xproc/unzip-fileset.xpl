<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:unzip-fileset" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" version="1.0" name="main">
    
    <p:option name="href" required="true"/>
    <p:option name="unzipped-basedir" required="true"/>
    <p:option name="encode-as-base64" select="'false'"/>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="zip.fileset"/>
    </p:output>

    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="zip.in-memory"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/zip-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl"/>

    <px:unzip>
        <p:with-option name="href" select="$href"/>
    </px:unzip>
    <p:rename match="/*" new-name="d:fileset"/>
    <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="$unzipped-basedir"/>
    </p:add-attribute>
    <p:delete match="/*/@*[not(name()='xml:base')]"/>
    <p:delete match="/*/*[ends-with(@name,'/')]"/>
    <p:viewport match="/*/*">
        <p:rename match="/*" new-name="d:file"/>
        <p:add-attribute match="/*" attribute-name="href">
            <p:with-option name="attribute-value" select="/*/@name"/>
        </p:add-attribute>
    </p:viewport>
    <p:delete match="/*/*/@*[not(name()='href')]"/>
    <px:mediatype-detect name="zip.fileset"/>
    
    <p:for-each>
        <p:iteration-source select="/*/*"/>
        <p:variable name="entry-href" select="/*/@href"/>
        <px:unzip>
            <p:with-option name="href" select="$href"/>
            <p:with-option name="file" select="/*/@href"/>
            <p:with-option name="content-type" select="if ($encode-as-base64='true') then 'application/octet-stream' else /*/@media-type"/>
        </px:unzip>
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="resolve-uri($entry-href, $unzipped-basedir)"/>
        </p:add-attribute>
    </p:for-each>
    <p:identity name="zip.in-memory"/>

</p:declare-step>
