<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:louis="http://liblouis.org/liblouis"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    xmlns:brl="http://www.daisy.org/ns/pipeline/braille"
    exclude-result-prefixes="xs louis brl css"
    version="2.0">
    
    <xsl:param name="louis:running-header"/>
    <xsl:param name="louis:running-footer"/>
    
    <xsl:template match="/*">
        <louis:semantics>
            <xsl:choose>
                <xsl:when test="/louis:toc">
                    <xsl:for-each select="distinct-values(//louis:toc-item/@louis:style/string())">
                        <xsl:value-of select="concat('heading', position())"/>
                        <xsl:text> &amp;xpath(//louis:toc-item[@louis:style='</xsl:text>
                        <xsl:value-of select="."/>
                        <xsl:text>'])&#xa;</xsl:text>
                    </xsl:for-each>
                    <xsl:text>&#xa;</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>namespaces </xsl:text>
                    <xsl:text>louis=http://liblouis.org/liblouis&#xa;</xsl:text>
                    <xsl:text>root &amp;xpath(/*)&#xa;</xsl:text>
                    <xsl:for-each select="distinct-values(//*/@louis:style/string())">
                        <xsl:if test="starts-with(., '#')">
                            <xsl:value-of select="substring-after(., '#')"/>
                            <xsl:text> &amp;xpath(//*[@louis:style='</xsl:text>
                            <xsl:value-of select="."/>
                            <xsl:text>'])&#xa;</xsl:text>
                        </xsl:if>
                    </xsl:for-each>
                    <xsl:sequence select="if ($louis:running-header='true') then 'runninghead' else 'skip'"/>
                    <xsl:text> &amp;xpath(//louis:running-header)&#xa;</xsl:text>
                    <xsl:sequence select="if ($louis:running-footer='true') then 'footer' else 'skip'"/>
                    <xsl:text> &amp;xpath(//louis:running-footer)&#xa;</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </louis:semantics>
    </xsl:template>
    
</xsl:stylesheet>
