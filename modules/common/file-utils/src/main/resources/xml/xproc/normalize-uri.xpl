<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                exclude-inline-prefixes="#all"
                type="px:normalize-uri"
                name="main">

    <!-- step behaves similar to p:identity -->
    <p:input port="source" primary="true" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="result" primary="true" sequence="true">
        <p:pipe port="source" step="main"/>
    </p:output>

    <!-- the normalized URI is made available on a secondary port -->
    <p:output port="normalized" primary="false">
        <p:pipe port="result" step="normalized"/>
    </p:output>

    <!-- the href to normalize -->
    <p:option name="href" required="true"/>

    <p:declare-step type="pxi:normalize-uri">
        <p:option name="uri" required="true"/>
        <p:output port="result"/>
        <!-- Implemented in ../../../java/org/daisy/pipeline/file/calabash/impl/NormalizeURIProvider.java -->
    </p:declare-step>

    <p:sink/>
    <pxi:normalize-uri name="normalized">
        <p:with-option name="uri" select="$href"/>
    </pxi:normalize-uri>

</p:declare-step>
