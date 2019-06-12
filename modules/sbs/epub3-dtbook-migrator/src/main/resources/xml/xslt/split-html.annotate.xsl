<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
    
    <xsl:param name="output-dir" required="yes"/>
    
    <xsl:template match="@* | node()" mode="#all">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@* | node()" mode="#current"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/*">

        <xsl:variable name="partition-types" select="('cover','frontmatter','bodymatter','backmatter')"/>
        <xsl:variable name="division-types"
            select="('abstract','acknowledgments','afterword','answers','appendix','assessment','assessments','bibliography','z3998:biographical-note','case-study','chapter','colophon','conclusion','contributors','copyright-page','credits','dedication','z3998:discography','division','z3998:editorial-note','epigraph','epilogue','errata','z3998:filmography','footnotes','foreword','glossary','z3998:grant-acknowledgment','halftitlepage','imprimatur','imprint','index','index-group','index-headnotes','index-legend','introduction','keywords','landmarks','loa','loi','lot','lov','notice','other-credits','page-list','part','practices','preamble','preface','prologue','z3998:promotional-copy','z3998:published-works','z3998:publisher-address','qna','rearnotes','revision-history','z3998:section','seriespage','subchapter','z3998:subsection','titlepage','toc','toc-brief','z3998:translator-note','volume')"/>
        <xsl:variable name="identifier" select="(//html/head/meta[@name='dc:identifier']/string(@content))[1]"/>
        <xsl:variable name="padding-size"
            select="string-length(string(count(/*/body/( header | section | article | section[f:types(.)='part']/(section|article)[f:types(.)=$division-types] | (section|article)[f:types(.)='bodymatter']/section[f:types(.)='rearnotes'] ))))"/>

        <xsl:copy exclude-result-prefixes="#all">
            <xsl:copy-of select="@*" exclude-result-prefixes="#all"/>
            <xsl:attribute name="xml:base" select="pf:base-uri(/*)"/>
            <xsl:copy-of select="head" exclude-result-prefixes="#all"/>
            <xsl:for-each select="body">
                <xsl:copy exclude-result-prefixes="#all">
                    <xsl:copy-of select="@*" exclude-result-prefixes="#all"/>
                    <xsl:variable name="top-level-sections" select="header | section | article"/>
                    <xsl:variable name="part-sections" select="$top-level-sections[f:types(.)='part']/(section | article)[f:types(.)=$division-types]"/>
                    <xsl:variable name="bodymatter-rearnotes" select="($top-level-sections, $part-sections)[not(f:types(.)=('cover','frontmatter','backmatter'))]/section[f:types(.)='rearnotes']"/>
                    <xsl:for-each select="$top-level-sections | $part-sections | $bodymatter-rearnotes">
                        <xsl:copy exclude-result-prefixes="#all">
                            <xsl:variable name="types" select="f:types(.)"/>
                            <xsl:variable name="partition" select="if (self::header) then 'frontmatter' else ((ancestor-or-self::*/f:types(.)[.=$partition-types]), 'bodymatter')[1]"/>
                            <xsl:variable name="division" select="if (self::header) then 'header' else if (count($types[.=$division-types])) then ($types[.=$division-types])[1] else if ($partition='bodymatter') then 'chapter' else ()"/>
                            <xsl:variable name="filename"
                                select="concat($identifier,'-',f:zero-pad(string(position()),$padding-size),'-',if ($division) then tokenize($division,':')[last()] else $partition)"/>

                            <xsl:copy-of select="@*" exclude-result-prefixes="#all"/>
                            <xsl:attribute name="xml:base" select="concat($output-dir,$filename,'.xhtml')"/>
                            <xsl:if test="not(self::header)">
                                <xsl:attribute name="epub:type" select="string-join(($partition, $division, $types[not(.=($partition-types,$division-types))]),' ')"/>
                            </xsl:if>

                            <xsl:choose>
                                <xsl:when test="$division='part'">
                                    <xsl:apply-templates select="node()[not((self::section | self::article)[f:types(.)=$division-types])]"/>

                                </xsl:when>
                                <xsl:when test="$partition='bodymatter'">
                                    <xsl:apply-templates select="node()[not(self::section[f:types(.)='rearnotes'])]"/>

                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:apply-templates select="node()"/>

                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:copy>
                    </xsl:for-each>
                </xsl:copy>
            </xsl:for-each>

        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[f:types(.)='pagebreak' and exists(ancestor::section[1][f:types(.) = 'rearnotes' and exists(following-sibling::section[not(f:types(.) = 'rearnotes')])])]"/>
    
    <xsl:template match="section">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@* | node()"/>
            
            <xsl:if test="not(ancestor-or-self::*/f:types(.) = 'rearnotes') and following-sibling::section[1]/f:types(.) = 'rearnotes'">
                <xsl:for-each select="(following-sibling::section intersect following-sibling::section[not(f:types(.) = 'rearnotes')][1]/preceding-sibling::section)//*[f:types(.) = 'pagebreak']">
                    <div>
                        <xsl:apply-templates select="@* | node()"/>
                    </div>
                </xsl:for-each>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:function name="f:types" as="xs:string*">
        <xsl:param name="element" as="element()"/>
        <xsl:sequence select="tokenize($element/@epub:type,'\s+')"/>
    </xsl:function>

    <xsl:function name="f:zero-pad" as="xs:string">
        <xsl:param name="text" as="xs:string"/>
        <xsl:param name="desired-length" as="xs:integer"/>
        <xsl:sequence select="concat(string-join(for $i in (string-length($text)+1 to $desired-length) return '0',''),$text)"/>
    </xsl:function>

</xsl:stylesheet>
