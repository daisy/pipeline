<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:h="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" exclude-result-prefixes="#all" version="2.0">

    <xsl:param name="content-dir" required="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="h:a">
        <xsl:variable name="a-href" select="tokenize(@href,'#')[1]"/>
        <xsl:variable name="a-fragment" select="if (contains(@href,'#')) then tokenize(@href,'#')[last()] else ''"/>
        <xsl:variable name="self-id" select="ancestor-or-self::*/@id"/>
        <xsl:choose>
            <xsl:when test="starts-with(@href,'#') or not(matches($a-href,'^[^/]+:')) and resolve-uri(replace($a-href,'\.html$','.xhtml'),$content-dir) = base-uri(/*)">
                <!-- is link to the same document -->
                <xsl:choose>
                    <xsl:when test="$a-fragment = ('',$self-id)">
                        <!-- is link to the same part of the document (or no part of the document); replace the link with a span -->
                        <span xmlns="http://www.w3.org/1999/xhtml">
                            <xsl:apply-templates select="(@* except (@href|@target|@rel|@hreflang|@type)) | node()"/>
                        </span>
                    </xsl:when>
                    <xsl:otherwise>
                        <!-- is link to another part of the document; only keep the fragment part of the href -->
                        <xsl:copy>
                            <xsl:apply-templates select="@*"/>
                            <xsl:attribute name="href" select="concat('#',$a-fragment)"/>
                            <xsl:apply-templates select="*|text()|processing-instruction()|comment()"/>
                        </xsl:copy>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <!-- links to another document; keep it as it is -->
                <xsl:copy>
                    <xsl:apply-templates select="@*|node()"/>
                </xsl:copy>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@class">
        <xsl:copy-of select="."/>
        <!--
            - I'm unsure about these: group, index-category, print-toc, section, sub-section, minor-head, optional-prodnote
            - page-front, page-normal, page-special: is there no way of distinguishing front/normal/special?
            - glossary: could glossterm and glossdef be inferred automatically?
        -->
        <xsl:variable name="types" select="for $class in tokenize(.,'\s+') return (
                                                if ($class='title') then 'title' else
                                                if ($class='jacket') then 'cover' else
                                                if ($class='front') then 'frontmatter' else
                                                if ($class='title-page') then 'titlepage' else
                                                if ($class='copyright-page') then 'copyright-page' else
                                                if ($class='acknowledgments') then 'acknowledgments' else
                                                if ($class='prolog') then 'prologue' else
                                                if ($class='introduction') then 'introduction' else
                                                if ($class='dedication') then 'dedication' else
                                                if ($class='foreword') then 'foreword' else
                                                if ($class='preface') then 'preface' else
                                                if ($class='print-toc') then 'toc' else
                                                if ($class='part') then 'part' else
                                                if ($class='chapter') then 'chapter' else
                                                if ($class='section') then 'subchapter' else
                                                if ($class='sub-section') then 'division' else
                                                if ($class='minor-head') then 'bridgehead' else
                                                if ($class='bibliography') then 'bibliography' else
                                                if ($class='glossary') then 'glossary' else
                                                if ($class='appendix') then 'appendix' else
                                                if ($class='index') then 'index' else
                                                if ($class='index-category') then 'index-category' else
                                                if ($class='sidebar') then 'sidebar' else
                                                if ($class='optional-prodnote') then 'annotation' else
                                                if ($class='noteref') then 'noteref' else
                                                if ($class='group') then () else
                                                if ($class='page-front') then 'pagebreak' else
                                                if ($class='page-normal') then 'pagebreak' else
                                                if ($class='page-special') then 'pagebreak' else ()
                                           )"/>
        <xsl:if test="$types">
            <xsl:attribute name="epub:type" select="string-join($types,' ')"/>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match="h:link">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="not(matches(@href,'^/+[^/]+:'))">
                <xsl:attribute name="href" select="replace(@href,'^(.*)\.html([\?#]|$)(.*)','$1.xhtml$2$3','i')"/>
            </xsl:if>
            <xsl:copy-of select="node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="h:meta">
        <xsl:choose>
            <xsl:when test=".[@http-equiv]">
                <xsl:if test="lower-case(@http-equiv)='content-type' and matches(@content,'charset=utf-8','i') and not(./parent::*/h:meta[@charset])">
                    <meta charset="utf-8" xmlns="http://www.w3.org/1999/xhtml"/>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:copy-of select="."/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
