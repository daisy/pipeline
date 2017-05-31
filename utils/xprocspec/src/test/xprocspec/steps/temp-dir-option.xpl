<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step name="main" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:ex="http://example.net/ns" type="ex:temp-dir-option" version="1.0">

    <p:output port="result" primary="true"/>

    <p:option name="temp-dir" select="'default temp-dir value'"/>
    <p:option name="output-dir" select="'default output-dir value'"/>

    <p:add-attribute match="/*" attribute-name="temp-dir">
        <p:input port="source">
            <p:inline exclude-inline-prefixes="#all">
                <c:result/>
            </p:inline>
        </p:input>
        <p:with-option name="attribute-value" select="$temp-dir"/>
    </p:add-attribute>

    <p:add-attribute match="/*" attribute-name="output-dir">
        <p:with-option name="attribute-value" select="$output-dir"/>
    </p:add-attribute>

</p:declare-step>
