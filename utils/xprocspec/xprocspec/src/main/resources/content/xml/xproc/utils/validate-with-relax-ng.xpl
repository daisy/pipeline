<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" type="pxi:validate-with-relax-ng" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/"
    exclude-inline-prefixes="#all" version="1.0" xpath-version="2.0" name="main">

    <p:input port="source" primary="true"/>
    <p:input port="schema"/>
    <p:output port="result"/>
    <p:option name="dtd-attribute-values" select="'false'"/>
    <p:option name="dtd-id-idref-warnings" select="'false'"/>
    <p:option name="assert-valid" select="'true'"/>

    <p:option name="step-available" select="'false'"/>

    <p:choose>
        <p:when test="$step-available='true'">
            <p:validate-with-relax-ng>
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
                <p:input port="schema">
                    <p:pipe port="schema" step="main"/>
                </p:input>
                <p:with-option name="dtd-attribute-values" select="$dtd-attribute-values"/>
                <p:with-option name="dtd-id-idref-warnings" select="$dtd-id-idref-warnings"/>
                <p:with-option name="assert-valid" select="$assert-valid"/>
            </p:validate-with-relax-ng>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>


</p:declare-step>
