<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:epub="http://www.idpf.org/2007/ops"
    xmlns:html="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <xsl:template match="noteref|dtb:noteref">
        <xsl:copy>
            <xsl:sequence select="@* except @class"/>
            <xsl:variable name="class" as="xs:string*">
                <xsl:sequence select="tokenize(@class,'\s+')[not(.='')]"/>
                <xsl:variable name="idref" as="xs:string" select="replace(@idref, '^#', '')"/>
                <xsl:variable name="note" as="element()" select="//(note|dtb:note)[@id=$idref]"/>
                <xsl:sequence select="tokenize($note/@class,'\s+')[.=('footnote','endnote')]"/>
            </xsl:variable>
            <xsl:if test="exists($class)">
                <xsl:attribute name="class" select="string-join($class,' ')"/>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="html:a[tokenize(@class, '\s+') = 'noteref']">
        <xsl:copy>
            <xsl:sequence select="@* except @class"/>
            <xsl:variable name="class" as="xs:string*">
                <xsl:sequence select="tokenize(@class,'\s+')[not(.='')]"/>
                <xsl:variable name="idref" as="xs:string" select="replace(@href, '^.*?#', '')"/>
                <xsl:variable name="note" as="element()" select="//(html:li|html:aside)[@id = $idref]"/>
                <xsl:sequence select="tokenize($note/@epub:type,'\s+')[.=('footnote','rearnote')]"/>
            </xsl:variable>
            <xsl:if test="exists($class)">
                <xsl:attribute name="class" select="string-join($class,' ')"/>
            </xsl:if>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="node()">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
