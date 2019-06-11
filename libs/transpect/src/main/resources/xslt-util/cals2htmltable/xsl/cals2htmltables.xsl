<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:css="http://www.w3.org/1996/css"
  xmlns="http://www.w3.org/1999/xhtml"
  version="2.0"
  exclude-result-prefixes="xs xsl">

  <xsl:template match="@* | node()" mode="cals2html-table">
    <xsl:copy copy-namespaces="yes">
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="*:informaltable | *:table" mode="cals2html-table">
    <table>
      <xsl:apply-templates select="@*" mode="#current"/>
      <xsl:if test="exists(*:alt)">
        <xsl:attribute name="rendition"
          select="*:alt/*:inlinemediaobject/*:imageobject/*:imagedata/@fileref"/>
      </xsl:if>
      <xsl:apply-templates select="node()" mode="#current"/>
    </table>
  </xsl:template>

  <xsl:template match="*:informaltable/*:alt | *:table/*:alt" mode="cals2html-table"/>

  <xsl:template match="*:row" mode="cals2html-table">
    <tr>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </tr>
  </xsl:template>

  <xsl:template match="*:tgroup" mode="cals2html-table">
    <xsl:if test="exists(*:colspec) and (every $col in *:colspec satisfies ($col[@colwidth]))">
      <colgroup>
        <xsl:for-each select="*:colspec">
          <col css:width="{@colwidth}"/>
        </xsl:for-each>
      </colgroup>
    </xsl:if>
    <xsl:apply-templates select="node()" mode="#current"/>
  </xsl:template>

  <xsl:template match="*:tbody" mode="cals2html-table">
    <tbody>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </tbody>
  </xsl:template>

  <xsl:template match="*:thead" mode="cals2html-table">
    <thead>
      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </thead>
  </xsl:template>

  <xsl:template match="*:entry" mode="cals2html-table">
    <xsl:element name="{if (ancestor::*:thead) then 'th' else 'td'}">
      <xsl:if test="@namest">
        <!-- should be more robust than just relying on certain column name literals -->
        <xsl:attribute name="colspan"
          select="number(replace(@nameend, '^c(ol)?', '')) - number(replace(@namest, 'c(ol)?', '')) + 1"
        />
      </xsl:if>
      <xsl:if test="@morerows &gt; 0">
        <xsl:attribute name="rowspan" select="@morerows + 1"/>
      </xsl:if>
      <!--<xsl:apply-templates select="@srcpath, @class, @style, @align, @valign" mode="#current"/>-->

      <xsl:apply-templates select="@*, node()" mode="#current"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="*:colspec" mode="cals2html-table"/>

  <xsl:template match="@morerows" mode="cals2html-table"/>
  <xsl:template match="@nameend" mode="cals2html-table"/>
  <xsl:template match="@namest" mode="cals2html-table"/>
  <xsl:template match="@colname" mode="cals2html-table"/>
  <xsl:template match="@frame" mode="cals2html-table"/>

</xsl:stylesheet>
