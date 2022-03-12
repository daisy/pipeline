<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                type="px:file-peek"
                exclude-inline-prefixes="#all">

    <p:option name="href" required="true">
        <p:documentation>URI to the file you want to read bytes from.</p:documentation>
    </p:option>
    <p:option name="offset" required="true">
        <p:documentation>Number of bytes to skip into the file.</p:documentation>
    </p:option>
    <p:option name="length" required="true">
        <p:documentation>Number of bytes to include in the result.</p:documentation>
    </p:option>
    <p:option name="use-base64" select="'false'">
        <p:documentation>By default, the output will be hex-ecoded, which is normally easier to manipulate. If you want base64-encoded output then you can set this to true.</p:documentation>
    </p:option>

    <p:output port="result"/>

    <p:declare-step type="pxi:file-peek">
        <p:option name="href" required="true"/>
        <p:option name="offset" required="true"/>
        <p:option name="length" required="true"/>
        <p:output port="result"/>
		<!-- Implemented in ../../../java/org/daisy/pipeline/file/calabash/impl/PeekProvider.java -->
    </p:declare-step>

    <pxi:file-peek>
      <p:with-option name="href" select="$href"/>
      <p:with-option name="offset" select="$offset"/>
      <p:with-option name="length" select="$length"/>
    </pxi:file-peek>
    <p:add-attribute match="/*" attribute-name="offset">
      <p:with-option name="attribute-value" select="$offset"/>
    </p:add-attribute>
    <p:add-attribute match="/*" attribute-name="length">
      <p:with-option name="attribute-value" select="$length"/>
    </p:add-attribute>

    <p:choose>
        <p:when test="$use-base64 = 'true'">
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:xslt>
                <p:with-param name="offset" select="$offset"/>
                <p:with-param name="length" select="$length"/>
                <p:input port="stylesheet">
                    <p:document href="../xslt/base64-to-hex.xsl"/>
                </p:input>
            </p:xslt>
        </p:otherwise>
    </p:choose>

</p:declare-step>
