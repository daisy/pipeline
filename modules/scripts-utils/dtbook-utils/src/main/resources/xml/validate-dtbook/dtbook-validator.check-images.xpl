<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="dtbook-validator.check-images" type="px:dtbook-validator.check-images"
    xmlns:p="http://www.w3.org/ns/xproc" 
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"    
    xmlns:xhtml="http://www.w3.org/1999/xhtml" 
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:m="http://www.w3.org/1998/Math/MathML" 
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Helper step for DTBook Validator</h1>
        <p px:role="desc">Checks to see if referenced images exist on disk.</p>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- INPUT, OUTPUT and OPTIONS -->
    <!-- ***************************************************** -->
    
    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">source</h1>
            <p px:role="desc">A valid DTBook document.</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">List of missing images, or an empty sequence if nothing is missing.</p>
        </p:documentation>
        <p:pipe port="report" step="check-images-exist"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    
    <p:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xpl">
        <p:documentation>For manipulating files.</p:documentation>
    </p:import>
    
    <p:import
        href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation> Collection of utilities for validation and reporting. </p:documentation>
    </p:import>
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>Utilities for representing a fileset.</p:documentation>
    </p:import>
    
    <p:variable name="dtbook-uri" select="base-uri()"/>
    
    <px:message message="Checking that DTBook images exist on disk."/>
    
    <p:for-each name="list-images">
        <p:output port="result"/>
        <p:iteration-source select="//dtb:img | //m:math"/>
        <p:variable name="refid" select="*/@id"/>
        
        
        <p:choose>
            <!-- dtb:img has @src -->
            <p:when test="*/@src">
                <p:variable name="imgpath" select="resolve-uri(*/@src, $dtbook-uri)"/>
                <px:fileset-add-entry>
                    <p:with-option name="href" select="$imgpath"/>
                    <p:with-option name="ref" select="concat($dtbook-uri, '#', $refid)"/>
                    <p:input port="source">
                        <p:inline>
                            <d:fileset/>
                        </p:inline>
                    </p:input>
                </px:fileset-add-entry>
            </p:when>
            <!-- m:math has @altimg -->
            <p:otherwise>
                <p:variable name="imgpath" select="resolve-uri(*/@altimg, $dtbook-uri)"/>
                <px:fileset-add-entry>
                    <p:with-option name="href" select="$imgpath"/>
                    <p:with-option name="ref" select="concat($dtbook-uri, '#', $refid)"/>
                    <p:input port="source">
                        <p:inline>
                            <d:fileset/>
                        </p:inline>
                    </p:input>
                </px:fileset-add-entry>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    
    <!-- input fileset -->
    <px:fileset-create name="fileset-init"/>
    
    <!-- output fileset -->
    <px:fileset-join name="fileset-join">
        <p:input port="source">
            <p:pipe step="fileset-init" port="result"/>
            <p:pipe step="list-images" port="result"/>
        </p:input>
    </px:fileset-join>
    
    <px:check-files-exist name="check-images-exist">
        <p:input port="source">
            <p:pipe step="fileset-join" port="result"/>
        </p:input>
    </px:check-files-exist>  
    <p:sink/>
    
</p:declare-step>
