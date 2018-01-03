<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                exclude-inline-prefixes="#all"
                type="px:normalize-uri"
                name="main"
                version="1.0">
    
    <!-- step behaves similar to p:identity -->
    <p:input port="source" primary="true" sequence="true">
        <p:empty/>
    </p:input>
    <p:output port="result" primary="true" sequence="true">
        <p:pipe port="source" step="main"/>
    </p:output>
    
    <!-- the normalized URI is made available on a secondary port -->
    <p:output port="normalized" primary="false">
        <p:pipe port="result" step="normalized"/>
    </p:output>
    
    <!-- the href to normalize -->
    <p:option name="href" required="true"/>
    
    <p:sink/>
    <p:xslt>
        <p:with-param name="href" select="$href"/>
        <p:input port="source">
            <p:inline>
                <c:result/>
            </p:inline>
        </p:input>
        <p:input port="stylesheet">
            <p:inline>
                <xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                    version="2.0">
                    <xsl:import href="../xslt/uri-functions.xsl"/>
                    <xsl:param name="href" required="yes"/>
                    <xsl:template match="/*">
                        <xsl:copy>
                            <xsl:sequence select="pf:normalize-uri($href)"/>
                        </xsl:copy>
                    </xsl:template>
                </xsl:stylesheet>
            </p:inline>
        </p:input>
    </p:xslt>
    <p:identity name="normalized"/>
    
</p:declare-step>
