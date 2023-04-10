<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                exclude-inline-prefixes="#all"
                type="px:check-files-wellformed" name="main">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Check that files exist and are well-formed XML</h1>
        <p>Given a list of files, ensure that each exists and is well-formed XML.</p>
    </p:documentation>

    <p:input port="source.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input fileset</p>
        </p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>

    <p:output port="result.fileset">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Output fileset which contains the well-formed files from <code>source</code>.</p>
        </p:documentation>
        <p:pipe step="check-files-xml" port="fileset"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="check-files-xml" port="in-memory"/>
    </p:output>

    <p:output port="report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>List of malformed files, formatted as <code>d:error</code> elements, or an empty
            <code>d:errors</code> element if nothing is missing.</p>
        </p:documentation>
        <p:pipe step="report" port="result"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Validation status (http://daisy.github.io/pipeline/StatusXML) of the file check.</p>
        </p:documentation>
        <p:pipe step="validation-status" port="result"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-purge
        </p:documentation>
    </p:import>
    <p:import href="check-files-exist.xpl">
        <p:documentation>
            px:check-files-exist
        </p:documentation>
    </p:import>
    <p:import href="create-validation-report-error-for-file.xpl">
        <p:documentation>
            pxi:create-validation-report-error-for-file
        </p:documentation>
    </p:import>
    <p:import href="validation-status.xpl">
        <p:documentation>
            px:validation-status
        </p:documentation>
    </p:import>

    <!-- first, make sure that the files exist (on disk or in memory) -->
    <px:check-files-exist name="check-files-exist">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:check-files-exist>

    <!-- then check whether files are XML -->
    <!-- files that are already in memory are assumed to be well-formed XML, unless they are c:data elements -->
    <!-- files that are not in memory yet are loaded as XML -->
    <p:group name="check-files-xml">
        <p:output port="fileset" primary="true">
            <p:pipe step="purge" port="result.fileset"/>
        </p:output>
        <p:output port="in-memory" sequence="true">
            <p:pipe step="xml" port="matched"/>
        </p:output>
        <p:output port="errors">
            <p:pipe step="errors" port="result"/>
        </p:output>
        <p:add-attribute match="d:file" attribute-name="method" attribute-value="xml"/>
        <px:fileset-load fail-on-not-found="false" name="load">
            <p:input port="in-memory">
                <p:pipe step="check-files-exist" port="result.in-memory"/>
            </p:input>
        </px:fileset-load>
        <p:split-sequence test="not(/c:data)" name="xml"/>
        <p:sink/>
        <p:delete match="d:file/@method">
            <p:input port="source">
                <p:pipe step="load" port="result.fileset"/>
            </p:input>
        </p:delete>
        <px:fileset-purge detect-existing="false" warn-on-missing="false" name="purge">
            <p:input port="source.in-memory">
                <p:pipe step="xml" port="matched"/>
            </p:input>
        </px:fileset-purge>
        <p:sink/>
        <p:for-each>
            <p:iteration-source select="//d:file">
                <p:pipe step="purge" port="purged"/>
            </p:iteration-source>
            <p:variable name="base" select="base-uri(/*)">
                <p:pipe step="purge" port="purged"/>
            </p:variable>
            <p:variable name="filepath" select="resolve-uri(/*/@href,$base)"/>
            <pxi:create-validation-report-error-for-file px:message="File not well-formed XML: {$filepath}"
                                                         px:message-severity="WARN">
                <p:with-option name="error-type" select="'file-not-wellformed'"/>
                <p:with-option name="desc" select="'File is not well-formed XML'"/>
                <p:with-option name="base" select="$base"/>
            </pxi:create-validation-report-error-for-file>
        </p:for-each>
        <p:identity name="errors"/>
    </p:group>
    <p:sink/>

    <!-- append the error report from the wellformedness check to the report from the initial check-files-exist step -->
    <p:insert match="d:errors" position="last-child" name="report">
        <p:input port="source">
            <p:pipe step="check-files-exist" port="report"/>
        </p:input>
        <p:input port="insertion">
            <p:pipe step="check-files-xml" port="errors"/>
        </p:input>
    </p:insert>

    <px:validation-status name="validation-status"/>
    <p:sink/>

</p:declare-step>
