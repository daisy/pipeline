<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                name="dtbook-to-mods-meta" type="px:dtbook-to-mods-meta"
                exclude-inline-prefixes="p px">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Generate a MODS metadata record from a DTBook 2005-3 document.</p>
        <address>
            Authors:
            <dl px:role="author">
                <dt>Name:</dt>
                <dd px:role="name">Marisa DeMeglio</dd>
                <dt>E-mail:</dt>
                <dd><a href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a></dd>
                <dt>Organization:</dt>
                <dd px:role="organization">DAISY Consortium</dd>
            </dl>
        </address>
    </p:documentation>

    <p:input port="source"/>
    <p:output port="result"/>

    <p:xslt>
        <p:input port="stylesheet">
            <p:document href="dtbook-to-mods-meta.xsl"/>
        </p:input>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
    </p:xslt>

</p:declare-step>
