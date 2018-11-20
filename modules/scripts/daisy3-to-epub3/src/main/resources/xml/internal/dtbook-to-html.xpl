<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="pxi:dtbook-to-html" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-epub3" version="1.0">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="in-memory.out" step="convert"/>
    </p:output>

    <p:option name="language" required="false" select="''"/>
    <p:option name="output-dir" required="true" px:output="result"/>
    <p:option name="assert-valid" required="false" select="'true'"/>
    <p:option name="chunk-size" required="false" select="'-1'"/>

    <p:import
        href="http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-to-html/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>


    <p:split-sequence name="first-dtbook" test="position()=1" initial-only="true"/>
    <p:sink/>

    <p:group name="convert">
        <p:output port="fileset.out" primary="true" sequence="false"/>
        <p:output port="in-memory.out" sequence="true">
            <p:pipe port="in-memory.out" step="to-html"/>
        </p:output>
        <p:variable name="encoded-title"
            select="replace(replace(base-uri(/),'^.*/([^/]+)$','$1'),'\.[^\.]*$','')">
            <p:pipe port="matched" step="first-dtbook"/>
        </p:variable>
        <p:identity>
            <p:input port="source">
                <p:pipe port="fileset.in" step="main"/>
            </p:input>
        </p:identity>

        <px:message message="Converting to ZedAI..."/>
        <px:dtbook-to-zedai-convert name="to-zedai">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.in" step="main"/>
            </p:input>
            <p:with-option name="opt-output-dir" select="concat($output-dir,'zedai/')"/>
            <p:with-option name="opt-zedai-filename" select="concat($encoded-title,'.xml')"/>
            <p:with-option name="opt-lang" select="$language"/>
            <p:with-option name="opt-assert-valid" select="$assert-valid"/>
        </px:dtbook-to-zedai-convert>

        <px:message message="Converting to XHTML5..."/>
        <px:zedai-to-html-convert name="to-html" chunk="true">
            <p:input port="in-memory.in">
                <p:pipe port="in-memory.out" step="to-zedai"/>
            </p:input>
            <p:with-option name="output-dir" select="$output-dir"/>
            <p:with-option name="chunk-size" select="$chunk-size"/>
        </px:zedai-to-html-convert>

    </p:group>
</p:declare-step>
