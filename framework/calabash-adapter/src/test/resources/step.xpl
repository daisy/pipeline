<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:cx="http://xmlcalabash.com/ns/extensions"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <p:option name="option-1" cx:as="xs:string*" required="true"/>
    <p:output port="result"/>

    <p:xslt template-name="main">
        <p:input port="source">
            <p:empty/>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet version="2.0" exclude-result-prefixes="#all">
                    <xsl:param name="strings" as="xs:string*"/>
                    <xsl:template name="main">
                        <result>
                            <xsl:value-of select="string-join($strings,'///')"/>
                        </result>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
        <p:with-param name="strings" select="$option-1"/>
    </p:xslt>

</p:declare-step>

