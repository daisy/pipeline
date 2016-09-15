<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:pf="http://www.daisy.org/ns/pipeline/functions" xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0" xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <!-- Returns the string containing the translation, or an empty sequence if no translation was found. -->
    <xsl:function name="pf:i18n-translate" as="xs:string">
        
        <!--
        example translation map:
            
        <i18n xmlns="http://www.daisy.org/ns/pipeline/data">
            <translation string="Guide">
                <text xml:lang="en">Guide</text>
                <text xml:lang="ar">دليل</text>
                <text xml:lang="bg">Ръководство</text>
            </translation>
            <translation string="...">
                <text xml:lang="...">...</text>
                ...
            </translation>
        </i18n>
        -->

        <!-- The string to look up in the translation map. -->
        <xsl:param name="string" as="xs:string"/>

        <!-- The preferred language (RFC5646). For instance "en" or "en-US". -->
        <xsl:param name="language" as="xs:string"/>

        <!-- The i18n XML documents. -->
        <xsl:param name="maps" as="element()*"/>

        <xsl:variable name="generic-language-code" select="tokenize(lower-case($language),'-')[1]"/>

        <xsl:variable name="exact-language" select="(for $map in ($maps) return $map//d:translation[@string=$string]/d:text[@xml:lang=$language])[1]"/>
        <xsl:variable name="generic-language"
            select="if ($exact-language) then () else (for $map in ($maps) return $map//d:translation[@string=$string]/d:text[starts-with(@xml:lang,$generic-language-code)])[1]"/>
        <xsl:variable name="any-language" select="if ($exact-language or $generic-language) then () else (for $map in ($maps) return $map//d:translation[@string=$string]/d:text[1])[1]"/>

        <xsl:value-of select="($exact-language, $generic-language, $any-language)[1]"/>
        
    </xsl:function>

</xsl:stylesheet>
