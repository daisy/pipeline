<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:variable name="a" as="element(d:fileset)">
        <xsl:apply-templates mode="normalize" select="collection()[1]/*">
            <xsl:with-param name="base" tunnel="yes" select="collection()[1]/*/base-uri(.)"/>
        </xsl:apply-templates>
    </xsl:variable>
    <xsl:variable name="b" as="element(d:fileset)">
        <xsl:apply-templates mode="normalize" select="collection()[2]/*">
            <xsl:with-param name="base" tunnel="yes" select="collection()[2]/*/base-uri(.)"/>
        </xsl:apply-templates>
    </xsl:variable>

    <xsl:template match="/">
        <xsl:apply-templates select="$a"/>
    </xsl:template>

    <xsl:template match="d:fileset">
        <xsl:copy>
            <xsl:sequence select="@* except @xml:base"/>
            <xsl:variable name="files-and-anchors" as="element()*"> <!-- element(d:file|d:anchor)* -->
                <xsl:for-each select="d:file|d:anchor">
                    <xsl:choose>
                        <xsl:when test="self::d:file">
                            <xsl:variable name="a-file" as="element(d:file)" select="."/>
                            <xsl:variable name="b-file" as="element(d:file)*"
                                          select="$b/d:file[@original-href=$a-file/@href]"/>
                            <xsl:choose>
                                <xsl:when test="exists($b-file)">
                                    <xsl:for-each select="$b-file">
                                        <xsl:copy>
                                            <xsl:sequence select="@href|$a-file/@original-href"/>
                                        </xsl:copy>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:sequence select="."/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="a-anchor" as="element(d:anchor)" select="."/>
                            <xsl:variable name="b-anchor" as="element(d:anchor)?"
                                          select="$b/d:anchor[@original-id=$a-anchor/@id and
                                                              @original-href=$a-anchor/@href][1]"/>
                            <xsl:choose>
                                <xsl:when test="exists($b-anchor)">
                                    <xsl:copy>
                                        <xsl:sequence select="@original-id|@original-href|$b-anchor/(@id|@href)"/>
                                    </xsl:copy>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:variable name="b-file" as="element(d:file)?"
                                                  select="$b/d:file[@original-href=$a-anchor/@href][1]"/>
                                    <xsl:choose>
                                        <xsl:when test="exists($b-file)">
                                            <xsl:copy>
                                                <xsl:sequence select="@original-id|@id|@original-href|$b-file/@href"/>
                                            </xsl:copy>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:sequence select="."/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
                <xsl:sequence select="$b/d:file[not(@original-href=$a/d:file/(@href,@original-href))]"/>
                <xsl:for-each select="$b/d:anchor[not(some $anc in $a/d:anchor
                                                      satisfies $anc/@href=@original-href and
                                                                $anc/@id=@original-id)]
                                                 [not(some $anc in $a/d:anchor
                                                      satisfies $anc/@original-href=@original-href and
                                                                $anc/@original-id=@original-id)]">
                    <xsl:variable name="b-anchor" as="element(d:anchor)" select="."/>
                    <xsl:variable name="a-file" as="element(d:file)?"
                                  select="$a/d:file[@href=$b-anchor/@original-href][1]"/>
                    <xsl:choose>
                        <xsl:when test="exists($a-file)">
                            <xsl:copy>
                                <xsl:sequence select="@original-id|@id|@href|$a-file/@original-href"/>
                            </xsl:copy>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:sequence select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="files-with-anchors" as="element(d:file)*">
                <xsl:for-each-group select="$files-and-anchors" group-by="@original-href">
                    <xsl:for-each-group select="current-group()" group-by="@href">
                        <d:file>
                            <xsl:sequence select="@href|@original-href"/>
                            <xsl:sequence select="current-group()[self::d:anchor]"/>
                        </d:file>
                    </xsl:for-each-group>
                </xsl:for-each-group>
            </xsl:variable>
            <xsl:variable name="base" as="attribute()?" select="($b/@xml:base,@xml:base)[1]"/>
            <xsl:sequence select="$base"/>
            <xsl:apply-templates mode="relativize" select="$files-with-anchors">
                <xsl:with-param name="base" tunnel="yes" select="$base"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="normalize"
                  match="/d:fileset|
                         /d:fileset/d:file/d:anchor/@id|
                         /d:fileset/d:file/d:anchor/@original-id">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="normalize"
                  match="/d:fileset/d:file[@href]">
        <xsl:variable name="normalized-file" as="element(d:file)">
            <xsl:copy>
                <xsl:variable name="normalized-attrs" as="attribute()*">
                    <xsl:apply-templates mode="#current" select="@*"/>
                </xsl:variable>
                <xsl:sequence select="$normalized-attrs"/>
                <xsl:if test="not(@original-href)">
                    <xsl:attribute name="original-href" select="$normalized-attrs[name()='href']"/>
                </xsl:if>
            </xsl:copy>
        </xsl:variable>
        <xsl:sequence select="$normalized-file"/>
        <xsl:apply-templates mode="#current">
            <xsl:with-param name="normalized-parent" select="$normalized-file"/>
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template mode="normalize"
                  match="/d:fileset/d:file/d:anchor[@id]">
        <xsl:param name="normalized-parent" as="element(d:file)" required="yes"/>
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*"/>
            <xsl:if test="not(@original-id)">
                <xsl:attribute name="original-id" select="@id"/>
            </xsl:if>
            <xsl:sequence select="$normalized-parent/(@href|@original-href)"/>
        </xsl:copy>
    </xsl:template>
 
    <xsl:template mode="normalize"
                  match="d:fileset/@xml:base|
                         d:file/@href|
                         d:file/@original-href">
        <xsl:param name="base" tunnel="yes" required="yes"/>
        <xsl:attribute name="{name()}" select="pf:normalize-uri(resolve-uri(.,$base))"/>
    </xsl:template>

    <xsl:template mode="relativize" match="d:anchor/@href|
                                           d:anchor/@original-href|
                                           d:anchor/@original-id[.=../@id]|
                                           d:file/@original-href[.=../@href]"/>

    <xsl:template mode="relativize" match="d:file/@href">
        <xsl:param name="base" tunnel="yes" required="yes" as="xs:string?"/>
        <xsl:choose>
            <xsl:when test="exists($base)">
                <xsl:attribute name="href" select="pf:relativize-uri(.,$base)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template mode="normalize" match="@*|node()" priority="0.4"/>

    <xsl:template mode="#default relativize" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
