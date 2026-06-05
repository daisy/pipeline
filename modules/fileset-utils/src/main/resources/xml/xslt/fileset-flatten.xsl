<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:param name="prefix" select="''"/>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:for-each select="d:file">
                <xsl:copy>
                    <xsl:copy-of select="@* except @href"/>
                    <!-- remove everything from the path except the file name -->
                    <xsl:variable name="path" as="xs:string*" select="tokenize(@href,'[\\/]+')"/>
                    <xsl:variable name="filename" as="xs:string" select="$path[last()]"/>
                    <xsl:variable name="filename" as="xs:string"
                                  select="if ($filename='') then concat($path[last()-1],'/') else $filename"/>
                    <xsl:attribute name="href" select="concat($prefix,$filename)"/>
                    <xsl:copy-of select="node()"/>
                </xsl:copy>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
