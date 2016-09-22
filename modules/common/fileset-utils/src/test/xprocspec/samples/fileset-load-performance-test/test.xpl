<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/fileset-load" xmlns:c="http://www.w3.org/ns/xproc-step" exclude-inline-prefixes="#all">

    <p:output port="result" sequence="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>

    <p:identity>
        <p:input port="source">
            <p:inline>
                <d:fileset>
                    <d:file href="556886-01-titlepage.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-02-colophon.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-03-foreword.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-04-toc.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-05-chapter-1.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-06-chapter-2.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-07-chapter-3.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-08-chapter-4.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-09-chapter-5.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-10-chapter-6.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-11-backmatter.xhtml" media-type="application/xhtml+xml"/>
                    <d:file href="556886-12-index.xhtml" media-type="application/xhtml+xml"/>
                </d:fileset>
            </p:inline>
        </p:input>
    </p:identity>
    <p:add-attribute match="/*" attribute-name="xml:base">
        <p:with-option name="attribute-value" select="replace(static-base-uri(),'^(.*/)[^/]*$','$1')"/>
    </p:add-attribute>
    <p:identity name="fileset"/>

    <p:for-each>
        <p:iteration-source>
            <p:document href="556886-05-chapter-1.xhtml"/>
            <p:document href="556886-06-chapter-2.xhtml"/>
            <p:document href="556886-07-chapter-3.xhtml"/>
            <p:document href="556886-08-chapter-4.xhtml"/>
            <p:document href="556886-09-chapter-5.xhtml"/>
            <p:document href="556886-10-chapter-6.xhtml"/>
        </p:iteration-source>
        <p:add-attribute match="/*" attribute-name="xml:base">
            <p:with-option name="attribute-value" select="base-uri(/*)"/>
        </p:add-attribute>
    </p:for-each>
    <p:identity name="in-memory"/>

    <!--<p:for-each>
        <p:iteration-source select="/*/*"/>-->
    <p:for-each>
        <p:iteration-source select="/*/*">
            <p:pipe port="result" step="fileset"/>
        </p:iteration-source>
        <px:fileset-load>
            <p:input port="fileset">
                <p:pipe port="result" step="fileset"/>
            </p:input>
            <p:input port="in-memory">
                <p:pipe port="result" step="in-memory"/>
            </p:input>
        </px:fileset-load>
    </p:for-each>
    <!--</p:for-each>-->

    <p:count/>

</p:declare-step>
