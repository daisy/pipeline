<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="check-files-exist" type="px:check-files-exist"
    xmlns:p="http://www.w3.org/ns/xproc" 
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"    
    xmlns:xhtml="http://www.w3.org/1999/xhtml" 
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Check that files exist on disk</h1>
        <p px:role="desc">Given a list of files, ensure that each exists on disk.</p>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- INPUT, OUTPUT and OPTIONS -->
    <!-- ***************************************************** -->
    
    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">source</h1>
            <p px:role="desc">A list of files, formatted as a FileSet (http://code.google.com/p/daisy-pipeline/wiki/FileSetUtils).</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">List of existing files, formatted as a DAISY Pipeline FileSet.</p>
        </p:documentation>
        <p:pipe port="result" step="wrap-fileset"/>
    </p:output>
    
    <p:output port="report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">List of missing files, formatted as &lt;d:error&gt; elements, or an empty d:errors element if nothing is missing.</p>
        </p:documentation>
        <p:pipe port="result" step="wrap-errors"/>
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
    
    <p:import href="create-validation-report-error-for-file.xpl"/>
    <p:import href="validation-status.xpl"/>
    
    <p:variable name="base" select="/*/@xml:base"/>
    
    <p:for-each name="check-each-file">
        <p:iteration-source select="//d:file"/>
        <p:output port="result" sequence="true">
            <p:pipe port="result" step="file-exists"/>
        </p:output>
        <p:output port="report" sequence="true">
            <p:pipe port="report" step="file-exists"/>
        </p:output>
        
        <p:variable name="filepath" select="resolve-uri(*/@href, $base)"/>    
        <p:try>
            <p:group>
                <px:info>
                    <p:with-option name="href" select="$filepath"/>
                </px:info>
            </p:group>
            <p:catch>
                <p:identity>
                    <p:input port="source">
                        <p:empty/>
                    </p:input>
                </p:identity>
            </p:catch>
        </p:try>
        
        <p:wrap-sequence wrapper="info"/>
        
        <!-- the <info> element, generated above, will be empty if the file was not found -->
        <p:choose name="file-exists">
            <p:when test="empty(/info/*)">
                <p:output port="report" sequence="true">
                    <p:pipe port="result" step="create-error"/>
                </p:output>
                
                <p:output port="result" sequence="true">
                    <p:pipe port="result" step="empty-fileset"/>
                </p:output>
                
                <px:message>
                    <p:with-option name="message" select="concat('File not found: ', $filepath)"/>
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
                    <p:with-option name="error-type" select="'file-not-found'"/>
                    <p:with-option name="desc" select="'File not found'"/>
                    <p:with-option name="base" select="$base"/>
                </pxi:create-validation-report-error-for-file>
            </p:when>
            <p:otherwise>
                <p:output port="result" sequence="true">
                    <p:pipe port="result" step="create-fileset-entry"/>
                </p:output>
                <p:output port="report" sequence="true">
                    <p:pipe port="result" step="empty-error"/>
                </p:output>
                
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
            </p:otherwise>
        </p:choose>
    </p:for-each>  
    
    <p:wrap-sequence wrapper="errors" wrapper-prefix="d" wrapper-namespace="http://www.daisy.org/ns/pipeline/data" name="wrap-errors">
        <p:input port="source">
            <p:pipe port="report" step="check-each-file"/>
        </p:input>
    </p:wrap-sequence>
    
    <px:validation-status name="format-validation-status">
        <p:input port="source">
            <p:pipe port="result" step="wrap-errors"/>
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
