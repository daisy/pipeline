<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    xmlns:louis="http://liblouis.org/liblouis"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:re="regex-utils"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <!--
        css-utils [2.0.0,3.0.0)
    -->
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="/*">
        <xsl:variable name="page-stylesheet" as="xs:string" select="string(@css:page)"/>
        <xsl:variable name="rules" as="element()*" select="css:deep-parse-page-stylesheet($page-stylesheet)"/>
        <xsl:variable name="properties" as="element()*" select="$rules[not(@selector)]/*"/>
        <xsl:variable name="size" as="xs:string"
                      select="($properties[@name='size'][css:is-valid(.)]/@value, css:initial-value('size'))[1]"/>
        <xsl:variable name="top-right-content" as="element()*" select="pxi:margin-content($rules, '@top-right')"/>
        <xsl:variable name="bottom-right-content" as="element()*" select="pxi:margin-content($rules, '@bottom-right')"/>
        <xsl:variable name="top-center-content" as="element()*" select="pxi:margin-content($rules, '@top-center')"/>
        <xsl:variable name="bottom-center-content" as="element()*" select="pxi:margin-content($rules, '@bottom-center')"/>
        <xsl:variable name="print-page-position"
            select="if ($top-right-content/self::css:string[@name='print-page']) then 'top-right' else
                    if ($bottom-right-content/self::css:string[@name='print-page']) then 'bottom-right' else
                    'none'"/>
        <xsl:variable name="braille-page-position"
            select="if ($top-right-content/self::css:counter[@name='braille-page']) then 'top-right' else
                    if ($bottom-right-content/self::css:counter[@name='braille-page']) then 'bottom-right' else
                    'none'"/>
        <xsl:element name="louis:page-layout">
            <xsl:element name="c:param-set">
                <xsl:element name="c:param">
                    <xsl:attribute name="name" select="'louis:page-width'"/>
                    <xsl:attribute name="value" select="tokenize($size, '\s+')[1]"/>
                </xsl:element>
                <xsl:element name="c:param">
                    <xsl:attribute name="name" select="'louis:page-height'"/>
                    <xsl:attribute name="value" select="tokenize($size, '\s+')[2]"/>
                </xsl:element>
                <xsl:element name="c:param">
                    <xsl:attribute name="name" select="'louis:print-page-position'"/>
                    <xsl:attribute name="value" select="$print-page-position"/>
                </xsl:element>
                <xsl:element name="c:param">
                    <xsl:attribute name="name" select="'louis:braille-page-position'"/>
                    <xsl:attribute name="value" select="$braille-page-position"/>
                </xsl:element>
                <xsl:if test="$braille-page-position!='none'">
                    <xsl:variable name="format"
                        select="(if ($braille-page-position='top-right') then $top-right-content else $bottom-right-content)
                                /self::css:counter[@name='braille-page'][1]/@style"/>
                    <xsl:element name="c:param">
                        <xsl:attribute name="name" select="'louis:braille-page-format'"/>
                        <xsl:attribute name="value">
                            <xsl:choose>
                                <xsl:when test="$format=('lower-roman','upper-roman')">
                                    <xsl:value-of select="'lower-roman'"/>
                                </xsl:when>
                                <xsl:when test="$format='prefix-p'">
                                    <xsl:value-of select="'prefix-p'"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="'decimal'"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>
                    </xsl:element>
                </xsl:if>
                <xsl:element name="c:param">
                    <xsl:attribute name="name" select="'louis:page-break-separator'"/>
                    <xsl:attribute name="value"
                        select="if (//louis:print-page[@break='true'] and not(//louis:print-page[@break='false']))
                                then 'true' else 'false'"/>
                </xsl:element>
                <xsl:element name="c:param">
                    <xsl:attribute name="name" select="'louis:running-header'"/>
                    <xsl:attribute name="value"
                        select="if ($top-center-content/self::css:string[@name='running-header'])
                                then 'true' else 'false'"/>
                </xsl:element>
                <xsl:element name="c:param">
                    <xsl:attribute name="name" select="'louis:running-footer'"/>
                    <xsl:attribute name="value"
                        select="if ($bottom-center-content/self::css:string[@name='running-footer'])
                                then 'true' else 'false'"/>
                </xsl:element>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    
    <xsl:function name="pxi:margin-content" as="element()*">
        <xsl:param name="margin-rules" as="element()*"/>
        <xsl:param name="selector" as="xs:string"/>
        <xsl:sequence select="css:parse-content-list(
                                $margin-rules[@selector=$selector]/*[@name='content'][1]/@value,
                                ())"/>
    </xsl:function>

</xsl:stylesheet>
