<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                type="css:repeat-string-set"
                exclude-inline-prefixes="p xsl"
                version="1.0">
    
    <p:documentation>
        Repeat 'string-set' declarations at the beginning of documents.
    </p:documentation>
    
    <p:input port="source" sequence="true">
        <p:documentation>
            The input is a sequence of one or more documents. 'string-set' properties must be
            declared in css:string-set attributes. Root elements of documents must not have
            css:string-set or css:string-entry attributes yet.
        </p:documentation>
    </p:input>
    
    <p:output port="result" sequence="true">
        <p:documentation>
            For each document in the input sequence and for each named string, the "entry" value of
            that named string (if not absent) is declared in a css:string-entry attribute on the
            document root element.
        </p:documentation>
    </p:output>
    
    <p:for-each px:progress=".45">
        <p:xslt>
            <p:input port="stylesheet">
                <p:inline>
                    <xsl:stylesheet version="2.0">
                        <xsl:include href="library.xsl"/>
                        <xsl:template match="@*|node()">
                            <xsl:copy>
                                <xsl:sequence select="@*"/>
                                <xsl:apply-templates select="@css:string-set"/>
                                <xsl:apply-templates/>
                            </xsl:copy>
                        </xsl:template>
                        <xsl:template match="@css:string-set">
                            <xsl:sequence select="css:parse-string-set(.)"/>
                        </xsl:template>
                    </xsl:stylesheet>
                </p:inline>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:for-each>
    
    <p:identity name="iteration-source"/>
    <p:for-each px:progress=".10">
        <p:identity name="current-section"/>
        <p:split-sequence>
            <p:input port="source">
                <p:pipe step="iteration-source" port="result"/>
            </p:input>
            <p:with-option name="test" select="concat('position()&lt;',p:iteration-position())"/>
        </p:split-sequence>
        <p:wrap-sequence wrapper="_" name="preceding-sections"/>
        <p:insert match="/*" position="first-child">
            <p:input port="source">
                <p:pipe step="current-section" port="result"/>
            </p:input>
            <p:input port="insertion"
                     select="for $n in distinct-values(//css:string-set/@name)
                             return (//css:string-set[@name=$n])[last()]">
                <p:pipe step="preceding-sections" port="result"/>
            </p:input>
        </p:insert>
    </p:for-each>
    
    <p:for-each px:progress=".45">
        <p:xslt>
            <p:input port="stylesheet">
                <p:inline>
                    <xsl:stylesheet version="2.0">
                        <xsl:include href="library.xsl"/>
                        <xsl:template match="@*|node()">
                            <xsl:copy>
                                <xsl:apply-templates select="@*|node()"/>
                            </xsl:copy>
                        </xsl:template>
                        <xsl:template match="/*">
                            <xsl:copy>
                                <xsl:if test="child::css:string-set">
                                    <xsl:attribute name="css:string-entry"
                                                   select="css:serialize-string-set(child::css:string-set)"/>
                                </xsl:if>
                                <xsl:apply-templates select="@*|node()"/>
                            </xsl:copy>
                        </xsl:template>
                        <xsl:template match="css:string-set"/>
                    </xsl:stylesheet>
                </p:inline>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
    </p:for-each>
    
</p:declare-step>
