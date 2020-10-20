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

    <xsl:template match="*[@id]" priority="0.9">
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
                        <xsl:apply-templates>
                            <xsl:with-param name="inside-smilref" tunnel="yes" select="true()"/>
                        </xsl:apply-templates>
                    </xsl:element>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="html:a[not(matches(@href,'.+\.[Ss][Mm][Ii][Ll]#.+$'))]" priority="1">
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
                            <xsl:apply-templates/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <!--
                            Check if there is a seq that references the heading/pagenum using a @textref
                        -->
                        <xsl:variable name="seq" select="($smil//seq[@textref][resolve-uri(@textref,base-uri())=$href])[1]"/>
                        <xsl:variable name="smil-href" as="xs:string"
                                      select="if ($smil-href[1]) then $smil-href[1]
                                              else pf:relativize-uri(base-uri($seq/root()),$base-uri)"/>
                        <xsl:choose>
                            <!--
                                Check if the referenced element is a page number that is contained
                                in an element that is referenced by a par.
                            -->
                            <xsl:when test="$seq/@contained-in">
                                <xsl:variable name="par" select="$seq/root()//*[@id=$seq/@contained-in]/(self::par|self::seq/par[1])"/>
                                <xsl:if test="not(exists($par))">
                                    <xsl:message terminate="yes">coding error</xsl:message>
                                </xsl:if>
                                <xsl:copy>
                                    <xsl:apply-templates select="@* except @href"/>
                                    <xsl:attribute name="href" select="concat($smil-href,'#',$par/@id)"/>
                                    <xsl:apply-templates/>
                                </xsl:copy>
                            </xsl:when>
                            <xsl:when test="$seq">
                                <xsl:copy>
                                    <xsl:apply-templates select="@* except @href"/>
                                    <xsl:attribute name="href" select="concat($smil-href,'#',$seq/par[1]/@id)"/>
                                    <xsl:apply-templates/>
                                </xsl:copy>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:message terminate="yes">coding error</xsl:message>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="html:a" priority="0.8">
        <xsl:param name="inside-smilref" tunnel="yes" select="false()"/>
        <xsl:choose>
            <xsl:when test="$inside-smilref">
                <xsl:element name="span" namespace="http://www.w3.org/1999/xhtml">
                    <xsl:sequence select="@id|@title|@xml:lang"/>
                    <xsl:attribute name="class"
                                   select="string-join(('anchor',distinct-values(@class/tokenize(.,'\s+')[not(.='')])),' ')"/>
                    <xsl:apply-templates/>
                </xsl:element>
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
