<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                name="dtbook-to-mods-meta" type="px:dtbook-to-mods-meta"
                exclude-inline-prefixes="p px">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Generate a MODS metadata record from a DTBook 2005-3 document.</p>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a px:role="contact" href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <p:input port="source"/>
    <p:output port="result"/>
    <p:input port="parameters" kind="parameter"/>

    <p:option name="assert-valid" required="false" px:type="boolean" select="'true'">
        <p:documentation>
            Whether to stop processing and raise an error on validation issues.
        </p:documentation>
    </p:option>

    <p:import href="dtbook-utils-library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation>
            Collection of utilities for validation and reporting.
        </p:documentation>
    </p:import>
    <p:import href="http://www.daisy.org/pipeline/modules/metadata-utils/library.xpl">
        <p:documentation>
            For px:validate-mods
        </p:documentation>
    </p:import>

    <px:dtbook-validator.select-schema name="dtbook-schema" dtbook-version="2005-3" mathml-version="2.0"/>
    <px:validate-with-relax-ng-and-report name="validate-dtbook-input">
        <p:input port="source">
            <p:pipe port="source" step="dtbook-to-mods-meta"/>
        </p:input>
        <p:input port="schema">
            <p:pipe port="result" step="dtbook-schema"/>
        </p:input>
        <p:with-option name="assert-valid" select="$assert-valid"/>
    </px:validate-with-relax-ng-and-report>

    <p:xslt name="generate-mods">
        <p:input port="stylesheet">
            <p:document href="dtbook-to-mods-meta.xsl"/>
        </p:input>
    </p:xslt>

    <px:validate-mods/>

</p:declare-step>
