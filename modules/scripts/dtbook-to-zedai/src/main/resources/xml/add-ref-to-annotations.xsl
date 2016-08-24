<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:z="http://www.daisy.org/ns/z3998/authoring/"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    exclude-result-prefixes="xs z" version="2.0">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="z:annotation[not(@ref)]">
        <xsl:message>Annotations without @ref will be attached to their nearest section parent
            element.</xsl:message>
        <xsl:copy>
            <xsl:attribute name="ref" select="ancestor::z:section/@xml:id"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <!-- we know that, in our converted documents, frontmatter always contains a section -->
    <xsl:template match="z:frontmatter/z:section[1]">
        <xsl:param name="firstSection"/>
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <xsl:if test="//z:annotation[not(@ref)]">
                <annotation xmlns="http://www.daisy.org/ns/z3998/authoring/" role="production"> Please review annotations and their targets (@ref).
                    Source document contained one or more annotations with no explicit references.
                </annotation>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
