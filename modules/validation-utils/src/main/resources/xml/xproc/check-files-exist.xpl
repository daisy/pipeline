<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="#all"
                type="px:check-files-exist" name="main">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Check that files exist</h1>
        <p>Given a list of files, ensure that each exists.</p>
    </p:documentation>
    
    <p:input port="source.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input fileset.</p>
        </p:documentation>
    </p:input>
    <p:input port="source.in-memory" sequence="true">
        <p:empty/>
    </p:input>
    
    <p:output port="result.fileset" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Output fileset of files that exist in memory or on disk.</p>
        </p:documentation>
        <p:pipe step="purge" port="result.fileset"/>
    </p:output>
    <p:output port="result.in-memory" sequence="true">
        <p:pipe step="main" port="source.in-memory"/>
    </p:output>
    
    <p:output port="report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>List of missing files, formatted as &lt;d:error&gt; elements, or an empty d:errors element if nothing is missing.</p>
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
            px:fileset-purge
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
    
    <px:fileset-purge warn-on-missing="true" name="purge">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-purge>
    <p:sink/>
    
    <p:for-each>
        <p:iteration-source select="//d:file">
            <p:pipe step="purge" port="purged"/>
        </p:iteration-source>
        <pxi:create-validation-report-error-for-file>
            <p:with-option name="error-type" select="'file-not-found'"/>
            <p:with-option name="desc" select="'File not found'"/>
            <p:with-option name="base" select="base-uri(/*)">
                <p:pipe step="purge" port="purged"/>
            </p:with-option>
        </pxi:create-validation-report-error-for-file>
    </p:for-each>
    <p:wrap-sequence wrapper="errors" wrapper-prefix="d" wrapper-namespace="http://www.daisy.org/ns/pipeline/data"
                     name="report"/>
    <px:validation-status name="validation-status"/>
    <p:sink/>
    
</p:declare-step>
