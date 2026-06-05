<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                xmlns="http://www.daisy.org/z3986/2005/dtbook/"
                xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

  <xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

  <xsl:param name="output-base-uri" required="yes"/>

  <xsl:variable name="mathml-formulae-img"
                select="collection()[2]//d:file[1]
                        /pf:relativize-uri(resolve-uri(@href,base-uri(.)),$output-base-uri)"/>

  <xsl:variable name="title" select="(//meta[@name='dc:Title'])[1]"/>
  <xsl:variable name="safe-title">
    <xsl:choose>
      <xsl:when test="not($title) or $title/@content = ''">
	<xsl:value-of select="'Content'"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="$title/@content"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:variable>

  <xsl:template match="bodymatter" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:variable name="first-level" select="(.//level|.//level1)[1]"/>
      <xsl:choose>
	<xsl:when test="not($first-level)">
	  <!-- Case 1: no levels at all -->
	  <level1>
	    <h1 id="faux-heading"><xsl:value-of select="$safe-title"/></h1>
	    <xsl:apply-templates select="node()"/>
	  </level1>
	</xsl:when>
	<xsl:when test="not((.//hd)[1]|(.//h1)[1]|(.//h2)[1]|(.//h3)[1]|(.//h4)[1]|(.//h5)[1]|(.//h6)[1])">
	  <!-- Case 2: no headings at all -->
	  <xsl:apply-templates select="node()" mode="find-level">
	    <xsl:with-param name="first-level" select="$first-level"/>
	  </xsl:apply-templates>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates select="node()"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="book" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="not(child::frontmatter)">
	<frontmatter>
	  <xsl:if test="not(//doctitle)">
	    <xsl:call-template name="add-doc-title"/>
	  </xsl:if>
	</frontmatter>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="frontmatter" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="not(//doctitle)">
	<xsl:call-template name="add-doc-title"/>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template name="add-doc-title">
    <doctitle><xsl:value-of select="$safe-title"/></doctitle>
  </xsl:template>

  <xsl:template match="level|level1" priority="2" mode="find-level">
    <xsl:param name="first-level" select="()"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:choose>
	<xsl:when test="$first-level = .">
	  <xsl:apply-templates select="." mode="add-heading"/>
	  <xsl:apply-templates select="node()"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:apply-templates select="node()" mode="find-level">
	    <xsl:with-param name="first-level" select="$first-level"/>
	  </xsl:apply-templates>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="level" mode="add-heading">
    <hd id="faux-heading">Section</hd>
  </xsl:template>

  <xsl:template match="level1" mode="add-heading">
    <h1 id="faux-heading">Section</h1>
  </xsl:template>

  <xsl:template match="node()|@*" priority="1" mode="find-level">
    <xsl:param name="first-level" select="''"/>
    <xsl:copy>
      <xsl:apply-templates select="node()|@*" mode="find-level">
	<xsl:with-param name="first-level" select="$first-level"/>
      </xsl:apply-templates>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="m:math" priority="2">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:if test="not(@alttext)">
	<!-- TODO: serialize the MathML -->
	<xsl:attribute name="alttext">
	  <xsl:text>Math Formulae</xsl:text>
	</xsl:attribute>
      </xsl:if>
      <xsl:if test="not(@altimg) and $mathml-formulae-img != ''">
	<xsl:attribute name="altimg">
	  <xsl:value-of select="$mathml-formulae-img"/>
	</xsl:attribute>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="node()|@*" priority="1">
    <xsl:copy>
      <xsl:apply-templates select="node()|@*"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
