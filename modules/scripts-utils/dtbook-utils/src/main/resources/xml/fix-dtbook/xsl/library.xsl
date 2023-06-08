<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
		xmlns:Library="org.daisy.pipeline.modules.dtbook_utils.impl.DTBookCleanerLibrary"
                exclude-result-prefixes="#all">

    <xsl:variable name="_lib_" select="Library:new()" />

    <!-- Get the default locale from the JVM -->
    <xsl:function name="pf:default-locale" as="xs:string">
        <xsl:value-of select="Library:getDefaultLocale($_lib_)">
            <!--
                Implemented in java/org/daisy/pipeline/modules/dtbook_cleaner/impl/Pipeline1LibraryDefinition.java
            -->
        </xsl:value-of>
    </xsl:function>


    <!-- content serialization
       < et > are replaced respectively by ￼ et � for tags and ease unserialization
       of text.
       -->
    <xsl:template match="*" mode="serialize">
        <xsl:text>￼</xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:apply-templates select="@*" mode="serialize" />
        <xsl:choose>
            <xsl:when test="node()">
                <xsl:text>�</xsl:text>
                <xsl:apply-templates mode="serialize" />
                <xsl:text>￼/</xsl:text>
                <xsl:value-of select="name()"/>
                <xsl:text>�</xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:text> /�</xsl:text>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@*" mode="serialize">
        <xsl:text> </xsl:text>
        <xsl:value-of select="name()"/>
        <xsl:text>="</xsl:text>
        <xsl:value-of select="."/>
        <xsl:text>"</xsl:text>
    </xsl:template>

    <xsl:template match="text()" mode="serialize">
        <xsl:value-of disable-output-escaping="yes" select="." />
    </xsl:template>

    <!-- Unserialization template -->
    <xsl:template name="un-serialize">
        <xsl:param name="text" />
        <xsl:choose>
            <xsl:when test="$text = ''" >
                <!-- Prevent this routine from hanging -->
                <xsl:value-of select="$text" />
            </xsl:when>
            <!-- Si le texte contient un symbole '￼' balise ouvrante-->
            <xsl:when test="contains($text, '￼')">
                <!-- On copie le contenu avant le symbole d'ouverture-->
                <xsl:value-of select="substring-before($text,'￼')" />
                <!-- Recréation du < d'ouverture de balise -->
                <xsl:value-of
                        disable-output-escaping="yes"
                        select="'&lt;'" />
                <!-- copie du contenu de la balise -->
                <xsl:value-of select="substring-before(substring-after($text,'￼'), '�')" />
                <!-- fermeture de la balise -->
                <xsl:value-of
                        disable-output-escaping="yes"
                        select="'&gt;'" />
                <!-- on continue sur le reste du contenu textuel après la balise -->
                <xsl:call-template name="un-serialize">
                    <xsl:with-param name="text" select="substring-after($text,'�')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <!-- le texte ne contient pas de symbole de balisage, on le copie tel quel-->
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
