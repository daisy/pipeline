<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:saxon		= "http://saxon.sf.net/"
    exclude-result-prefixes="xs"
    version="2.0">
  
    <xsl:output
        method="xml"
        encoding="UTF-8"
        indent="yes"
        saxon:line-length="100000"
        />
    <xsl:template match="/*:root" priority="2">
       <root>
          <xsl:apply-templates select="@* | node()"/>
        </root>
    </xsl:template>

    <xsl:template match="*:file">
        <xsl:message>Processing file#<xsl:value-of select="@position"/></xsl:message>
        <xsl:element name="file">
          <xsl:apply-templates select="@*" mode="#default"/>
          <xsl:variable name="first">
            <xsl:apply-templates select="node()" mode="#default"/>
          </xsl:variable>
          <xsl:apply-templates select="$first" mode="repair-subsup"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@position"/>

    <xsl:template match="@href">
        <xsl:attribute name="href">
            <xsl:value-of select="."/>
        </xsl:attribute>
    </xsl:template>

    <xsl:include href="mathtype_to_mathml/lib/transform.xsl" />
</xsl:stylesheet>
