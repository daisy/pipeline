<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/2001/SMIL20/"
                xpath-default-namespace="http://www.w3.org/2001/SMIL20/"
                exclude-result-prefixes="xs pf">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:template match="/">
        <xsl:apply-templates select="/*">
            <xsl:with-param name="smils" tunnel="yes" select="collection()[position()&gt;1]"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="/*">
        <xsl:param name="smils" as="node()*" tunnel="yes"/> <!-- as="document-node()*" -->
        <xsl:variable name="base-uri" select="base-uri()"/>
        <xsl:copy>
            <xsl:apply-templates select="@*|node()">
                <xsl:with-param name="base-uri" tunnel="yes" select="$base-uri"/>
                <xsl:with-param name="smil-href" tunnel="yes" select="if (count($smils)=1)
                                                                      then pf:relativize-uri(base-uri($smils[1]),$base-uri)
                                                                      else ()"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[@id]">
        <xsl:param name="base-uri" as="xs:string" tunnel="yes"/>
        <xsl:param name="smil-href" as="xs:string?" tunnel="yes"/>
        <xsl:param name="smils" as="node()*" tunnel="yes"/> <!-- as="document-node()*" -->
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="text" select="($smils//text[resolve-uri(@src,base-uri())=concat($base-uri,'#',$id)])[1]"/>
        <xsl:choose>
            <xsl:when test="$text/parent::par/@id">
                <xsl:copy>
                    <xsl:variable name="smil-href" as="xs:string"
                                  select="if ($smil-href[1]) then $smil-href[1]
                                          else pf:relativize-uri(base-uri($text/root()),$base-uri)"/>
                    <xsl:apply-templates select="@*"/>
                    <xsl:attribute name="smilref" select="concat($smil-href,'#',$text/parent::par/@id)"/>
                    <xsl:apply-templates select="node()"/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="seq"
                              select="($smils//seq[resolve-uri(@epub:textref,base-uri())=concat($base-uri,'#',$id)])[1]"/>
                <xsl:choose>
                    <xsl:when test="$seq/@id">
                        <xsl:copy>
                            <xsl:variable name="smil-href" as="xs:string"
                                          select="if ($smil-href[1]) then $smil-href[1]
                                                  else pf:relativize-uri(base-uri($seq/root()),$base-uri)"/>
                            <xsl:apply-templates select="@*"/>
                            <xsl:attribute name="smilref" select="concat($smil-href,'#',$seq/@id)"/>
                            <xsl:apply-templates select="node()"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:next-match/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
