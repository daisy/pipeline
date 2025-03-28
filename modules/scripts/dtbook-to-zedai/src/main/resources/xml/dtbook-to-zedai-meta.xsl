<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns="http://www.daisy.org/ns/z3998/authoring/"
                version="2.0" exclude-result-prefixes="dtb">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="//dtb:head">
        <xsl:for-each select="dtb:meta">
            <xsl:choose>
                <xsl:when test="@name = 'dc:Title'">
                    <meta property="dc:title" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Creator'">
                    <meta property="dc:creator" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Date'">
                    <meta property="dc:date" content="{@content}" xml:id="meta-dcdate"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Publisher'">
                    <meta property="dc:publisher" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Language'">
                    <meta property="dc:language" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Subject'">
                    <meta property="dc:subject" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Description'">
                    <meta property="dc:description" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Contributor'">
                    <meta property="dc:contributor" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Type'">
                    <meta property="dc:type" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Format'">
                    <meta property="dc:format" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Source'">
                    <meta property="dc:source" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Relation'">
                    <meta property="dc:relation" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Coverage'">
                    <meta property="dc:coverage" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dc:Rights'">
                    <meta property="dc:rights" content="{@content}"/>
                </xsl:when>
                <xsl:when test="@name = 'dtb:revisionDescription'">
                    <meta property="dc:description" content="{@content}"
                          about="#meta-dcdate"/>
                </xsl:when>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

    <!-- identity template which discards everything -->
    <xsl:template match="@*|node()">
        <xsl:apply-templates select="@*|node()"/>
    </xsl:template>

</xsl:stylesheet>
