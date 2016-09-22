<?xml version="1.0" encoding="UTF-8"?>
<p:library xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    exclude-inline-prefixes="#all" version="1.0">

    <p:declare-step type="px:html-load">
        <p:output port="result"/>
        <p:option name="href" required="true"/>

        <p:load>
            <p:with-option name="href" select="$href"/>
        </p:load>

    </p:declare-step>

</p:library>
