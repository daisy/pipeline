<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:err="http://www.w3.org/ns/xproc-error" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    type="px:mediaoverlay-upgrade-smil" version="1.0">

    <p:output port="result"/>
    <p:input port="source"/>

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
            <p:error code="err:PMU0001">
                <p:input port="source">
                    <p:inline>
                        <c:message>It is a dynamic error if the document arriving on the input port
                            is not a valid DAISY 2.02, DAISY 3 (DTBook) or EPUB3 Media Overlay
                            document.</c:message>
                    </p:inline>
                </p:input>
            </p:error>
        </p:otherwise>

    </p:choose>

</p:declare-step>
