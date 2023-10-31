<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                exclude-inline-prefixes="#all"
                type="px:fileset-compose">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Return composition of mapping documents.</p>
    </p:documentation>

    <p:input port="source" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The input mapping documents (one or more).</p>
            <p>A mapping document is a <code>d:fileset</code> document that maps files from
            <code>@original-href</code> to <code>@href</code> and anchors from
            <code>@original-id</code> to <code>@id</code>.</p>
        </p:documentation>
    </p:input>

    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The composed mapping document.</p>
            <p>The mappings are applied in the order in which they appear on the source port.</p>
        </p:documentation>
    </p:output>

    <p:option name="limit-scope" cx:as="xs:boolean" select="false()">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Limit the scope of the result mapping document to the files and anchors contained in
            the first input document.</p>
            <p>If this option is not set, all input documents contribute to the scope of the result
            mapping document.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>

    <p:declare-step type="pxi:fileset-compose-recursively">
        <p:input port="source" sequence="true"/>
        <p:output port="result"/>
        <p:option name="limit-scope" required="true"/>
        <p:identity name="source"/>
        <p:count/>
        <p:choose>
            <p:when test="/*=1">
                <p:sink/>
                <p:identity>
                    <p:input port="source">
                        <p:pipe step="source" port="result"/>
                    </p:input>
                </p:identity>
            </p:when>
            <p:otherwise>
                <p:split-sequence test="position()&lt;=2" name="first-two">
                    <p:input port="source">
                        <p:pipe step="source" port="result"/>
                    </p:input>
                </p:split-sequence>
                <p:xslt name="first-two-composed">
                    <p:input port="stylesheet">
                        <p:document href="../xslt/fileset-compose.xsl"/>
                    </p:input>
                    <p:with-param name="limit-scope" select="$limit-scope">
                        <p:empty/>
                    </p:with-param>
                </p:xslt>
                <p:sink/>
                <pxi:fileset-compose-recursively>
                    <p:input port="source">
                        <p:pipe step="first-two-composed" port="result"/>
                        <p:pipe step="first-two" port="not-matched"/>
                    </p:input>
                    <p:with-option name="limit-scope" select="$limit-scope"/>
                </pxi:fileset-compose-recursively>
            </p:otherwise>
        </p:choose>
    </p:declare-step>

    <px:assert test-count-min="1" message="At least one input document expected" error-code="XXX"/>

    <pxi:fileset-compose-recursively>
        <p:with-option name="limit-scope" select="$limit-scope"/>
    </pxi:fileset-compose-recursively>

    <p:delete match="d:file[not(d:anchor|@original-href)]"/>

</p:declare-step>
