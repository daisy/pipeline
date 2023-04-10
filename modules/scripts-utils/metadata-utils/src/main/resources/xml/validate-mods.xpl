<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:validate-mods"
                exclude-inline-prefixes="p px">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Validate a MODS document.</p>
        <dl px:role="author">
            <dt>Name:</dt>
            <dd px:role="name">Marisa DeMeglio</dd>
            <dt>E-mail:</dt>
            <dd><a href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a></dd>
            <dt>Organization:</dt>
            <dd px:role="organization">DAISY Consortium</dd>
        </dl>
    </p:documentation>

    <p:input port="source"/>
    <p:output port="result"/>

    <p:validate-with-xml-schema name="validate-mods-output" assert-valid="true">
        <p:input port="schema">
            <p:document href="schema/mods-3-3.xsd"/>
        </p:input>
    </p:validate-with-xml-schema>

</p:declare-step>
