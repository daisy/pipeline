<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <xsl:template
        match="table[tokenize(@class, '\s+') = 'render_by_both']|dtb:table[tokenize(@class, '\s+') = 'render_by_both']|html:table[tokenize(@class, '\s+') = 'render_by_both']">
        <xsl:variable name="caption" as="element()?"
            select="caption|dtb:caption|html:caption"/>
        <xsl:if test="$caption">
            <xsl:element name="div" namespace="{namespace-uri(.)}">
                <xsl:copy-of select="$caption/@* except @class"/>
                <xsl:attribute name="class"
                    select="'table_caption', $caption/@class"/>
                <xsl:apply-templates
                    select="$caption/node()"/>
            </xsl:element>
        </xsl:if>
        <xsl:element name="p" namespace="{namespace-uri(.)}">
            ((tabellen vist rækkevis))
        </xsl:element>
        <xsl:copy>
            <xsl:copy-of select="@* except @class"/>
            <xsl:attribute name="class"
                select="'render_by_row', tokenize(@class, '\s+')[. ne
                'render_by_both']"/>
            <xsl:apply-templates mode="DISCARD_TABLE_CAPTION"/>
        </xsl:copy>
        <xsl:element name="p" namespace="{namespace-uri(.)}">
            ((tabellen vist kolonnevis))
        </xsl:element>
        <xsl:copy>
            <xsl:copy-of select="@* except (@class|@id)"/>
            <xsl:attribute name="class"
                select="'render_by_column', tokenize(@class, '\s+')[. ne
                'render_by_both']"/>
            <xsl:apply-templates mode="DISCARD_TABLE_CAPTION"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template mode="DISCARD_TABLE_CAPTION"
        match="table/caption|dtb:table/dtb:caption|html:table/html:caption"/>
    
    <xsl:template mode="#default DISCARD_TABLE_CAPTION" match="node()|@*">
        <xsl:copy>
            <xsl:apply-templates select="node()|@*"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
