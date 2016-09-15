<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="px:epub3-store" xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:cx="http://xmlcalabash.com/ns/extensions"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" version="1.0" name="main">

    <p:input port="fileset.in" primary="true"/>
    <p:input port="in-memory.in" sequence="true"/>

    <p:output port="result" primary="false">
        <p:pipe port="result" step="zip"/>
    </p:output>

    <p:option name="href" required="true"/>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl"/>
    <p:import href="ocf-zip.xpl"/>

    <px:fileset-store name="fileset-store">
        <p:input port="fileset.in">
            <p:pipe port="fileset.in" step="main"/>
        </p:input>
        <p:input port="in-memory.in">
            <p:pipe port="in-memory.in" step="main"/>
        </p:input>
    </px:fileset-store>

    <px:epub3-ocf-zip name="zip" cx:depends-on="fileset-store">
        <p:with-option name="target" select="$href"/>
        <p:input port="source">
            <p:pipe port="fileset.out" step="fileset-store"/>
        </p:input>
    </px:epub3-ocf-zip>

</p:declare-step>
