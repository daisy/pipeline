<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:param name="identifier-id" as="xs:string" required="true"/>

    <xsl:template match="/*">
        <metadata>
            <xsl:namespace name="dc" select="'http://purl.org/dc/elements/1.1/'"/>
            
            <xsl:variable name="identifier"
                          select="//html:head/html:meta[lower-case(@name)=('dc:identifier',
                                                                           'dct:identifier',
                                                                           'dcterms:identifier',
                                                                           'dtb:uid')][1]"/>
            <dc:identifier id="{$identifier-id}">
                <xsl:copy-of select="$identifier/@scheme" exclude-result-prefixes="#all"/>
                <xsl:value-of select="($identifier/@content,replace(replace(string(current-dateTime()),'\+.*',''),'[^\d]',''))[1]"/>
            </dc:identifier>
            
            <xsl:if test="not(/html:html/html:head/html:meta[lower-case(@name)='dc:language'])">
                <dc:language>
                    <xsl:value-of select="(/*/@lang,/*/@xml:lang,'zxx')[1]"/>
                </dc:language>
            </xsl:if>

            <dc:format id="format">EPUB3</dc:format>

            <xsl:if test="not(/html:html/html:head/html:meta[lower-case(@name)='dc:title'])">
                <dc:title>
                    <xsl:value-of select="(/html:html/html:head/html:title/text()[normalize-space()],
                                           /html:html/html:body//html:h1/text()[normalize-space()][1],
                                           replace(base-uri(/),'.*?([^/]+)\.[^/.]+$','$1')
                                          )[1]"/>
                </dc:title>
            </xsl:if>
            
            <xsl:for-each select="/html:html/html:head/html:meta">
                <xsl:variable name="lcname" select="lower-case(@name)"/>
                <xsl:variable name="id"
                    select="if (matches($lcname,'^dc:\w+$'))
                            then concat(replace($lcname,'^dc:',''),'_',count(preceding-sibling::html:meta[lower-case(@name)=$lcname])+1)
                            else if (matches($lcname,'^dtb:\w+$'))
                            then concat(replace($lcname,'dtb:','dtb-'),'_',count(preceding-sibling::html:meta[lower-case(@name)=$lcname]))
                            else concat('meta_',count(preceding-sibling::html:meta)+1)"/>
                <xsl:choose>
                    <xsl:when test="@http-equiv">
                        <xsl:message select="'Dropping empty meta element with http-equiv'"/>
                    </xsl:when>
                    <xsl:when test="string-length(normalize-space(@content)) = 0">
                        <xsl:message select="concat('Dropping empty meta element: ',@name)"/>
                    </xsl:when>
                    <xsl:when test="$lcname=('dc:identifier','dct:identifier','dcterms:identifier','dtb:uid')"/>
                    <xsl:when test="$lcname=('dcterms:modified','dc:format')">
                        <xsl:message select="concat('Discarding pre-existing meta element (it will be replaced with a new one): ',@name)"/>
                    </xsl:when>
                    <xsl:when test="matches($lcname,'^dc:')">
                        <xsl:element name="{$lcname}">
                            <xsl:attribute name="id" select="$id"/>
                            <xsl:copy-of select="@scheme" exclude-result-prefixes="#all"/>
                            <xsl:value-of select="@content"/>
                        </xsl:element>
                        <xsl:if test="@role">
                            <meta refines="#{$id}" property="role" scheme="marc:relators">
                                <xsl:value-of select="@role"/>
                            </meta>
                        </xsl:if>
                    </xsl:when>
                    <xsl:when test="matches($lcname,'^dct:') or matches($lcname,'^dcterms:')">
                        <meta property="{replace($lcname,'^dct:','dcterms:')}">
                            <xsl:attribute name="id" select="$id"/>
                            <xsl:copy-of select="@scheme" exclude-result-prefixes="#all"/>
                            <xsl:value-of select="@content"/>
                        </meta>
                    </xsl:when>
                    <xsl:when test="@name='dtb:narrator'">
                        <dc:contributor id="{$id}">
                            <xsl:attribute name="id" select="$id"/>
                            <xsl:copy-of select="@scheme" exclude-result-prefixes="#all"/>
                            <xsl:value-of select="@content"/>
                        </dc:contributor>
                        <meta refines="#{$id}" property="role" scheme="marc:relators">nrt</meta>
                    </xsl:when>
                    <xsl:when test="@name='dtb:producer'">
                        <dc:contributor id="{$id}">
                            <xsl:attribute name="id" select="$id"/>
                            <xsl:copy-of select="@scheme" exclude-result-prefixes="#all"/>
                            <xsl:value-of select="@content"/>
                        </dc:contributor>
                        <meta refines="#{$id}" property="role" scheme="marc:relators">pro</meta>
                    </xsl:when>
                    <xsl:otherwise>
                        <meta property="{@name}">
                            <xsl:attribute name="id" select="$id"/>
                            <xsl:copy-of select="@scheme" exclude-result-prefixes="#all"/>
                            <xsl:value-of select="@content"/>
                        </meta>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>

            <meta property="dcterms:modified">
                <xsl:value-of
                    select="format-dateTime(
                    adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
                    '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][Z]')"
                />
            </meta>
        </metadata>
    </xsl:template>
    
</xsl:stylesheet>
