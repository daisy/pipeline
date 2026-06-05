<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

    <xsl:include href="epub3-vocab.xsl"/>

    <xsl:param name="prefixes" required="yes"/>

    <xsl:template match="/*">
        <xsl:variable name="existing-mappings" as="element(f:vocab)*"
                      select="if (@prefix|@epub:prefix)
                              then f:parse-prefix-decl(@prefix|@epub:prefix)
                              else ()"/>
        <xsl:variable name="new-mappings" as="element(f:vocab)*" select="f:parse-prefix-decl($prefixes)"/>
        <xsl:variable name="prefix" as="xs:string"
                      select="string-join(
                                for $vocab in f:merge-prefix-decl(($existing-mappings,$new-mappings),
                                                                  $f:default-prefixes)
                                  return concat($vocab/@prefix,': ',$vocab/@uri),
                                ' ')"/>
        <xsl:copy>
            <xsl:choose>
                <xsl:when test="self::opf:*">
                    <xsl:attribute name="prefix" select="$prefix"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="epub:prefix" select="$prefix"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:sequence select="(@* except (@prefix|@epub:prefix))|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
