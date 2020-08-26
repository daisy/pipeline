<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                type="px:smil-downgrade" name="main">

    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input SMIL</p>
            <p>Must be a version 3.0 SMIL document</p>
        </p:documentation>
    </p:input>
    <p:input port="ncx" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>NCX file</p>
            <p>If the input SMIL is from a DAISY 3, an NCX file may optionally be passed. The
            <code>smilCustomTest</code> information of the NCX is used, and when text elements are
            missing from the input SMIL, references are made to NCX items.</p>
        </p:documentation>
        <p:empty/>
    </p:input>
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Output SMIL</p>
        </p:documentation>
    </p:output>
    <p:option name="version" select="'1.0'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Version of the output SMIL</p>
            <p>Only supported value is 1.0</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
            px:error
        </p:documentation>
    </p:import>

    <px:assert error-code="XXX" message="The output SMIL version must be 1.0">
        <p:with-option name="test" select="$version='1.0'"/>
    </px:assert>

    <p:choose>
        <p:when test="local-name(/*)='smil' and namespace-uri(/*)='http://www.w3.org/ns/SMIL' and /*/@version='3.0'">
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="smil3-to-smil1.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:when test="local-name(/*)='smil' and namespace-uri(/*)='http://www.w3.org/2001/SMIL20/'">
            <p:identity name="smil"/>
            <p:sink/>
            <p:xslt>
                <p:input port="source">
                    <p:pipe step="smil" port="result"/>
                    <p:pipe step="main" port="ncx"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:document href="smil2-to-smil1.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:otherwise>
            <px:error code="XXX" message="The input must be a DAISY 3 or EPUB 3 Media Overlay document."/>
        </p:otherwise>
    </p:choose>

</p:declare-step>
