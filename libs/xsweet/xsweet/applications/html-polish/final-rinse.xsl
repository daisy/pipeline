<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xsw="http://coko.foundation/xsweet"
  xmlns="http://www.w3.org/1999/xhtml"
  xpath-default-namespace="http://www.w3.org/1999/xhtml"

  exclude-result-prefixes="#all">

<!-- XSweet: After everything, certain efficiencies can still be 'squeezed' out of presentational HTML markup as emitted raw. So, some final normalizations can be made opportunistically. -->
  
  <!-- Removes redundant tagging from HTML based on @style analysis, element type e.g. redundant b, i, u etc. -->

  <xsl:output method="xml" indent="no" omit-xml-declaration="yes"/>

<xsl:template match="html/head">
  <xsl:copy>
    <xsl:for-each select="(//h1 | //h2 | //h3)[1]">
      <title>
        <xsl:value-of select="."/>
      </title>
    </xsl:for-each>
    <xsl:apply-templates/>
  </xsl:copy>
</xsl:template>
  
<xsl:template match="html/head/title"/>

  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="style" priority="11">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:value-of select="replace(string(.),'xsweet','-xsweet')"/>
    </xsl:copy>
  </xsl:template>

  <!-- Disable when auto-indenting - this introduces cosmetic whitespace into
       an assumed text-brick. -->
  <xsl:template match="head | head//* | body | body/* | p | h1 | h2 | h3 | h4 | h5 | h6" priority="10">
    <xsl:text>&#xA;</xsl:text>
    <xsl:next-match/>
  </xsl:template>

  <!-- Insert a comment into any empty div or p so as not to confuse poor HTML parsers. -->
  <xsl:template match="div | p | h1 | h2 | h3 | h4 | h5 | h6">
    <xsl:copy>
      <xsl:apply-templates select="node() | @*"/>
      <xsl:if test="empty(*) and not(matches(.,'\S'))">
        <xsl:comment> empty </xsl:comment>
      </xsl:if>
    </xsl:copy>
  </xsl:template>


  <xsl:template match="span[@class=('EndnoteReference','FootnoteReference')]">
    <!-- These spans sometimes contain noise from input, in addition to a (generated) endnote or footnote reference.  -->
    <!-- Note the named style assignment is directly coded in the Word -->
<!--   <span class="EndnoteReference"><a class="endnoteReference" href="#en5">5</a>6</span>"-->
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <!-- Remove any 'p' element that is truly empty - nothing but whitespace, no elements.
       (Empty inline elements were stripped by generic logic: see scrub.xsl.) -->
  <!--<xsl:template match="p[not(matches(.,'\S'))][empty(*)]"/>-->

  <!-- Rewrite @style to remove properties duplicative of inherited properties -->

  <xsl:template match="@style">
    <xsl:variable name="here" select=".."/>
    <!-- Any CSS properties not declared on an ancestor are significant. -->
    <xsl:variable name="significant" as="xs:string*">
      <xsl:for-each select="tokenize(.,'\s*;\s*')">
        <xsl:variable name="prop" select="."/>
        <xsl:variable name="propName" select="replace($prop,':.*$','')"/>
        <!-- the property is redundant if the same as the same property on the closest element with the property -->
        <xsl:variable name="redundant" select="$here/ancestor::*[contains(@style,$propName)][1]/tokenize(@style,'\s*;\s*') = $prop"/>
        <xsl:if test="not($redundant)">
          <!-- We have some (pseudo) properties named 'xsweet' these are rewritten for CSS -->
          <xsl:sequence select="replace($prop,'\s*^xsweet','-xsweet')"/>
        </xsl:if>
      </xsl:for-each>
    </xsl:variable>

    <!-- Only if we have an item in sequence $significant (a sequence of strings) do we produce a new @style. -->
    <xsl:if test="exists($significant)">
      <xsl:attribute name="style">
        <xsl:value-of select="$significant" separator="; "/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template match="span[empty(@style|@class)]">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="span">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:apply-templates select="@style"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="tab">
    <span class="tab">&#x9;<xsl:comment> tab </xsl:comment></span>
  </xsl:template>

  <xsl:template match="b[ancestor::*[contains(@style,'font-weight')][1]/tokenize(@style,'\s*;\s*') = 'font-weight: bold']">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="i[ancestor::*[contains(@style,'font-style')][1]/tokenize(@style,'\s*;\s*') = 'font-style: italic']">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="u[ancestor::*[contains(@style,'text-decoration')][1]/tokenize(@style,'\s*;\s*') = 'text-decoration: underline']">
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="span[@style='font-style: italic']">
    <i>
      <xsl:apply-templates/>
    </i>
  </xsl:template>

  <xsl:template match="span[@style='font-weight: bold']">
    <b>
      <xsl:apply-templates/>
    </b>
  </xsl:template>

  <xsl:template match="span[@style='text-decoration: underline']">
    <u>
      <xsl:apply-templates/>
    </u>
  </xsl:template>


  <xsl:template match="b/b | i/i | u/u" priority="5">
    <xsl:apply-templates/>
  </xsl:template>

</xsl:stylesheet>
