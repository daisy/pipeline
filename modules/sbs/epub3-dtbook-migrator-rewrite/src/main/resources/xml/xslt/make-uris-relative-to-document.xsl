<xsl:stylesheet xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    xmlns="http://www.w3.org/1999/xhtml" xpath-default-namespace="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all">

    <xsl:variable name="doc-base" select="if (/html/head/base[@href][1]) then resolve-uri(normalize-space(/html/head/base[@href][1]/@href),base-uri(/*)) else base-uri(/*)"/>

    <xsl:template match="/*|@*|node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/*//*/@xml:base"/>

    <xsl:template match="@href|@src|@xlink:href|@altimg|@data[parent::object]">
        <xsl:choose>
            <xsl:when test="matches(.,'^[^/]+\.x?html#.+$')">
                <xsl:attribute name="{name()}" select="concat('#',substring-after(.,'#'))"/>
            </xsl:when>
            <xsl:when test="matches(.,'^[^/]+\.x?html#?$')">
                <xsl:variable name="target-href" select="string(resolve-uri(replace(., '#', ''), base-uri(.)))" as="xs:string"/>
                <xsl:variable name="target-id" select="(//*[@xml:base=$target-href]//@id)[1]" as="xs:string?"/>
                
                <xsl:if test="not($target-id)">
                    <xsl:message select="concat('Warning: could not infer fragment identifier for internal link: ', .)"/>
                </xsl:if>
                
                <xsl:attribute name="{name()}" select="concat('#', $target-id)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="." exclude-result-prefixes="#all"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
