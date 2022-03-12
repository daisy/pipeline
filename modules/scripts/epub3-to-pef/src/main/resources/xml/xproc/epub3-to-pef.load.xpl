<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef.load" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe step="add-preamble" port="in-memory"/>
    </p:output>
    
    <p:option name="epub" required="true"/>
    <p:option name="preamble" required="false" select="''"/>
    <p:option name="temp-dir" required="true">
        <p:documentation>Empty temporary directory dedicated to this step.</p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
            px:fileset-join
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-load
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/epub-utils/library.xpl">
        <p:documentation>
            px:epub-load
        </p:documentation>
    </p:import>
    
    <px:epub-load name="epub" version="3" px:progress="1">
        <p:with-option name="href" select="$epub"/>
        <p:with-option name="temp-dir" select="$temp-dir"/>
    </px:epub-load>

    <!--
        Add preamble HTML + resources
    -->
    <p:choose name="add-preamble">
        <p:when test="$preamble!=''">
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="epub" port="result.in-memory"/>
                <p:pipe step="preamble" port="result.in-memory"/>
            </p:output>
            <px:fileset-add-entry media-type="text/html" replace-attributes="true">
                <p:with-option name="href" select="$preamble"/>
                <p:with-param port="file-attributes" name="role" select="'preamble'"/>
            </px:fileset-add-entry>
            <px:html-load name="preamble"/>
            <p:sink/>
            <px:fileset-join>
                <p:input port="source">
                    <p:pipe step="epub" port="result.fileset"/>
                    <p:pipe step="preamble" port="result.fileset"/>
                </p:input>
            </px:fileset-join>
        </p:when>
        <p:otherwise>
            <p:output port="fileset" primary="true"/>
            <p:output port="in-memory" sequence="true">
                <p:pipe step="epub" port="result.in-memory"/>
            </p:output>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
