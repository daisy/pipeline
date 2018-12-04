<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:html="http://www.w3.org/1999/xhtml"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xpath-default-namespace=""
    exclude-result-prefixes="#all"
    version="2.0">
    
    <!-- Parameters -->
    <xsl:param name="contraction-grade" as="xs:string" select="'0'"/>
    
    <!-- FIXME: these numbers should be generated with CSS -->
    <xsl:param name="first-page-in-volume" as="xs:string" select="''"/>
    <xsl:param name="last-page-in-volume" as="xs:string" select="''"/>
    
    <!-- Variables -->
    <xsl:variable name="OUTPUT_NAMESPACE" as="xs:string" select="namespace-uri(/*)"/>
    <xsl:variable name="AUTHOR" as="xs:string?"
        select="(/dtbook/head/meta|/dtb:dtbook/dtb:head/dtb:meta|/html:html/
                html:head/html:meta)[@name eq 'dc:creator'][1]/@content"/>
    <xsl:variable name="PID" as="xs:string?"
        select="(/dtbook/head/meta|/dtb:dtbook/dtb:head/dtb:meta|/html:html/
                html:head/html:meta)[@name eq 'dc:identifier'][1]/@content"/>
    <xsl:variable name="SOURCE_ISBN" as="xs:string?"
        select="(/dtbook/head/meta|/dtb:dtbook/dtb:head/dtb:meta|/html:html/
                html:head/html:meta)[@name eq 'dc:source'][1]/replace(@content,
                '^urn:isbn:', '')"/>
    <xsl:variable name="TITLE" as="xs:string?"
        select="(/dtbook/head/meta|/dtb:dtbook/dtb:head/dtb:meta)[@name eq
                'dc:title'][1]/@content|/html:html/html:head/html:title"/>
    <xsl:variable name="YEAR" as="xs:integer"
        select="year-from-date(current-date())"/>
    
    <!-- Generic copy-all template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="TITLE_PAGE_CONTENT">
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_author'"/>
            <xsl:value-of select="$AUTHOR"/>
        </xsl:element>
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_title'"/>
            <xsl:value-of select="$TITLE"/>
        </xsl:element>
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_grade'"/>
            <xsl:value-of
                select="if ($contraction-grade eq '0')
                        then 'uforkortet'
                        else if ($contraction-grade eq '1')
                        then 'lille forkortelse'
                        else if ($contraction-grade eq '2')
                        then 'stor forkortelse'
                        else ''"/>
        </xsl:element>
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_volume'"/>
            <xsl:element name="span" namespace="{$OUTPUT_NAMESPACE}">
                <xsl:attribute name="style" select="'::before { content: -obfl-evaluate(&quot;(round $volume)&quot;); }'"/>
            </xsl:element>
            <xsl:text>. bind af </xsl:text>
            <xsl:element name="span" namespace="{$OUTPUT_NAMESPACE}">
                <xsl:attribute name="style" select="'::before { content: -obfl-evaluate(&quot;(round $volumes)&quot;); }'"/>
            </xsl:element>
        </xsl:element>
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_imprint'"/>
            nota<br/>
            nationalbibliotek for<br/>
            mennesker med læsevanskeligheder<br/>
            københavn <xsl:value-of select="$YEAR"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="COLOPHON_CONTENT">
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_title_colophon'"/>
            <xsl:value-of select="$TITLE"/>
        </xsl:element>
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_isbn'"/>
            <xsl:value-of select="concat('isbn ', $SOURCE_ISBN)"/>
        </xsl:element>
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_report'"/>
            fejl i punktudgaven kan rapporteres på aub@nota.nu
        </xsl:element>
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_pid'"/>
            <xsl:value-of select="$PID"/>
        </xsl:element>
        <xsl:element name="p" namespace="{$OUTPUT_NAMESPACE}">
            <xsl:attribute name="class" select="'braille_volume_contents'"/>
            <xsl:element name="span" namespace="{$OUTPUT_NAMESPACE}">
                <xsl:attribute name="style" select="'::before { content: -obfl-evaluate(&quot;(round $volume)&quot;); }'"/>
            </xsl:element>
            <xsl:value-of select="concat('. punktbind: ', $first-page-in-volume, '-', $last-page-in-volume)"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="frontmatter/doctitle">
        <xsl:copy-of select="."/>
        <level depth="1" class="braille_title_page">
            <xsl:call-template name="TITLE_PAGE_CONTENT"/>
        </level>
        <level depth="1" class="braille_colophon">
            <xsl:call-template name="COLOPHON_CONTENT"/>
        </level>
    </xsl:template>
    
    <xsl:template match="dtb:frontmatter/dtb:doctitle">
        <xsl:copy-of select="."/>
        <level1 xmlns="http://www.daisy.org/z3986/2005/dtbook/" class="braille_title_page">
            <xsl:call-template name="TITLE_PAGE_CONTENT"/>
        </level1>
        <level1 xmlns="http://www.daisy.org/z3986/2005/dtbook/" class="braille_colophon">
            <xsl:call-template name="COLOPHON_CONTENT"/>
        </level1>
    </xsl:template>
    
    <xsl:template match="html:html/html:body[1]">
        <body xmlns="http://www.w3.org/1999/xhtml" class="braille_title_page">
            <xsl:call-template name="TITLE_PAGE_CONTENT"/>
        </body>
        <body xmlns="http://www.w3.org/1999/xhtml" class="braille_colophon">
            <xsl:call-template name="COLOPHON_CONTENT"/>
        </body>
        <xsl:copy-of select="."/>
    </xsl:template>
    
</xsl:stylesheet>