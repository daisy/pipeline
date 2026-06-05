<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"  version="1.0"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xhtml="http://www.w3.org/1999/xhtml"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                type="pxi:dtbook-validator.check-images" name="main"
                exclude-inline-prefixes="#all">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1>Helper step for DTBook Validator</h1>
        <p>Checks to see if referenced images exist on disk.</p>
    </p:documentation>
    
    <!-- ***************************************************** -->
    <!-- INPUT, OUTPUT and OPTIONS -->
    <!-- ***************************************************** -->
    
    <p:input port="source.fileset" primary="true"/>
    <p:input port="source.in-memory" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input fileset</p>
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>List of missing images referenced from the DTBook(s), or an empty sequence if nothing is missing.</p>
        </p:documentation>
        <p:pipe port="report" step="check-images-exist"/>
    </p:output>
    
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation>
            px:check-files-exist
        </p:documentation>
    </p:import>
    
    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-load
            px:fileset-add-entry
            px:fileset-add-ref
            px:fileset-join
            px:fileset-interset
        </p:documentation>
    </p:import>
    
    <p:variable name="dtbook-uri" select="base-uri()"/>
    
    <px:fileset-load media-types="application/x-dtbook+xml" px:message="Checking that DTBook images exist on disk.">
        <p:input port="in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:fileset-load>
    
    <p:for-each>
        <p:iteration-source select="//dtb:img | //m:math"/>
        <p:variable name="refid" select="*/@id"/>
        
        <p:choose>
            <!-- dtb:img has @src -->
            <p:when test="*/@src">
                <p:variable name="imgpath" select="resolve-uri(*/@src, $dtbook-uri)"/>
                <px:fileset-add-entry>
                    <p:with-option name="href" select="$imgpath"/>
                    <p:input port="source.fileset">
                        <p:inline><d:fileset/></p:inline>
                    </p:input>
                </px:fileset-add-entry>
                <px:fileset-add-ref>
                  <p:with-option name="href" select="$imgpath"/>
                  <p:with-option name="ref" select="concat($dtbook-uri, '#', $refid)"/>
                </px:fileset-add-ref>
            </p:when>
            <!-- m:math has @altimg -->
            <p:otherwise>
                <p:variable name="imgpath" select="resolve-uri(*/@altimg, $dtbook-uri)"/>
                <px:fileset-add-entry>
                    <p:with-option name="href" select="$imgpath"/>
                    <p:input port="source.fileset">
                        <p:inline><d:fileset/></p:inline>
                    </p:input>
                </px:fileset-add-entry>
                <px:fileset-add-ref>
                  <p:with-option name="href" select="$imgpath"/>
                  <p:with-option name="ref" select="concat($dtbook-uri, '#', $refid)"/>
                </px:fileset-add-ref>
            </p:otherwise>
        </p:choose>
    </p:for-each>
    <px:fileset-join name="list-images"/>
    
    <!-- add file attributes from source fileset -->
    <px:fileset-intersect>
        <p:input port="source">
            <p:pipe step="main" port="source.fileset"/>
            <p:pipe step="list-images" port="result"/>
        </p:input>
    </px:fileset-intersect>
    
    <px:check-files-exist name="check-images-exist">
        <p:input port="source.in-memory">
            <p:pipe step="main" port="source.in-memory"/>
        </p:input>
    </px:check-files-exist>
    <p:sink/>
    
</p:declare-step>
