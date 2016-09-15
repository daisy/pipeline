<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="check-files-wellformed" type="px:check-files-wellformed"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Check that files exist and are well-formed XML</h1>
        <p px:role="desc">Given a list of files, ensure that each exists and is well-formed XML.</p>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUT, OUTPUT and OPTIONS -->
    <!-- ***************************************************** -->

    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">source</h1>
            <p px:role="desc">A list of files, formatted as a FileSet
                (http://code.google.com/p/daisy-pipeline/wiki/FileSetUtils).</p>
        </p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">List of well-formed files, formatted as a DAISY Pipeline FileSet.</p>
        </p:documentation>
        <p:pipe port="result" step="wrap-fileset"/>
    </p:output>

    <p:output port="report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">List of malformed files, formatted as &lt;d:error&gt; elements, or an
                empty d:errors element if nothing is missing.</p>
        </p:documentation>
        <p:pipe port="result" step="process-errors"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">validation-status</h1>
            <p px:role="desc">Validation status (http://code.google.com/p/daisy-pipeline/wiki/ValidationStatusXML) of the file check.</p>
        </p:documentation>
        <p:pipe step="format-validation-status" port="result"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>Utilities for representing a fileset.</p:documentation>
    </p:import>

    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>For manipulating files.</p:documentation>
    </p:import>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <p:import href="check-files-exist.xpl"/>
    
    <p:import href="create-validation-report-error-for-file.xpl"/>
    
    <p:import href="validation-status.xpl"/>

    <p:variable name="base" select="/*/@xml:base"/>
    
    <!-- first, make sure that the files exist on disk -->
    <px:check-files-exist name="check-files-exist"/>

    <p:for-each name="check-each-file">
        <p:iteration-source select="//d:file">
            <p:pipe port="result" step="check-files-exist"/>
        </p:iteration-source>
        <p:output port="result" sequence="true">
            <p:pipe port="result" step="try-loading-each-file"/>
        </p:output>
        <p:output port="report" sequence="true">
            <p:pipe port="report" step="try-loading-each-file"/>
        </p:output>

        <p:variable name="filepath" select="resolve-uri(*/@href, $base)"/>
        <p:try name="try-loading-each-file">
            <p:group>
                <p:output port="result" sequence="true">
                    <p:pipe port="result" step="create-fileset-entry"/>
                </p:output>
                <p:output port="report" sequence="true">
                    <p:pipe port="result" step="empty-error"/>
                </p:output>
                
                <p:load name="load-file">
                    <p:with-option name="href" select="$filepath"/>
                </p:load>
                
                <p:identity name="empty-error">
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
                <p:sink/>
                
                <px:fileset-add-entry name="create-fileset-entry">
                    <p:with-option name="href" select="$filepath"/>
                    <p:input port="source">
                        <p:inline>
                            <d:fileset/>
                        </p:inline>
                    </p:input>
                </px:fileset-add-entry>
            </p:group>

            <p:catch>
                <p:output port="report" sequence="true">
                    <p:pipe port="result" step="create-error"/>
                </p:output>
                
                <p:output port="result" sequence="true">
                    <p:pipe port="result" step="empty-fileset"/>
                </p:output>
                
                <px:message>
                    <p:with-option name="message"
                        select="concat('File not well-formed XML: ', $filepath)"/>
                </px:message>
                
                <p:identity name="empty-fileset">
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
                
                <pxi:create-validation-report-error-for-file name="create-error">
                    <p:input port="source">
                        <p:pipe port="current" step="check-each-file"/>
                    </p:input>
                    <p:with-option name="error-type" select="'file-not-wellformed'"/>
                    <p:with-option name="desc" select="'File is not well-formed XML'"/>
                    <p:with-option name="base" select="$base"/>
                </pxi:create-validation-report-error-for-file>
            </p:catch>
        </p:try>
        
    </p:for-each>

    <!-- append the error report from the wellformedness check to the report from the initial check-files-exist step -->
    <p:insert match="d:errors" position="last-child" name="process-errors">
        <p:input port="source">
            <p:pipe port="report" step="check-files-exist"/>
        </p:input>
        <p:input port="insertion">
            <p:pipe port="report" step="check-each-file"/>
        </p:input>    
    </p:insert>
    
    <px:validation-status name="format-validation-status">
        <p:input port="source">
            <p:pipe port="result" step="process-errors"/>
        </p:input>
    </px:validation-status>
    <p:sink/>    

    <p:group name="wrap-fileset">
        <p:output port="result"/>

        <!-- input fileset -->
        <px:fileset-create name="fileset.in-memory-base"/>

        <!-- output fileset -->
        <px:fileset-join>
            <p:input port="source">
                <p:pipe step="fileset.in-memory-base" port="result"/>
                <p:pipe step="check-each-file" port="result"/>
            </p:input>
        </px:fileset-join>
    </p:group>
    <p:sink/>
</p:declare-step>
