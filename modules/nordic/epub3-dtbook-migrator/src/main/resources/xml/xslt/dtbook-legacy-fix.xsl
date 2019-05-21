<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0"
    xmlns="http://www.daisy.org/z3986/2005/dtbook/" xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/" xmlns:pf="http://www.daisy.org/ns/pipeline/functions">

    <!-- TODO: fix capitalization for language codes in xml:lang -->

    <xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/numeral-conversion.xsl"/>
    <!-- TODO: move these functions to numeral-conversion.xsl -->
    <xsl:function name="pf:numeric-decimal-to-alpha" as="xs:string">
        <xsl:param name="integer" as="xs:integer"/>
        <xsl:variable name="this" select="$integer mod 26"/>
        <xsl:variable name="remainder" select="max(($integer - $this*26, 0))"/>
        <xsl:sequence select="string-join((if ($remainder) then pf:numeric-decimal-to-alpha($remainder) else (), codepoints-to-string($this+96)),'')"/>
    </xsl:function>
    <xsl:function name="pf:numeric-alpha-to-decimal" as="xs:integer">
        <xsl:param name="alpha" as="xs:string"/>
        <xsl:variable name="codepoints" select="for $cp in (string-to-codepoints('ab')) return $cp - 96"/>
        <xsl:sequence select="sum(for $pos in (1 to count($codepoints)) return $codepoints[$pos] * pf:numeric-integer-power(26, $pos - 1))"/>
    </xsl:function>
    <xsl:function name="pf:numeric-integer-power" as="xs:integer">
        <xsl:param name="integer" as="xs:integer"/>
        <xsl:param name="power" as="xs:integer"/>
        <xsl:sequence select="if ($power=0) then $integer else $integer * pf:numeric-integer-power($integer, $power - 1)"/>
    </xsl:function>

    <xsl:template match="@*|node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="meta[not(starts-with(@name,'track')) and matches(@name,'.*:Supplier$','i')]">
        <meta name="track:Supplier" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[not(starts-with(@name,'track')) and matches(@name,'.*:SupplierDate$','i')]">
        <meta name="track:SuppliedDate" content="{@content}"/>
    </xsl:template>

    <xsl:template match="meta[matches(@name,'.*:Guidelines$','i')]"/>

    <xsl:template match="meta/@scheme"/>

    <xsl:template match="head">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*|node()"/>
            <meta name="track:Guidelines" content="2015-1"/>
            <xsl:if test="not(meta/@name='dc:Source')">
                <xsl:variable name="source-element" select="(/dtbook/book/frontmatter/level1[tokenize(@class,'\s+')='colophon']//text()[matches(.,'^\s*IS[BS]N:?\s*([\dX –-]+)([^\dX –-]|$)')])[1]"/>
                <xsl:variable name="source-type" select="if (contains($source-element,'ISBN')) then 'isbn' else if (contains($source-element,'ISSN')) then 'issn' else ''"/>
                <xsl:variable name="source"
                    select="replace(replace(replace(replace(replace($source-element,'^\s*IS[BS]N:?\s*([\dX –-]+)([^\dX –-].*?$|$)','$1'),'[^\dX-]','-'),'-+','-'),'^-+',''),'-+$','')"/>
                <xsl:choose>
                    <xsl:when test="$source">
                        <meta name="dc:Source" content="urn:isbn:{$source}"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- ISBN is in a non-standard position => do a broader search in frontmatter -->
                        <xsl:variable name="source" select="string-join(/dtbook/book/frontmatter/level1//text(),' ')"/>
                        <xsl:variable name="source"
                            select="if (not(contains($source,'ISBN'))) then if (not(contains($source,'ISSN'))) then '' else substring-after($source,'ISSN') else substring-after($source,'ISBN')"/>
                        <xsl:variable name="source" select="if ($source='') then '' else replace($source,'^[:\s]*([\dX –-]+).*?$','$1','s')"/>
                        <xsl:variable name="source" select="if ($source='') then '' else replace(replace(replace($source,'[^\dX]','-','s'),'^-+',''),'-+$','')"/>
                        <xsl:choose>
                            <xsl:when test="$source">
                                <meta name="dc:Source" content="urn:isbn:{$source}"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:message select="'No ISBN or ISSN found, can''t create dc:Source !'"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="list/@type">
        <xsl:attribute name="type" select="'pl'"/>
    </xsl:template>

    <xsl:template match="list[@type='ul']/li//text()[normalize-space(.)!='']">
        <xsl:if test="normalize-space(string-join((ancestor::li[parent::list/@type='ul'])[1]//text() intersect preceding::text(),''))=''">
            <xsl:text>• </xsl:text>
        </xsl:if>
        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:template match="list[@type='ol']/li//text()[normalize-space(.)!='']">
        <xsl:variable name="li" select="(ancestor::li[parent::list/@type='ul'])[1]"/>
        <xsl:if test="normalize-space(string-join($li//text() intersect preceding::text(),''))=''">
            <xsl:variable name="number" select="count($li/preceding-sibling::li) + number(($li/parent::list/@start,1)[1])"/>
            <xsl:variable name="enum" select="string(($li/parent::list/@enum,'1')[1])"/>
            <xsl:choose>
                <xsl:when test="$enum='1'">
                    <xsl:value-of select="$number"/>
                </xsl:when>
                <xsl:when test="$enum='a'">
                    <xsl:value-of select="lower-case(pf:numeric-decimal-to-alpha(xs:integer($number)))"/>
                </xsl:when>
                <xsl:when test="$enum='A'">
                    <xsl:value-of select="upper-case(pf:numeric-decimal-to-alpha(xs:integer($number)))"/>
                </xsl:when>
                <xsl:when test="$enum='i'">
                    <xsl:value-of select="lower-case(pf:numeric-decimal-to-roman(xs:integer($number)))"/>
                </xsl:when>
                <xsl:when test="$enum='I'">
                    <xsl:value-of select="upper-case(pf:numeric-decimal-to-roman(xs:integer($number)))"/>
                </xsl:when>
            </xsl:choose>
        </xsl:if>
        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
    </xsl:template>

    <xsl:variable name="partition-type-classes" select="('cover','frontmatter','bodymatter','backmatter')"/>
    <xsl:variable name="division-type-classes"
        select="('abstract','acknowledgments','afterword','answers','appendix','assessments','assessment','bibliography','biographical-note','case-study','chapter','colophon','conclusion','contributors','copyright-page','credits','dedication','discography','division','editorial-note','epigraph','epilogue','errata','filmography','footnotes','foreword','glossary','grant-acknowledgment','halftitlepage','imprimatur','imprint','index-group','index-headnotes','index-legend','index','introduction','keywords','landmarks','loa','loi','lot','lov','notice','other-credits','page-list','part','practices','preamble','preface','prologue','promotional-copy','published-works','publisher-address','qna','rearnotes','revision-history','section','seriespage','subchapter','subsection','titlepage','toc-brief','toc','translator-note','volume')"/>
    <xsl:variable name="special-classes" select="('part','cover','colophon','nonstandardpagination')"/>
    <!-- 'part','cover','colophon','nonstandardpagination','jacketcopy','precedingemptyline','precedingseparator','byline','dateline' -->
    <xsl:variable name="allowed-classes" select="distinct-values(($partition-type-classes, $division-type-classes, $special-classes))"/>
    <xsl:template match="level1/@class | level2[parent::level1/tokenize(@class,'\s+')='part']/@class">
        <xsl:variable name="classes" select="tokenize(.,'\s+')"/>
        <xsl:variable name="classes"
            select="for $class in ($classes) return if ($class=('briefToc','level_toc','print_toc','print-toc')) then 'toc' else if ($class=('colophom','colphon')) then 'colophon' else if ($class='jacketcopy') then 'cover' else if ($class='halftitle-page') then 'halftitlepage' else if ($class='title-page') then 'titlepage' else $class"/>
        <xsl:variable name="classes" select="$classes[.=$allowed-classes]"/>
        <!--<xsl:variable name="classes" select="if (parent::level2) then $classes[not(.=$partition-type-classes)] else $classes"/>
        <xsl:variable name="classes" select="if (parent::level2) then ($classes, if (not($classes[.=$division-type-classes]) and not(ancestor::level1[tokenize(@class,'\s+')=$partition-type-classes[not(.='bodymatter')]])) then 'chapter' else ()) else $classes"/>-->
        <xsl:if test="count($classes)">
            <xsl:attribute name="class" select="string-join($classes,' ')"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="noteref/@class"/>
    <xsl:template match="note/@class"/>

</xsl:stylesheet>
