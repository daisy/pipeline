<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:output indent="no"/>
    
    <xsl:include href="update-epub-prefixes.xsl"/>
    
    <xsl:template match="/*">
        <xsl:choose>
            <xsl:when test="local-name()='wrapper'">
                <!-- for xspec testing -->
                <xsl:apply-templates select="*[1]">
                    <xsl:with-param name="collection" select="* except *[1]" tunnel="yes"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy exclude-result-prefixes="#all">
                    <xsl:apply-templates select="@* | node()">
                        <xsl:with-param name="collection" select="collection()/*" tunnel="yes"/>
                    </xsl:apply-templates>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@* | node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="html:html">
        <xsl:param name="collection" tunnel="yes" as="element()*"/>
        <xsl:variable name="prefixes" select="distinct-values(for $document in ($collection) return f:prefixes($document/html:head, $document/html:body, ()))"/>
        
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:copy-of select="(. | $collection | $collection/*)/namespace::*" exclude-result-prefixes="#all"/>
            <xsl:if test="count($prefixes)">
                <xsl:attribute name="epub:prefix" select="string-join($prefixes, ' ')"/>
            </xsl:if>
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="html:section[@xml:base]">
        <xsl:param name="collection" tunnel="yes" as="element()*"/>
        <xsl:variable name="section" select="."/>
        <xsl:variable name="base-uri" select="base-uri()"/>
        <xsl:variable name="document" select="$collection[base-uri() = $base-uri]"/>

        <xsl:choose>
            <xsl:when test="not($document)">
                <xsl:message
                    select="concat('WARNING: the document ',replace($base-uri,'.*/',''),' is not available! There are ',count($collection),' documents in the collection: (',string-join($collection/base-uri(),', '),')')"/>

            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="$document/html:body">
                    <xsl:element name="{if (tokenize(@epub:type,'\s+')='article') then 'article' else 'section'}" namespace="http://www.w3.org/1999/xhtml">
                        <xsl:copy-of select="@*" exclude-result-prefixes="#all"/>

                        <xsl:attribute name="xml:base" select="$base-uri"/>

                        <xsl:variable name="lang" select="(@xml:lang,@lang,$document/@xml:lang,$document/@lang)[1]"/>
                        <xsl:if test="$lang">
                            <xsl:attribute name="xml:lang" select="$lang"/>
                            <xsl:attribute name="lang" select="$lang"/>
                        </xsl:if>

                        <xsl:if test="@epub:type and not($section/parent::html:body)">
                            <xsl:attribute name="epub:type" select="string-join(tokenize(@epub:type,'\s+')[not(.=('cover','frontmatter','bodymatter','backmatter'))],' ')"/>
                        </xsl:if>

                        <xsl:copy-of select="node()" exclude-result-prefixes="#all"/>

                        <xsl:for-each select="$section/node()">
                            <xsl:apply-templates select=".">
                                <xsl:with-param name="collection" select="$collection" tunnel="yes"/>
                            </xsl:apply-templates>
                        </xsl:for-each>
                    </xsl:element>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
