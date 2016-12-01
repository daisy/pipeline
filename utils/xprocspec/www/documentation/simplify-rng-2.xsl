<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:x="http://www.daisy.org/ns/pipeline/xproc/test" xmlns:rng="http://relaxng.org/ns/structure/1.0" xmlns="http://www.w3.org/1999/xhtml"
    xmlns:a="http://relaxng.org/ns/compatibility/annotations/1.0" version="2.0">

    <xsl:output indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="rng:attribute">
        <xsl:param name="ancestors" tunnel="yes" as="node()*"/>
        <xsl:choose>
            <xsl:when test="count(ancestor::rng:element) &gt; 1"/>
            <xsl:when test="not(@name)">
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy>
                    <xsl:apply-templates select="@*"/>
                    <xsl:copy-of select="*[not(self::a:documentation) and not(self::rng:value) and not(self::rng:data) and not(self::rng:choice)]"/>
                    <xsl:variable name="name" select="@name"/>
                    <xsl:variable name="siblings" select="ancestor::rng:element//rng:attribute[@name=$name and count(ancestor::rng:element)=1]"/>
                    <xsl:variable name="documentation" select="$siblings/a:documentation"/>
                    <xsl:variable name="values" select="distinct-values($siblings//rng:value/text())"/>
                    <xsl:variable name="data" select="distinct-values($siblings//rng:data/@type)"/>
                    <a:documentation xmlns="http://www.w3.org/1999/xhtml">
                        <xsl:for-each select="$documentation">
                            <xsl:variable name="position" select="position()"/>
                            <xsl:if test="not($documentation[position()&lt;$position]/@xml:id=@xml:id)">
                                <xsl:for-each select="node()">
                                    <xsl:choose>
                                        <xsl:when test="self::text() and not(matches(.,'[^\s]'))"/>
                                        <xsl:when test="self::text()">
                                            <p><![CDATA[]]><xsl:apply-templates select="."/><![CDATA[]]></p>
                                        </xsl:when>
                                        <xsl:otherwise><![CDATA[]]><xsl:apply-templates select="."/><![CDATA[]]></xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </xsl:if>
                        </xsl:for-each>
                    </a:documentation>
                    <xsl:choose>
                        <xsl:when test="count(($values,$data)) &gt; 1">
                            <choose>
                                <xsl:call-template name="attribute-choice">
                                    <xsl:with-param name="values" select="$values"/>
                                    <xsl:with-param name="data" select="$data"/>
                                </xsl:call-template>
                            </choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="attribute-choice">
                                <xsl:with-param name="values" select="$values"/>
                                <xsl:with-param name="data" select="$data"/>
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="attribute-choice">
        <xsl:param name="values" required="yes"/>
        <xsl:param name="data" required="yes"/>
        <xsl:for-each select="$values">
            <value xmlns="http://relaxng.org/ns/structure/1.0"><![CDATA[]]><xsl:value-of select="."/><![CDATA[]]></value>
        </xsl:for-each>
        <xsl:for-each select="$data">
            <data type="{.}"/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="a:documentation">
        <xsl:if test="not(preceding-sibling::a:documentation/@xml:id=@xml:id)">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()" priority="10">
        <xsl:if test="matches(.,'[^\s]')">
            <xsl:copy-of select="."/>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
