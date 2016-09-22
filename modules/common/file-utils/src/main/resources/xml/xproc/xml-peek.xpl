<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" type="px:file-xml-peek" version="1.0"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal" xmlns:cx="http://xmlcalabash.com/ns/extensions" exclude-inline-prefixes="#all">

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
    <p:option name="use-java-implementation" select="'true'">
        <p:documentation>If the Java implementation of this step is available but you don't want to use it; set this to false (default 'true').</p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:declare-step type="pxi:file-xml-peek">
        <p:option name="href" required="true"/>
        <p:output port="result"/>
    </p:declare-step>

    <p:choose name="choose">
        <p:when test="p:step-available('pxi:file-xml-peek') and $use-java-implementation = 'true'">
            <pxi:file-xml-peek>
                <p:with-option name="href" select="$href"/>
            </pxi:file-xml-peek>

        </p:when>
        <p:otherwise>
            <px:message name="message" severity="WARN"
                message="pxi:file-xml-peek is not available; will read and parse entire file using XProc and XSLT which might cause performance issues for large files: $1">
                <p:with-option name="param1" select="$href"/>
            </px:message>
            <p:add-attribute match="/*" attribute-name="href">
                <p:with-option name="attribute-value" select="$href"/>
                <p:input port="source">
                    <p:inline exclude-inline-prefixes="#all">
                        <c:request method="GET" override-content-type="text/plain; charset=utf-8"/>
                    </p:inline>
                </p:input>
            </p:add-attribute>
            <p:http-request/>
            <p:xslt>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="../xslt/xml-peek.strip-content.xsl"/>
                </p:input>
            </p:xslt>
        </p:otherwise>
    </p:choose>
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
