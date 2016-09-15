<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:mo="http://www.w3.org/ns/SMIL" xmlns:epub="http://www.idpf.org/2007/ops" version="2.0"
    exclude-result-prefixes="#all">
    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:copy-of select="mo:head"/>
            <xsl:choose>
                <xsl:when
                    test="mo:body[not(@epub:textref) and not(@epub:type) and count(*)=1 and mo:seq]">
                    <body id="{mo:body/@id}" xmlns="http://www.w3.org/ns/SMIL">
                        <xsl:if test="mo:body/mo:seq/@epub:textref">
                            <xsl:attribute name="epub:textref" select="mo:body/mo:seq/@epub:textref"
                            />
                        </xsl:if>
                        <xsl:if test="mo:body/mo:seq/@epub:type">
                            <xsl:attribute name="epub:textref" select="mo:body/mo:seq/@epub:type"/>
                        </xsl:if>
                        <xsl:copy-of select="mo:body/mo:seq/*"/>
                    </body>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:copy-of select="mo:body"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
