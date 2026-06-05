<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

    <xsl:param name="output-base-uri" required="yes"/>

    <xsl:key name="original-href" match="d:file[@original-href]" use="@original-href"/>
    <xsl:key name="original-id" match="d:anchor" use="(@original-id,@id)[1]"/>
    <xsl:key name="href" match="d:file[not(@original-href)]" use="@href"/>

    <xsl:variable name="mapping" as="document-node(element(d:fileset))">
        <xsl:document>
            <xsl:apply-templates mode="normalize" select="collection()[2]/*"/>
        </xsl:document>
    </xsl:variable>

    <xsl:template mode="normalize"
                  match="d:file/@href|
                         d:file/@original-href|
                         d:clip/@src|
                         d:clip/@textref">
        <xsl:attribute name="{name()}" select="pf:normalize-uri(resolve-uri(.,pf:base-uri(..)))"/>
    </xsl:template>

    <xsl:template match="/d:audio-clips">
        <xsl:variable name="audio-clips" as="element(d:audio-clips)">
            <xsl:apply-templates mode="normalize" select="."/>
        </xsl:variable>
        <xsl:variable name="textrefs" as="xs:string*" select="$audio-clips/d:clip/@textref"/>
        <xsl:if test="not(count($textrefs)=count(distinct-values($textrefs)))">
            <xsl:message terminate="yes">d:audio-clips document contains clips with the same textref</xsl:message>
        </xsl:if>
        <!--
            update textref and src of clips
        -->
        <xsl:variable name="audio-clips" as="element(d:audio-clips)">
            <xsl:apply-templates mode="map" select="$audio-clips"/>
        </xsl:variable>
        <!--
            merge clips with same textref
        -->
        <xsl:variable name="audio-clips" as="element(d:audio-clips)">
            <xsl:for-each select="$audio-clips">
                <xsl:copy>
                    <xsl:sequence select="@*"/>
                    <xsl:for-each-group select="d:clip" group-by="@textref">
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
                                        <d:clip textref="{$clips[1]/@textref}"
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

    <xsl:template mode="map" match="d:clip/@src">
        <xsl:variable name="file" as="xs:string" select="."/>
        <xsl:variable name="new-file" as="element(d:file)?" select="key('original-href',$file,$mapping)"/>
        <xsl:variable name="new-file" as="xs:string?" select="$new-file/@href"/>
        <xsl:attribute name="src" select="pf:relativize-uri(($new-file,$file)[1],$output-base-uri)"/>
    </xsl:template>

    <xsl:template mode="map" match="d:clip/@textref">
        <xsl:variable name="uri" as="xs:string" select="."/>
        <xsl:variable name="uri" as="xs:string*" select="pf:tokenize-uri($uri)"/>
        <xsl:variable name="fragment" as="xs:string" select="$uri[5]"/>
        <xsl:variable name="file" as="xs:string" select="pf:recompose-uri($uri[position()&lt;5])"/>
        <xsl:variable name="new-file" as="element(d:file)*" select="key('original-href',$file,$mapping)"/>
        <xsl:variable name="new-file" as="element(d:file)?" select="($new-file[exists(key('original-id',$fragment,.))],
                                                                     $new-file)[1]"/>
        <xsl:variable name="new-fragment" as="xs:string?" select="if (exists($new-file))
                                                                  then key('original-id',$fragment,$new-file)/@id
                                                                  else for $f in key('href',$file,$mapping)[1]
                                                                       return key('original-id',$fragment,$f)/@id"/>
        <xsl:variable name="new-file" as="xs:string?" select="$new-file/@href"/>
        <xsl:variable name="new-uri" select="string-join((($new-file,$file)[1],($new-fragment,$fragment)[1]),'#')"/>
        <xsl:attribute name="textref" select="pf:relativize-uri($new-uri,$output-base-uri)"/>
    </xsl:template>

    <xsl:template mode="#default normalize map" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

