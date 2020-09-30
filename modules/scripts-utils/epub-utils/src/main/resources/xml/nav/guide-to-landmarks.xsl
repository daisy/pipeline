<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:epub="http://www.idpf.org/2007/ops"
                xmlns="http://www.w3.org/1999/xhtml"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

  <xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

  <xsl:param name="output-base-uri" required="yes"/>

  <xsl:template match="/*">
    <xsl:variable name="opf-base" select="pf:base-uri(.)"/>
    <xsl:for-each select="//guide[1]">
      <nav epub:type="landmarks">
        <h1>Guide</h1>
        <ol>
          <xsl:for-each select="descendant::reference">
            <li>
              <a href="{pf:relativize-uri(resolve-uri(@href,$opf-base),$output-base-uri)}">
                <xsl:attribute name="epub:type">
                  <xsl:choose>
                    <xsl:when test="@type='title-page'">
                      <xsl:sequence select="'titlepage'"/>
                    </xsl:when>
                    <xsl:when test="@type=('acknowledgements',   'glossary',
                                           'bibliography',       'index',
                                           'colophon',           'loi',
                                           'copyright-page',     'lot',
                                           'cover',              'notes',
                                           'dedication',         'preface',
                                           'epigraph',           'text',
                                           'foreword',           'toc'
                                           )">
                      <xsl:sequence select="@type"/>
                    </xsl:when>
                    <xsl:when test="starts-with(@type,'other.')">
                      <xsl:sequence select="substring-after(@type,'other.')"/>
                    </xsl:when>
                    <xsl:otherwise/>
                  </xsl:choose>
                </xsl:attribute>
                <xsl:variable name="type" select="replace(@type,'^other\.','')"/>
                <xsl:choose>
                  <xsl:when test="@title">
                    <xsl:value-of select="@title"/>
                  </xsl:when>
                  <xsl:when test="$type='toc'">
                    <xsl:text>Table of contents</xsl:text>
                  </xsl:when>
                  <xsl:when test="$type='loi'">
                    <xsl:text>List of illustrations</xsl:text>
                  </xsl:when>
                  <xsl:when test="$type='lot'">
                    <xsl:text>List of tables</xsl:text>
                  </xsl:when>
                  <xsl:when test="$type='text'">
                    <xsl:text>Start of content</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:value-of select="concat(upper-case(substring($type,1,1)),
                                                 replace(substring($type,2),'-',' '))"/>
                  </xsl:otherwise>
                </xsl:choose>
              </a>
            </li>
          </xsl:for-each>
        </ol>
      </nav>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>

