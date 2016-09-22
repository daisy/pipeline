<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" version="1.0" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:err="http://www.w3.org/ns/xproc-error">

    <!-- px:fileset-add-entry / px:fileset-create / px:fileset-join -->

    <p:declare-step type="px:fileset-create">
        <p:output port="result"/>
        <p:option name="base" required="false"/>

        <p:identity>
            <p:input port="source">
                <p:inline exclude-inline-prefixes="px">
                    <d:fileset/>
                </p:inline>
            </p:input>
        </p:identity>
        <p:choose>
            <p:when test="p:value-available('base')">
                <p:add-attribute match="/*" attribute-name="xml:base">
                    <p:with-option name="attribute-value" select="$base"/>
                </p:add-attribute>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
    </p:declare-step>

    <p:declare-step type="px:fileset-join">
        <p:input port="source" sequence="true"/>
        <p:output port="result" primary="true"/>

        <p:wrap-sequence wrapper="d:fileset"/>
        <p:choose>
            <p:when test="/*/*/@xml:base">
                <p:add-attribute match="/*" attribute-name="xml:base">
                    <p:with-option name="attribute-value" select="(/*/*/@xml:base)[1]"/>
                </p:add-attribute>
            </p:when>
            <p:otherwise>
                <p:identity/>
            </p:otherwise>
        </p:choose>
        <p:unwrap match="/*/*"/>
    </p:declare-step>

    <p:declare-step version="1.0" type="px:fileset-add-entry">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:option name="href" required="true"/>

        <p:insert match="/*" position="last-child">
            <p:input port="insertion">
                <p:inline>
                    <d:file/>
                </p:inline>
            </p:input>
        </p:insert>
        <p:add-attribute match="/*/*[last()]" attribute-name="href">
            <p:with-option name="attribute-value" select="$href"/>
        </p:add-attribute>
    </p:declare-step>

</p:library>
