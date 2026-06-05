<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:err="http://www.w3.org/ns/xproc-error"
                type="px:smil-upgrade">

    <p:input port="source">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Input SMIL</p>
            <p>Must be a version 1.0, 2.0 or 3.0 SMIL document</p>
        </p:documentation>
    </p:input>
    <p:output port="result">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Output SMIL</p>
        </p:documentation>
    </p:output>
    <p:option name="version" select="'3.0'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>Version of the output SMIL</p>
            <p>Only supported value is 3.0</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
            px:error
        </p:documentation>
    </p:import>

    <px:assert error-code="XXX" message="The output SMIL version must be 3.0">
        <p:with-option name="test" select="$version='3.0'"/>
    </px:assert>

    <p:choose>
        <p:when test="local-name(/*)='smil' and namespace-uri(/*)='http://www.w3.org/ns/SMIL' and /*/@version='3.0'">
            <!-- EPUB3 Media Overlay; pass right through. -->
            <p:identity/>
        </p:when>
        <p:when test="local-name(/*)='smil' and namespace-uri(/*)='http://www.w3.org/2001/SMIL20/'">
            <!-- DTBook -->
            <!-- TODO: validate SMIL 2.0 here (dtbsmil-2005-2.dtd) -->
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="upgrade-smil-dtbook.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:when test="local-name(/*)='smil'">
            <!-- DAISY 2.02 -->
            <!-- TODO: validate SMIL 1.0 here (SMIL10.dtd) -->
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="upgrade-smil-daisy202.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:otherwise>
            <px:error code="err:PMU0001"
                      message="It is a dynamic error if the document arriving on the input port
                            is not a valid DAISY 2.02, DAISY 3 (DTBook) or EPUB3 Media Overlay
                            document."/>
        </p:otherwise>

    </p:choose>

</p:declare-step>
