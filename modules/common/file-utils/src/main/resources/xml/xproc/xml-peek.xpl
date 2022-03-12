<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="px:file-xml-peek"
                exclude-inline-prefixes="#all">

    <p:option name="href" required="true"/>

    <p:output port="result" primary="true">
        <p:pipe port="result" step="unescaped"/>
    </p:output>
    <p:output port="escaped">
        <p:pipe port="result" step="escaped"/>
    </p:output>
    <p:output port="prolog">
        <p:pipe port="result" step="prolog"/>
    </p:output>

    <p:declare-step type="pxi:file-xml-peek">
        <p:option name="href" required="true"/>
        <p:output port="result"/>
        <!-- Implemented in ../../../java/org/daisy/pipeline/file/calabash/impl/XMLPeekProvider.java -->
    </p:declare-step>

    <pxi:file-xml-peek>
      <p:with-option name="href" select="$href"/>
    </pxi:file-xml-peek>
    <p:identity name="escaped"/>

    <p:xslt>
        <p:input port="parameters">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:document href="../xslt/xml-peek.parse.xsl"/>
        </p:input>
    </p:xslt>
    <p:identity name="parse"/>

    <p:identity>
        <p:input port="source" select="/*/c:root-element">
            <p:pipe port="result" step="parse"/>
        </p:input>
    </p:identity>
    <p:unescape-markup/>
    <p:filter select="/*/*"/>
    <p:identity name="unescaped"/>

    <p:delete match="/*/c:root-element">
        <p:input port="source">
            <p:pipe port="result" step="parse"/>
        </p:input>
    </p:delete>
    <p:identity name="prolog"/>

</p:declare-step>
