<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:param name="pub-id" required="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/*">
        <metadata>
            <dc:identifier id="pub-id">
                <xsl:value-of select="$pub-id"/>
            </dc:identifier>
            <dc:title id="title">
                <xsl:value-of select="//html:head/html:meta[@name='dc:title']/@content"/>
            </dc:title>
            <dc:language>
                <xsl:value-of select="//html:head/html:meta[@name='dc:language']/@content"/>
            </dc:language>
            <dc:date id="date">
                <xsl:value-of select="//html:head/html:meta[@name='dc:date']/@content"/>
            </dc:date>
            <meta property="dcterms:modified">
                <xsl:value-of select="format-dateTime(
                    adjust-dateTime-to-timezone(current-dateTime(),xs:dayTimeDuration('PT0H')),
                    '[Y0001]-[M01]-[D01]T[H01]:[m01]:[s01][Z]')"/>
            </meta>
            <dc:format>EPUB3</dc:format>
            <xsl:for-each select="//html:head/html:meta">
                <xsl:if test="not(@name=('dc:identifier','dc:title','dc:language','dcterms:modified','dc:format','dc:date','base','dtb:uid'))">
                    <xsl:choose>
                        <xsl:when test="@http-equiv"/>
                        <xsl:when test="string-length(normalize-space(@content)) = 0"/>
                        <xsl:when test="starts-with(@name,'dc:')">
                            <xsl:element name="{@name}">
                                <xsl:if test="@scheme">
                                    <xsl:attribute name="id" select="concat('meta_',position())"/>
                                </xsl:if>
                                <xsl:value-of select="@content"/>
                            </xsl:element>
                            <xsl:if test="@scheme">
                                <!-- TODO: handle different schemes for different metadata -->
                                <!--<meta refines="#{if (@name='dc:identifier') then 'pub-id' else concat('meta_',position())}" property="role" scheme="???">
                                    <xsl:value-of select="@scheme"/>
                                </meta>-->
                            </xsl:if>
                        </xsl:when>
                        <xsl:when test="starts-with(@name,'ncc:')">
                            <xsl:choose>
                                <xsl:when test="@name='ncc:narrator'">
                                    <xsl:variable name="id" select="if (count(preceding-sibling::*/starts-with(@id,'narrator')) &gt; 0) then generate-id() else concat('narrator_',(count(preceding-sibling::*/@name='ncc:narrator')+1))"/>
                                    <xsl:if test="string-length(normalize-space(@content)) &gt; 0">
                                        <dc:contributor id="{$id}">
                                            <xsl:value-of select="@content"/>
                                        </dc:contributor>
                                        <meta refines="#{$id}" property="role" scheme="marc:relators">nrt</meta>
                                    </xsl:if>
                                </xsl:when>
                                <xsl:when test="@name='ncc:producer'">
                                    <xsl:variable name="id" select="if (count(preceding-sibling::*/starts-with(@id,'producer')) &gt; 0) then generate-id() else concat('producer_',(count(preceding-sibling::*/@name='ncc:producer')+1))"/>
                                    <xsl:if test="string-length(normalize-space(@content)) &gt; 0">
                                        <dc:contributor id="{$id}">
                                            <xsl:value-of select="@content"/>
                                        </dc:contributor>
                                        <meta refines="#{$id}" property="role" scheme="marc:relators">pro</meta>
                                    </xsl:if>
                                </xsl:when>
                                <xsl:when test="@name=('ncc:producedDate','ncc:revision','ncc:revisionDate','ncc:sourceDate','ncc:sourceEdition','ncc:sourcePublisher','ncc:sourceRights','ncc:sourceTitle')">
                                    <!-- TODO -->
                                    <!--<meta property="{@name}">
                                        <xsl:value-of select="@content"/>
                                    </meta>-->
                                </xsl:when>
                                <!-- Other ncc: metadata are irrelevant or inappropriate to include in the EPUB3 version. -->
                            </xsl:choose>
                        </xsl:when>
                        <!-- Metadata in other namespaces than dc: and ncc: are not copied -->
                        <xsl:when test="not(contains(@name,':'))">
                            <meta property="{@name}">
                                <xsl:value-of select="@content"/>
                                <!-- TODO: try handling schemes for arbitrary metadata? -->
                                <!--<xsl:if test="@scheme">
                                    <xsl:attribute name="scheme" select="@scheme"/>
                                </xsl:if>-->
                            </meta>
                        </xsl:when>
                    </xsl:choose>

                </xsl:if>
            </xsl:for-each>
        </metadata>
    </xsl:template>

</xsl:stylesheet>
