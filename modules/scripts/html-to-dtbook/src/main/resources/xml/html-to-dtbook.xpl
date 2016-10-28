<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="html-to-dtbook" type="px:html-to-dtbook"
    px:input-filesets="html"
    px:output-filesets="dtbook"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:html="http://www.w3.org/1999/xhtml" xpath-version="2.0" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <!-- Script documentation -->
        <h1 px:role="name">HTML to DTBook</h1>
        <p px:role="desc">Converts an HTML document into DTBook</p>
    </p:documentation>

    <p:option name="html" required="true" px:type="anyFileURI" px:media-type="application/xhtml+xml text/html">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">HTML</h2>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="true" px:output="result" px:type="anyDirURI">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Output</h2>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/html-utils/library.xpl"/>
    <p:import href="html-to-dtbook.convert.xpl"/>

    <px:html-load name="html">
        <p:with-option name="href" select="$html"/>
    </px:html-load>

    <p:group name="convert">
        <p:output port="in-memory" primary="true">
            <p:pipe port="in-memory.out" step="convert.convert"/>
        </p:output>
        <p:output port="fileset">
            <p:pipe port="fileset.out" step="convert.fileset.out"/>
        </p:output>

        <p:identity name="convert.in-memory.in"/>

        <p:for-each>
            <p:iteration-source select="//html:link[@href]|//html:*[@src]"/>

            <!-- Find all auxilliary resuorces and create d:files for them -->
            <p:variable name="href" select="(/*/@src,/*/@href)[1]"/>
            <p:add-attribute attribute-name="href" match="/*">
                <p:with-option name="attribute-value" select="replace($href,'^\./','')"/>
                <p:input port="source">
                    <p:inline>
                        <d:file/>
                    </p:inline>
                </p:input>
            </p:add-attribute>

            <!-- Set correct base uri for each d:file -->
            <p:add-attribute attribute-name="xml:base" match="/*">
                <p:with-option name="attribute-value" select="p:resolve-uri($href,$html)"/>
            </p:add-attribute>
            <p:delete match="/*/@xml:base"/>
        </p:for-each>
        <p:wrap-sequence wrapper="d:fileset"/>
        <p:add-attribute attribute-name="xml:base" match="/*">
            <p:with-option name="attribute-value" select="p:resolve-uri('.',$html)"/>
        </p:add-attribute>
        <p:identity name="convert.fileset.in"/>

        <px:html-to-dtbook-convert name="convert.convert">
            <p:with-option name="output-dir" select="$output-dir"/>
            <p:input port="in-memory.in">
                <p:pipe port="result" step="convert.in-memory.in"/>
            </p:input>
            <p:input port="fileset.in">
                <p:pipe port="result" step="convert.fileset.in"/>
            </p:input>
        </px:html-to-dtbook-convert>
        <p:viewport match="d:file" name="convert.fileset.out">
            <p:output port="fileset.out"/>
            <p:choose>
                <p:when test="/*/@media-type='application/x-dtbook+xml'">
                    <p:add-attribute match="/*" attribute-name="doctype-public" attribute-value="-//NISO//DTD dtbook 2005-3//EN"/>
                    <p:add-attribute match="/*" attribute-name="doctype-system" attribute-value="http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd"/>
                </p:when>
                <p:otherwise>
                    <p:identity/>
                </p:otherwise>
            </p:choose>
        </p:viewport>
    </p:group>

    <px:fileset-store>
        <p:input port="fileset.in">
            <p:pipe port="fileset" step="convert"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory" step="convert"/>
        </p:input>
    </px:fileset-store>
    
</p:declare-step>
