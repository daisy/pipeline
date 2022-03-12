<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="nimas-fileset-validator.fileset-filter"
    type="pxi:nimas-fileset-validator.fileset-filter" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:m="http://www.w3.org/1998/Math/MathML" xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:pkg="http://openebook.org/namespaces/oeb-package/1.0/" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Helper step for Nimas Fileset Validator</h1>
        <p px:role="desc">Create a fileset of manifest items with the specified media-type.</p>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUT, OUTPUT and OPTIONS -->
    <!-- ***************************************************** -->

    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">source</h1>
            <p px:role="desc">A package document</p>
        </p:documentation>
    </p:input>

    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">FileSet representing the filtered file list.</p>
        </p:documentation>
    </p:output>
    
    <p:option name="media-type" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">media-type</h1>
            <p px:role="desc">Media type of the files to list.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:message
        </p:documentation>
    </p:import>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>
            px:fileset-add-entry
            px:fileset-add-ref
            px:fileset-create
            px:fileset-join
        </p:documentation>
    </p:import>

    <p:variable name="package-doc-uri" select="base-uri()"/>

    <px:message>
        <p:with-option name="message" select="concat('Nimas fileset validator: Creating fileset filtered by ', $media-type)"/>
    </px:message>

    <!-- loop that creates a fileset for each file -->
    <p:for-each name="fileset.in-memory-files">
        <p:output port="result"/>
        <p:iteration-source select="//pkg:item[@media-type = $media-type]"/>
        <p:variable name="refid" select="*/@id"/>
        <p:variable name="filepath" select="resolve-uri(*/@href, $package-doc-uri)"/>
        
        <px:fileset-add-entry>
            <p:with-option name="href" select="$filepath"/>
            <p:input port="source">
                <p:inline>
                    <d:fileset/>
                </p:inline>
            </p:input>
        </px:fileset-add-entry>
        <px:fileset-add-ref>
            <p:with-option name="href" select="$filepath"/>
            <p:with-option name="ref" select="concat($package-doc-uri, '#', $refid)"/>
        </px:fileset-add-ref>
    </p:for-each>

    <!-- input fileset -->
    <px:fileset-create name="fileset.in-memory-base"/>

    <!-- output fileset -->
    <px:fileset-join name="fileset.in-memory">
        <p:input port="source">
            <p:pipe step="fileset.in-memory-base" port="result"/>
            <p:pipe step="fileset.in-memory-files" port="result"/>
        </p:input>
    </px:fileset-join>
    
</p:declare-step>
