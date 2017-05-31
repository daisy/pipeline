<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" exclude-result-prefixes="#all" xmlns:xfi="http://www.daisy.org/ns/xprocspec/xslt-internal/">

    <xsl:output indent="yes"/>

    <xsl:param name="temp-dir" required="yes"/>
    <xsl:param name="test-base-uri" required="yes"/>

    <xsl:template match="/*">
        <p:declare-step xmlns:p="http://www.w3.org/ns/xproc" name="main" xmlns:cx="http://xmlcalabash.com/ns/extensions" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:px="http://www.daisy.org/ns/xprocspec"
            xmlns:pxi="http://www.daisy.org/ns/xprocspec/xproc-internal/" exclude-inline-prefixes="#all" version="1.0" xpath-version="2.0" xmlns:x="http://www.daisy.org/ns/xprocspec">

            <p:input port="multiplexed-source" primary="true"/>
            <p:output port="result" primary="true"/>

            <p:import href="{resolve-uri(@href,base-uri())}"/>
            
            <p:identity>
                <p:input port="source">
                    <p:pipe port="multiplexed-source" step="main"/>
                </p:input>
            </p:identity>
            <p:split-sequence test="/x:document[@port='context']"/>
            <p:for-each name="context">
                <p:output port="result"/>
                <p:iteration-source select="/*/*"/>
                <p:identity/>
            </p:for-each>
            
            <p:identity>
                <p:input port="source">
                    <p:pipe port="multiplexed-source" step="main"/>
                </p:input>
            </p:identity>
            <p:split-sequence test="/x:document[@port='expect']"/>
            <p:for-each name="expect">
                <p:output port="result"/>
                <p:iteration-source select="/*/*"/>
                <p:identity/>
            </p:for-each>

            <p:parameters name="parameters">
                <p:input port="parameters">
                    <p:inline>
                        <c:param-set>
                            <c:param name="temp-dir" namespace="http://www.daisy.org/ns/xprocspec" value="{$temp-dir}"/>
                            <c:param name="test-base-uri" namespace="http://www.daisy.org/ns/xprocspec" value="{$test-base-uri}"/>
                            <xsl:for-each select="@*">
                                <c:param name="{local-name()}" namespace="{namespace-uri()}" value="{string(.)}"/>
                            </xsl:for-each>
                        </c:param-set>
                    </p:inline>
                </p:input>
            </p:parameters>

            <xsl:element name="{tokenize(@step,':')[last()]}" namespace="{if (contains(@step,':')) then namespace-uri-for-prefix(tokenize(@step,':')[1],.) else namespace-uri()}">
                <p:input port="context">
                    <p:pipe port="result" step="context"/>
                </p:input>
                <p:input port="expect">
                    <p:pipe port="result" step="expect"/>
                </p:input>
                <p:input port="parameters">
                    <p:pipe port="result" step="parameters"/>
                </p:input>
            </xsl:element>

        </p:declare-step>
    </xsl:template>

    <!--<xsl:function name="xfi:string-to-xpath">
        <xsl:param name="string"/>
        <xsl:variable name="string" select="replace($string,'&quot;','&amp;quot;')"/>
        <xsl:variable name="string" select="string-join(tokenize($string,&quot;'&quot;), concat(&quot;',&quot;,'&quot;',&quot;'&quot;,'&quot;',&quot;,'&quot;))"/>
        <xsl:variable name="string" select="concat(&quot;concat('','&quot;, $string, &quot;')&quot;)"/>
        <xsl:sequence select="$string"/>
    </xsl:function>-->

</xsl:stylesheet>
