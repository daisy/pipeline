<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    xmlns:louis="http://liblouis.org/liblouis"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[contains(string(@style), 'string-set')]">
        <xsl:variable name="this" as="element()" select="."/>
        <xsl:variable name="properties"
            select="css:specified-properties('#all string-set display', true(), true(), true(), .)"/>
        <xsl:variable name="string-set" as="xs:string" select="$properties[@name='string-set']/@value"/>
        <xsl:if test="$string-set!='none'">
            <xsl:for-each select="tokenize($string-set, ',')">
                <xsl:variable name="identifier" select="replace(., '^\s*(\S+)\s.*$', '$1')"/>
                <xsl:variable name="content-list" select="substring-after(., $identifier)"/>
                <xsl:variable name="evaluated-content" as="node()*">
                    <xsl:apply-templates select="css:parse-content-list($content-list, $this)" mode="eval-content-list">
                        <xsl:with-param name="context" select="$this"/>
                    </xsl:apply-templates>
                </xsl:variable>
                <xsl:if test="exists($evaluated-content)">
                    <xsl:choose>
                        <xsl:when test="$identifier='print-page'">
                            <xsl:element name="louis:print-page">
                                <xsl:attribute name="break"
                                               select="if ($properties[@name='display' and @value='-louis-page-break'])
                                                       then 'true' else 'false'"/>
                                <xsl:sequence select="$evaluated-content"/>
                            </xsl:element>
                        </xsl:when>
                        <xsl:when test="$identifier='running-header'">
                            <xsl:element name="louis:running-header">
                                <xsl:sequence select="$evaluated-content"/>
                            </xsl:element>
                        </xsl:when>
                        <xsl:when test="$identifier='running-footer'">
                            <xsl:element name="louis:running-footer">
                                <xsl:sequence select="$evaluated-content"/>
                            </xsl:element>
                        </xsl:when>
                    </xsl:choose>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>
        <xsl:copy>
            <xsl:sequence select="@*[not(name()='style')]"/>
            <xsl:sequence select="css:style-attribute(css:serialize-declaration-list($properties[not(@name='string-set')]))"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="css:string[@value]" mode="eval-content-list">
        <xsl:value-of select="string(@value)"/>
    </xsl:template>
    
    <xsl:template match="css:attr" mode="eval-content-list">
        <xsl:param name="context" as="element()"/>
        <xsl:variable name="name" select="string(@name)"/>
        <xsl:value-of select="string($context/@*[name()=$name])"/>
    </xsl:template>
    
    <xsl:template match="css:content[not(@target)]" mode="eval-content-list">
        <xsl:param name="context" as="element()"/>
        <xsl:sequence select="$context/child::node()"/>
    </xsl:template>
    
</xsl:stylesheet>
