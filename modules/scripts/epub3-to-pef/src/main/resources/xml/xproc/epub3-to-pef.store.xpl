<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef.store" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:option name="epub" required="true"/>
    <p:input port="opf" primary="false"/>
    <p:input port="obfl" primary="false">
        <p:empty/>
    </p:input>
    <p:input port="css" primary="false">
        <p:empty/>
    </p:input>
    <p:input port="pef" primary="true">
        <p:empty/>
    </p:input>
    
    <p:option name="output-dir" select="''"/>
    <p:option name="pef-output-dir" select="''"/>
    <p:option name="preview-output-dir" select="''"/>
    <p:option name="obfl-output-dir" select="''"/>
    <p:option name="css-output-dir" select="''"/>
    
    <p:option name="include-preview" select="'false'"/>
    <p:option name="include-pef" select="'false'"/>
    <p:option name="include-css" select="false()" cx:as="xs:boolean"/>
    <p:option name="output-file-format" select="''"/>
    <p:option name="preview-table" select="''"/>
    
    <p:import href="xml-to-pef.store.xpl">
        <p:documentation>
            px:xml-to-pef.store
        </p:documentation>
    </p:import>

    <p:variable name="name" select="if (ends-with(lower-case($epub),'.epub'))
                                    then replace($epub,'^.*/([^/]*)\.[^/\.]*$','$1')
                                    else (/opf:package/opf:metadata/dc:identifier[not(@refines)],
                                          'unknown-identifier')[1]">
        <p:pipe step="main" port="opf"/>
    </p:variable>

    <p:sink/>
    <!-- store HTML with CSS first in case something goes wrong in px:xml-to-pef.store -->
    <p:group px:progress=".1">
        <p:documentation>
            Store HTML with inline CSS
        </p:documentation>
        <p:count>
            <p:input port="source">
                <p:pipe step="main" port="css"/>
            </p:input>
        </p:count>
        <p:choose>
            <p:when px:message="Storing HTML with inline CSS"
                    test="$include-css and $css-output-dir!='' and number(string(/*))&gt;0">
                <p:store encoding="utf-8" omit-xml-declaration="false">
                    <p:input port="source">
                        <p:pipe step="main" port="css"/>
                    </p:input>
                    <p:with-option name="href" select="concat($css-output-dir,'/',$name,'.html')"/>
                </p:store>
            </p:when>
            <p:otherwise>
                <p:sink/>
            </p:otherwise>
        </p:choose>
    </p:group>
    
    <px:xml-to-pef.store>
        <p:input port="pef">
            <p:pipe step="main" port="pef"/>
        </p:input>
        <p:input port="obfl">
            <p:pipe step="main" port="obfl"/>
        </p:input>
        <p:with-option name="name" select="$name">
            <p:empty/>
        </p:with-option>
        <p:with-option name="include-pef" select="$include-pef"/>
        <p:with-option name="include-preview" select="$include-preview"/>
        <p:with-option name="output-file-format" select="$output-file-format"/>
        <p:with-option name="preview-table" select="$preview-table"/>
        <p:with-option name="output-dir" select="$output-dir"/>
        <p:with-option name="pef-output-dir" select="$pef-output-dir"/>
        <p:with-option name="preview-output-dir" select="$preview-output-dir"/>
        <p:with-option name="obfl-output-dir" select="$obfl-output-dir"/>
    </px:xml-to-pef.store>
    
</p:declare-step>
