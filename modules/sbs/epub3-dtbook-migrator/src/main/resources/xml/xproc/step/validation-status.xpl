<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/pipeline/xproc" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:cx="http://xmlcalabash.com/ns/extensions" type="px:nordic-validation-status" name="main" version="1.0">

    <!-- TODO: merge with px:validation-status -->

    <p:input port="source" sequence="true"/>
    <p:output port="result"/>

    <p:count/>
    <p:choose>
        <p:when test=".='0'">
            <p:identity>
                <p:input port="source">
                    <p:inline exclude-inline-prefixes="#all">
                        <d:validation-status result="ok"/>
                    </p:inline>
                </p:input>
            </p:identity>
        </p:when>
        <p:otherwise>
            <p:xslt>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
                <p:input port="source">
                    <p:pipe port="source" step="main"/>
                </p:input>
                <p:input port="stylesheet">
                    <p:inline>
                        <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="#all" xmlns:d="http://www.daisy.org/ns/pipeline/data">
                            <xsl:template match="/*">
                                <d:validation-status result="{if (collection()//d:error-count[text() and not(normalize-space(text())='0')]) then 'error' else 'ok'}"/>
                            </xsl:template>
                        </xsl:stylesheet>
                    </p:inline>
                </p:input>
            </p:xslt>
        </p:otherwise>
    </p:choose>

</p:declare-step>
