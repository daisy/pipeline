<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
           xmlns:px="http://www.daisy.org/ns/pipeline/xproc">

    <p:import href="mark-transitions.xpl"/>
    <p:import href="select-by-base.xpl"/>
    <p:import href="select-by-position.xpl"/>
    <p:import href="xslt-for-each.xpl"/>
    <p:import href="delete-parameters.xpl"/>
    <p:import href="apply-stylesheets.xpl"/>

    <p:declare-step type="px:transform">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Apply an <a
            href="http://daisy.github.io/pipeline/modules/braille/common-utils/src/main/java/org/daisy/pipeline/braille/common/XMLTransform.java"
            >XML transformer</a> to a node.</p>
        </p:documentation>
        <p:input port="source"/>
        <p:input port="parameters" kind="parameter" primary="false"/>
        <p:output port="result"/>
        <p:option name="query" required="true">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>The transformer query</p>
            </p:documentation>
        </p:option>
        <!--
            Implemented in ../java/org/daisy/pipeline/braille/common/calabash/impl/PxTransformStep.java
        -->
    </p:declare-step>

    <p:declare-step type="px:parse-query">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Parse a query string and convert it to a c:param-set document.</p>
        </p:documentation>
        <p:option name="query" required="true"/>
        <p:output port="result">
            <p:documentation xmlns="http://www.w3.org/1999/xhtml">
                <p>A c:param-set document.</p>
            </p:documentation>
        </p:output>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/common/calabash/impl/PxParseQueryStep.java
        -->
    </p:declare-step>

</p:library>
