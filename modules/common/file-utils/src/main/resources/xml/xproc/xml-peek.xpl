<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                type="px:file-xml-peek"
                exclude-inline-prefixes="#all">

    <p:option name="href" required="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>URI pointing to a file on disk.</p>
            <p>May also be an entry inside a ZIP file.</p>
        </p:documentation>
    </p:option>

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

    <p:group cx:pure="true">
        <p:identity>
            <p:input port="source" select="/*/c:root-element">
                <p:pipe port="result" step="parse"/>
            </p:input>
        </p:identity>
        <!--
            note that this may unescape references to entities that are declared in the doctype, which will result in an error
        -->
        <p:unescape-markup/>
        <p:filter select="/*/*"/>
    </p:group>
    <p:identity name="unescaped"/>

    <p:delete match="/*/c:root-element">
        <p:input port="source">
            <p:pipe port="result" step="parse"/>
        </p:input>
    </p:delete>
    <p:identity name="prolog"/>

</p:declare-step>
