<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" type="pxi:dtbook-to-html" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal/daisy3-to-epub3" version="1.0">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:output port="fileset.out" primary="true"/>
    <p:output port="in-memory.out" sequence="true">
        <p:pipe port="in-memory.out" step="to-html"/>
    </p:output>

    <p:option name="language" required="false" select="''"/>
    <p:option name="output-dir" required="true" px:output="result"/>
    <p:option name="filename" required="true">
        <!-- filename without extension -->
    </p:option>
    <p:option name="assert-valid" required="false" select="'true'"/>
    <p:option name="chunk-size" required="false" select="'-1'"/>

    <p:import
        href="http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/zedai-to-html/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <px:message message="Converting to ZedAI..."/>
    <px:dtbook-to-zedai name="to-zedai">
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
        <p:with-option name="opt-output-dir" select="concat($output-dir,'zedai/')"/>
        <p:with-option name="opt-zedai-filename" select="concat($filename,'.xml')"/>
        <p:with-option name="opt-lang" select="$language"/>
        <p:with-option name="opt-assert-valid" select="$assert-valid"/>
    </px:dtbook-to-zedai>

    <px:message message="Converting to XHTML5..."/>
    <px:zedai-to-html name="to-html" chunk="true">
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.out" step="to-zedai"/>
        </p:input>
        <p:with-option name="output-dir" select="$output-dir"/>
        <p:with-option name="chunk-size" select="$chunk-size"/>
    </px:zedai-to-html>

</p:declare-step>
