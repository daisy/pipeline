<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-to-pef.load" version="1.0"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                name="main">
    
    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe step="epub" port="result.in-memory"/>
        <p:pipe step="preamble.in-memory" port="result"/>
    </p:output>
    
    <p:option name="epub" required="true"/>
    <p:option name="preamble" required="false" select="''"/>
    <p:option name="temp-dir" required="true">
        <p:documentation>Empty temporary directory dedicated to this step.</p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
            px:fileset-load
            px:fileset-join
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl">
        <p:documentation>
            px:html-to-fileset
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
    <p:sink/>

    <!--
        Add preamble HTML + resources
    -->
    <p:choose>
        <p:when test="$preamble!=''">
            <px:fileset-add-entry media-type="text/html">
                <p:with-option name="href" select="$preamble"/>
            </px:fileset-add-entry>
            <px:fileset-load/>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:empty/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    <p:identity name="preamble.in-memory"/>
    <p:sink/>
    
    <p:choose>
        <p:when test="$preamble!=''">
            <px:html-to-fileset>
                <p:input port="source">
                    <p:pipe step="preamble.in-memory" port="result"/>
                </p:input>
            </px:html-to-fileset>
            <px:fileset-add-entry replace-attributes="true">
                <p:input port="entry">
                    <p:pipe step="preamble.in-memory" port="result"/>
                </p:input>
                <p:with-param port="file-attributes" name="role" select="'preamble'"/>
            </px:fileset-add-entry>
            <p:identity name="preamble.fileset"/>
            <p:sink/>
            <px:fileset-join>
                <p:input port="source">
                    <p:pipe step="epub" port="result.fileset"/>
                    <p:pipe step="preamble.fileset" port="result"/>
                </p:input>
            </px:fileset-join>
        </p:when>
        <p:otherwise>
            <p:identity>
                <p:input port="source">
                    <p:pipe step="epub" port="result.fileset"/>
                </p:input>
            </p:identity>
        </p:otherwise>
    </p:choose>
    
</p:declare-step>
