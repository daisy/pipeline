<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:msxsl="urn:schemas-microsoft-com:xslt" xmlns:x="http://www.daisy.org/z3986/2005/dtbook/"
  exclude-result-prefixes="x msxsl">
  <xsl:output method="xml" indent="yes" doctype-system="dtbook110.dtd" encoding="utf-8"/>

  <!--Version 0.7 - 2015.04.20-->

  <xsl:template match="/">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="x:dtbook">
    <dtbook version="1.1.0">
      <xsl:apply-templates/>
    </dtbook>
  </xsl:template>

  <xsl:template match="x:a">
    <a>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </a>
  </xsl:template>

  <xsl:template match="x:abbr">
    <abbr>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </abbr>
  </xsl:template>

  <xsl:template match="x:acronym">
    <acronym>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </acronym>
  </xsl:template>

  <xsl:template match="x:annoref">
    <annoref>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </annoref>
  </xsl:template>

  <xsl:template match="x:blockqoute">
    <div class="blockquote">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="x:book">
    <book>
      <xsl:if test="parent::x:dtbook/@xml:lang">
        <xsl:attribute name="lang">
          <xsl:value-of select="parent::x:dtbook/@xml:lang"/>
        </xsl:attribute>
      </xsl:if>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </book>
  </xsl:template>

  <xsl:template match="x:bodymatter">
    <bodymatter>
      <xsl:apply-templates/>
    </bodymatter>
  </xsl:template>

  <xsl:template match="x:br">
    <br>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </br>
  </xsl:template>

  <xsl:template match="x:bridgehead">
    <p class="bridgehead">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <xsl:template match="x:caption">
    <xsl:choose>
      <xsl:when test="parent::x:imggroup">
        <prodnote class="caption" render="required">
          <xsl:choose>
            <xsl:when test="child::x:p|child::x:table|child::x:div">
              <xsl:apply-templates/>
            </xsl:when>
            <xsl:otherwise>
              <p>
                <xsl:apply-templates/>
              </p>
            </xsl:otherwise>
          </xsl:choose>

        </prodnote>
      </xsl:when>
      <xsl:otherwise>
        <caption>
          <xsl:for-each select="@*">
            <xsl:attribute name="{name()}">
              <xsl:value-of select="."/>
            </xsl:attribute>
          </xsl:for-each>
          <xsl:apply-templates/>
        </caption>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="x:code">
    <code>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </code>
  </xsl:template>

  <xsl:template match="x:colgroup">
    <colgroup>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </colgroup>
  </xsl:template>

  <xsl:template match="x:dd">
    <dd>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </dd>
  </xsl:template>

  <xsl:template match="x:dl">
    <dl>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </dl>
  </xsl:template>

  <xsl:template match="x:docauthor">
    <docauthor>
      <xsl:apply-templates/>
    </docauthor>
  </xsl:template>

  <xsl:template match="x:doctitle">
    <doctitle>
      <xsl:apply-templates/>
    </doctitle>
  </xsl:template>

  <xsl:template match="x:dt">
    <dt>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </dt>
  </xsl:template>

  <xsl:template match="x:em">
    <em>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </em>
  </xsl:template>

  <xsl:template match="x:frontmatter">
    <frontmatter>
      <xsl:apply-templates/>
    </frontmatter>
  </xsl:template>

  <xsl:template match="x:h1">
    <levelhd depth="1">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </levelhd>
  </xsl:template>

  <xsl:template match="x:h2">
    <levelhd depth="2">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </levelhd>
  </xsl:template>

  <xsl:template match="x:h3">
    <levelhd depth="3">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </levelhd>
  </xsl:template>

  <xsl:template match="x:h4">
    <levelhd depth="4">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </levelhd>
  </xsl:template>

  <xsl:template match="x:h5">
    <levelhd depth="5">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </levelhd>
  </xsl:template>

  <xsl:template match="x:h6">
    <levelhd depth="6">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </levelhd>
  </xsl:template>

  <xsl:template match="x:head">
    <head>
      <title>
        <xsl:value-of select="string(x:meta[@name='dc:Title']/@content)"/>
      </title>
      <meta name="prod:AutoBrailleReady" content="yes"/>
      <xsl:apply-templates/>
    </head>
  </xsl:template>

  <xsl:template match="x:img">
    <img>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </img>
  </xsl:template>

  <xsl:template match="x:imggroup">
    <imggroup>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </imggroup>
  </xsl:template>

  <xsl:template match="x:kbd">
    <kbd>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </kbd>
  </xsl:template>

  <xsl:template match="x:level1">
    <level depth="1">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </level>
  </xsl:template>

  <xsl:template match="x:level6">
    <level depth="6">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </level>
  </xsl:template>

  <xsl:template match="x:level2">
    <level depth="2">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </level>
  </xsl:template>

  <xsl:template match="x:level5">
    <level depth="5">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </level>
  </xsl:template>

  <xsl:template match="x:level3">
    <level depth="3">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </level>
  </xsl:template>

  <xsl:template match="x:level4">
    <level depth="4">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </level>
  </xsl:template>

  <xsl:template match="x:li">
    <li>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </li>
  </xsl:template>

  <xsl:template match="x:lic">
    <lic class="pageref">
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </lic>
  </xsl:template>

  <xsl:template match="x:list">
    <xsl:choose>
      <xsl:when test="string(@type)='pl'">

        <!--Check om første li har &#x25A0; eller &#x2022; som første karakter-->

        <xsl:choose>
          <xsl:when test="contains(string(child::x:li),'&#x25A0;')">
            <list type="ul" bullet="none">
              <xsl:apply-templates/>
            </list>
          </xsl:when>

          <xsl:when test="contains(string(child::x:li),'&#x2022;')">
            <list type="ul" bullet="none">
              <xsl:apply-templates/>
            </list>
          </xsl:when>

          <xsl:otherwise>
            <list type="ul">
              <xsl:apply-templates/>
            </list>
          </xsl:otherwise>
        </xsl:choose>

      </xsl:when>

      <xsl:when test="string(@type)='ul'">
        <list type="ul">
          <xsl:apply-templates/>
        </list>
      </xsl:when>
      <xsl:otherwise>
        <list type="ol">
          <xsl:apply-templates/>
        </list>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="x:linegroup">
    <div class="stanza">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="x:line">
    <line>
      <xsl:apply-templates/>
    </line>
  </xsl:template>

  <xsl:template match="x:meta">
    <xsl:element name="meta">
      <xsl:attribute name="name">
        <xsl:value-of select="string(@name)"/>
      </xsl:attribute>
      <xsl:attribute name="content">
        <xsl:value-of select="string(@content)"/>
      </xsl:attribute>
    </xsl:element>
  </xsl:template>

  <xsl:template match="x:noteref">
    <noteref>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="translate(.,'#','')"/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </noteref>
  </xsl:template>

  <xsl:template match="x:note">
    <note>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </note>
  </xsl:template>

  <xsl:template match="x:p">
    <p>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </p>
  </xsl:template>

  <!-- if pagenum is child of imggroup report-->
  <xsl:template match="x:pagenum">
    <xsl:choose>

      <!--Parent er imggroup: indsæt kommentar-->
      <xsl:when test="parent::x:imggroup">
        <xsl:comment>
          <xsl:text>Konverteringsproblem: pagenum kan ikke befinde sig på denne position: </xsl:text>
          <xsl:text>&lt;pagenum id='</xsl:text>
          <xsl:value-of select="@id"/>
          <xsl:text>'&gt;</xsl:text>
          <xsl:variable name="nodeAsStr" select="string(.)"/>
          <xsl:copy-of select="$nodeAsStr" exclude-result-prefixes="#all"/>
          <xsl:text>&lt;/pagenum&gt;</xsl:text>
        </xsl:comment>
      </xsl:when>

      <xsl:otherwise>
        <pagenum>
          <xsl:for-each select="@*">
            <xsl:attribute name="{name()}">
              <xsl:value-of select="."/>
            </xsl:attribute>
          </xsl:for-each>
          <xsl:apply-templates/>
        </pagenum>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="x:poem">
    <div class="poem">
      <xsl:apply-templates/>
    </div>
  </xsl:template>

  <xsl:template match="x:prodnote">
    <prodnote>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </prodnote>
  </xsl:template>

  <xsl:template match="x:q">
    <q>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </q>
  </xsl:template>

  <xsl:template match="x:rearmatter">
    <rearmatter>
      <xsl:apply-templates/>
    </rearmatter>
  </xsl:template>

  <xsl:template match="x:samp">
    <samp>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </samp>
  </xsl:template>

  <xsl:template match="x:sent">
    <sent>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </sent>
  </xsl:template>

  <!--Attributten render er forbudt i 110-->

  <xsl:template match="x:sidebar">
    <sidebar>

      <xsl:if test="@id">
        <xsl:attribute name="id">
          <xsl:value-of select="@id"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:if test="@class">
        <xsl:attribute name="class">
          <xsl:value-of select="@class"/>
        </xsl:attribute>
      </xsl:if>

      <xsl:apply-templates/>
    </sidebar>
  </xsl:template>

  <xsl:template match="x:span">
    <span>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </span>
  </xsl:template>

  <xsl:template match="x:strong">
    <strong>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </strong>
  </xsl:template>

  <xsl:template match="x:sub">
    <sub>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </sub>
  </xsl:template>

  <xsl:template match="x:sup">
    <sup>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </sup>
  </xsl:template>

  <xsl:template match="x:table">
    <xsl:apply-templates select="x:pagenum"/>
    <table>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates select="node()[not(self::x:pagenum)]|@*"/>
    </table>
  </xsl:template>

  <xsl:template match="x:tbody">
    <tbody>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </tbody>
  </xsl:template>

  <xsl:template match="x:td">
    <td>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </td>
  </xsl:template>

  <xsl:template match="x:tfoot">
    <tfoot>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </tfoot>
  </xsl:template>

  <xsl:template match="x:th">
    <th>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </th>
  </xsl:template>

  <xsl:template match="x:thead">
    <thead>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </thead>
  </xsl:template>

  <xsl:template match="x:tr">
    <tr>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </tr>
  </xsl:template>

  <xsl:template match="x:w">
    <w>
      <xsl:for-each select="@*">
        <xsl:attribute name="{name()}">
          <xsl:value-of select="."/>
        </xsl:attribute>
      </xsl:for-each>
      <xsl:apply-templates/>
    </w>
  </xsl:template>

</xsl:stylesheet>
