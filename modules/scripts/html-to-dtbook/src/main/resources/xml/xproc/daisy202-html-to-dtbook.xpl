<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="pxi:daisy202-html-to-dtbook"
                name="main">

    <p:input port="html.fileset" primary="true"/>
    <p:input port="html.in-memory">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            Single HTML document
        </p:documentation>
    </p:input>
    <p:input port="resources.fileset"/>
    <p:input port="resources.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            Resources
        </p:documentation>
    </p:input>

    <p:output port="result.fileset" primary="true"/>
    <p:output port="result.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            DTBook document and resources
        </p:documentation>
        <p:pipe step="add-dtbook" port="result.in-memory"/>
    </p:output>

    <p:option name="dtbook-file-name" required="true"/>
    <p:option name="dtbook-css" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>
            px:set-base-uri
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
            px:fileset-filter
            px:fileset-copy
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/css-utils/library.xpl">
        <p:documentation>
            px:css-to-fileset
        </p:documentation>
    </p:import>

    <p:identity>
        <p:input port="source">
            <p:pipe step="main" port="html.in-memory"/>
        </p:input>
    </p:identity>

    <!--
        Convert HTML to DTBook
    -->
    <p:xslt px:progress="1/2">
        <p:input port="stylesheet">
            <p:document href="../xslt/xhtml2dtbook.xsl"/>
        </p:input>
        <p:with-param name="transformationMode" select="'DTBmigration'"/>
        <p:with-param name="cssURI" select="'dtbook.2005.basic.css'"/>
    </p:xslt>

    <!--
        Rename
    -->
    <px:set-base-uri name="dtbook">
        <p:with-option name="base-uri"
                       select="resolve-uri(($dtbook-file-name[.!=''],
                                            concat(replace(base-uri(/*),'^(.*)\.[^/\.]*$','$1'),'.xml'))[1],
                                           base-uri(/*))"/>
    </px:set-base-uri>
    <p:sink/>

    <!--
        Combine DTBook with resources
    -->
    <px:fileset-filter not-media-types="text/css" name="rm-css">
        <p:input port="source">
            <p:pipe step="main" port="resources.fileset"/>
        </p:input>
        <p:input port="source.in-memory">
            <p:pipe step="main" port="resources.in-memory"/>
        </p:input>
    </px:fileset-filter>
    <p:group name="add-css">
        <p:output port="fileset" primary="true"/>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="rm-css" port="result.in-memory"/>
        </p:output>
        <p:sink/>
        <px:css-to-fileset>
            <p:with-option name="source" select="$dtbook-css"/>
        </px:css-to-fileset>
        <px:fileset-copy name="css">
            <p:with-option name="target" select="resolve-uri('./',base-uri(/*))">
                <p:pipe step="dtbook" port="result"/>
            </p:with-option>
        </px:fileset-copy>
        <p:sink/>
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="rm-css" port="result"/>
                <p:pipe step="css" port="result.fileset"/>
            </p:input>
        </px:fileset-join>
    </p:group>
    <px:fileset-add-entry media-type="application/x-dtbook+xml" name="add-dtbook" px:progress="1/20">
        <p:input port="source.in-memory">
            <p:pipe step="add-css" port="in-memory"/>
        </p:input>
        <p:input port="entry">
            <p:pipe step="dtbook" port="result"/>
        </p:input>
        <p:with-param port="file-attributes" name="omit-xml-declaration" select="'false'"/>
        <p:with-param port="file-attributes" name="version" select="'1.0'"/>
        <p:with-param port="file-attributes" name="encoding" select="'utf-8'"/>
        <p:with-param port="file-attributes" name="doctype-public" select="'-//NISO//DTD dtbook 2005-3//EN'"/>
        <p:with-param port="file-attributes" name="doctype-system" select="'http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd'"/>
    </px:fileset-add-entry>

</p:declare-step>
