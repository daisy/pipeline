<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="output-base-uri" required="yes"/>

    <xsl:key name="original-href" match="d:file" use="string(@original-href)"/>
    <xsl:key name="original-id" match="d:anchor" use="string(@original-id)"/>

    <xsl:variable name="mapping" as="document-node(element(d:fileset))">
        <xsl:document>
            <xsl:apply-templates mode="normalize" select="collection()[2]"/>
        </xsl:document>
    </xsl:variable>

    <xsl:template mode="normalize"
                  match="d:file/@href|
                         d:file/@original-href">
        <xsl:attribute name="{name()}" select="pf:normalize-uri(resolve-uri(.,base-uri(.)))"/>
    </xsl:template>

    <xsl:template match="/d:audio-clips">
        <!--
            update idref and src of clips
        -->
        <xsl:variable name="audio-clips" as="element(d:audio-clips)">
            <xsl:next-match/>
        </xsl:variable>
        <!--
            merge clips with same idref
        -->
        <xsl:variable name="audio-clips" as="element(d:audio-clips)">
            <xsl:for-each select="$audio-clips">
                <xsl:copy>
                    <xsl:sequence select="@*"/>
                    <xsl:for-each-group select="d:clip" group-by="@idref">
                        <xsl:variable name="clips" as="element(d:clip)*" select="current-group()"/>
                        <xsl:choose>
                            <xsl:when test="count($clips)=1">
                                <xsl:sequence select="$clips"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="clips" as="element(d:clip)*">
                                    <xsl:perform-sort select="$clips">
                                        <xsl:sort select="@clipBegin"/>
                                    </xsl:perform-sort>
                                </xsl:variable>
                                <xsl:choose>
                                    <xsl:when test="every $i in 1 to count($clips) - 1
                                                    satisfies $clips[$i]/@src=$clips[$i + 1]/@src
                                                    and $clips[$i]/@clipEnd=$clips[$i + 1]/@clipBegin">
                                        <d:clip idref="{$clips[1]/@idref}"
                                                src="{$clips[1]/@src}"
                                                clipBegin="{$clips[1]/@clipBegin}"
                                                clipEnd="{$clips[last()]/@clipEnd}"/>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:message terminate="yes"
                                                     select="concat(
                                                               'Audio clips can not be combined: ',
                                                               string-join($clips/concat(@src,' (',@clipBegin,'-',@clipEnd,')'),', '))"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </xsl:copy>
            </xsl:for-each>
        </xsl:variable>
        <xsl:sequence select="$audio-clips"/>
    </xsl:template>

    <xsl:template match="d:clip">
        <xsl:variable name="src" select="pf:normalize-uri(resolve-uri(@src,pf:base-uri(.)))"/>
        <xsl:variable name="file" as="element(d:file)?" select="key('original-href',$src,$mapping)"/>
        <xsl:variable name="anchor" as="element(d:anchor)?" select="key('original-id',@idref,$mapping)"/>
        <xsl:copy>
            <xsl:apply-templates select="@* except (@src|@idref)"/>
            <xsl:attribute name="src" select="pf:relativize-uri(($file/@href,$src)[1],$output-base-uri)"/>
            <xsl:choose>
                <xsl:when test="exists($anchor)">
                    <xsl:attribute name="idref" select="$anchor/@id"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="@idref"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="#default normalize" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

