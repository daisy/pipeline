<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns=""
                xpath-default-namespace=""
                exclude-result-prefixes="xs">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="is-ncc" as="xs:string" select="'false'"/>

    <xsl:template match="/">
        <xsl:apply-templates select="/*">
            <xsl:with-param name="smil" tunnel="yes" select="collection()[position()&gt;1]"/>
        </xsl:apply-templates>
    </xsl:template>
    
    <xsl:template match="/*">
        <xsl:param name="smil" as="node()*" tunnel="yes"/> <!-- as="document-node()*" -->
        <xsl:variable name="base-uri" select="base-uri()"/>
        <xsl:copy>
            <xsl:apply-templates select="@*|html:*">
                <xsl:with-param name="base-uri" tunnel="yes" select="$base-uri"/>
                <xsl:with-param name="smil-href" tunnel="yes" select="if (count($smil)=1)
                                                                      then pf:relativize-uri(base-uri($smil),$base-uri)
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
        <xsl:param name="smil-href" as="xs:string*" tunnel="yes"/>
        <xsl:param name="smil" as="node()*" tunnel="yes"/> <!-- as="document-node()*" -->
        <xsl:variable name="id" select="@id"/>
        <xsl:variable name="par" select="($smil//par[text/resolve-uri(@src,base-uri()) = concat($base-uri,'#',$id)])[1]"/>
        <xsl:choose>
            <xsl:when test="$par">
                <xsl:copy>
                    <xsl:apply-templates select="@*"/>
                    <xsl:element name="a" namespace="http://www.w3.org/1999/xhtml">
                        <xsl:attribute name="href" select="concat($smil-href,'#',$par/@id)"/>
                        <xsl:apply-templates select="node()"/>
                    </xsl:element>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="html:a">
        <xsl:param name="base-uri" as="xs:string" tunnel="yes"/>
        <xsl:param name="smil-href" as="xs:string*" tunnel="yes"/>
        <xsl:param name="smil" as="node()*" tunnel="yes"/> <!-- as="document-node()*" -->
        <xsl:choose>
            <xsl:when test="$is-ncc='true'">
                <xsl:variable name="href" select="resolve-uri(@href,$base-uri)"/>
                <xsl:variable name="par" select="($smil//par[text/resolve-uri(@src,base-uri())=$href])[1]"/>
                <xsl:choose>
                    <xsl:when test="$par">
                        <xsl:variable name="smil-href" as="xs:string"
                                      select="if ($smil-href[1]) then $smil-href[1]
                                              else pf:relativize-uri(base-uri($par/root()),$base-uri)"/>
                        <xsl:copy>
                            <xsl:apply-templates select="@* except @href"/>
                            <xsl:attribute name="href" select="concat($smil-href,'#',$par/@id)"/>
                            <xsl:apply-templates select="node()"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:next-match/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <!--
        for XSpec
    -->
    <xsl:template name="test">
        <xsl:param name="html"/>
        <xsl:apply-templates select="$html"/>
    </xsl:template>
    
</xsl:stylesheet>
