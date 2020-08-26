<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml">

    <xsl:variable name="untitled-section-titles" as="element()*" select="document('untitled-section-titles.xml')/*/*"/>

    <xsl:template name="get-untitled-section-title">
        <xsl:param name="sectioning-element" as="element()" required="yes"/>
        <xsl:variable name="section-element-name" select="local-name($sectioning-element)"/>
        <xsl:variable name="types" as="xs:string*"
                      select="tokenize($sectioning-element/@epub:type,'\s+')[not(.='')]"/>
        <xsl:variable name="title" as="text()?"
                      select="($untitled-section-titles[contains(@usage,$section-element-name)]
                                                       [@epub:type=$types]
                               /text()[not(.='')])[1]"/>
        <xsl:choose>
            <xsl:when test="exists($title)">
                <xsl:sequence select="$title"/>
            </xsl:when>
            <xsl:when test="$sectioning-element[self::body]">
                <xsl:sequence select="'Untitled document'"/>
            </xsl:when>
            <xsl:when test="$sectioning-element[self::article]">
                <xsl:sequence select="'Article'"/>
            </xsl:when>
            <xsl:when test="$sectioning-element[self::aside]">
                <xsl:sequence select="'Sidebar'"/>
            </xsl:when>
            <xsl:when test="$sectioning-element[self::nav]">
                <xsl:sequence select="'Navigation'"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="'Untitled section'"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
