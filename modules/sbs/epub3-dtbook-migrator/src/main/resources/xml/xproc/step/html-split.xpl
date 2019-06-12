<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    type="px:nordic-html-split-perform" name="main" version="1.0" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:html="http://www.w3.org/1999/xhtml">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true">
        <p:empty/>
    </p:input>

    <p:output port="fileset.out" primary="true">
        <p:pipe port="result" step="html-split.fileset.result"/>
    </p:output>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="result" step="html-split.in-memory.html"/>
        <p:pipe port="result" step="html-split.in-memory.resources"/>
    </p:output>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <px:fileset-load media-types="application/xhtml+xml" name="html-split.load-html">
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <px:assert test-count-min="1" test-count-max="1" message="There must be exactly one HTML file in the fileset." error-code="NORDICDTBOOKEPUB006"/>
    <p:identity name="html-split.html"/>

    <p:xslt name="html-split.split-html.annotate">
        <p:with-param name="output-dir" select="replace(base-uri(/*),'[^/]+$','')">
            <p:pipe port="result" step="html-split.html"/>
        </p:with-param>
        <p:input port="stylesheet">
            <p:document href="../../xslt/split-html.annotate.xsl"/>
        </p:input>
    </p:xslt>
    <p:xslt name="html-split.split-html">
        <p:with-param name="output-dir" select="replace(base-uri(/*),'[^/]+$','')">
            <p:pipe port="result" step="html-split.html"/>
        </p:with-param>
        <p:input port="stylesheet">
            <p:document href="../../xslt/split-html.xsl"/>
        </p:input>
    </p:xslt>
    <p:xslt name="html-split.fix-section-hierarchy">
        <p:with-param name="body-is-section" select="'true'"/>
        <p:input port="stylesheet">
            <p:document href="../../xslt/fix-section-hierarchy.xsl"/>
        </p:input>
    </p:xslt>
    <p:identity name="html-split.split"/>

    <p:for-each name="html-split.for-each">
        <p:iteration-source select="/*/*"/>
        <p:output port="html" primary="true" sequence="true">
            <p:pipe port="result" step="html-split.for-each.html"/>
        </p:output>
        <p:output port="fileset" sequence="true">
            <p:pipe port="result" step="html-split.for-each.fileset"/>
        </p:output>

        <p:variable name="base" select="base-uri(/*)"/>

        <p:identity name="html-split.for-each.html"/>

        <px:fileset-create name="html-split.for-each.create-fileset">
            <p:with-option name="base" select="replace($base,'[^/]+$','')"/>
        </px:fileset-create>
        <px:fileset-add-entry media-type="application/xhtml+xml" name="html-split.for-each.add-html-to-fileset">
            <p:with-option name="href" select="replace($base,'^.*/([^/]+)$','$1')"/>
        </px:fileset-add-entry>
        <p:add-attribute match="//d:file" attribute-name="omit-xml-declaration" attribute-value="false" name="html-split.for-each.dont-omit-xml-declaration"/>
        <p:add-attribute match="//d:file" attribute-name="version" attribute-value="1.0" name="html-split.for-each.xml-version"/>
        <p:add-attribute match="//d:file" attribute-name="encoding" attribute-value="utf-8" name="html-split.for-each.xml-encoding"/>
        <p:add-attribute match="//d:file" attribute-name="method" attribute-value="xhtml" name="html-split.for-each.method-xhtml"/>
        <p:add-attribute match="//d:file" attribute-name="indent" attribute-value="true" name="html-split.for-each.indent-true"/>
        <p:add-attribute match="//d:file" attribute-name="doctype" attribute-value="&lt;!DOCTYPE html&gt;" name="html-split.for-each.doctype-html"/>
        <p:identity name="html-split.for-each.fileset"/>
    </p:for-each>
    <p:identity name="html-split.in-memory.html"/>

    <px:fileset-load not-media-types="application/xhtml+xml" load-if-not-in-memory="false" name="html-split.load-resources">
        <p:input port="fileset">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-load>
    <p:identity name="html-split.in-memory.resources"/>

    <px:fileset-filter not-media-types="application/xhtml+xml" name="html-split.filter-html">
        <p:input port="source">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
    </px:fileset-filter>
    <p:identity name="html-split.fileset.resources"/>
    <px:fileset-join name="html-split.join-html-and-resource-filesets">
        <p:input port="source">
            <p:pipe port="fileset" step="html-split.for-each"/>
            <p:pipe port="result" step="html-split.fileset.resources"/>
        </p:input>
    </px:fileset-join>
    <p:identity name="html-split.fileset.result"/>

</p:declare-step>
