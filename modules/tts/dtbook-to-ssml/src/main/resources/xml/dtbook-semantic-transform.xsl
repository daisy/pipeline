<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    xmlns:dt="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:pho="http://braillenet.org/phonetic"
    exclude-result-prefixes="#all"
    version="2.0">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <!-- Create a new TTS sentence which contains the @alt attribute of the images. -->
  <xsl:template match="dt:img">
    <xsl:copy>
      <xsl:copy-of select="@* except @id"/>
      <xsl:if test="@alt">
	<ssml:s id="{@id}"><xsl:value-of select="@alt"/></ssml:s>
      </xsl:if>
      <xsl:apply-templates select="node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="dt:span[@pho:ipa]|dt:span[@pho:sampa]">
    <xsl:copy>
      <xsl:copy-of select="@*[not(name()='pho:ipa') and not(name()='pho:sampa')]" />
      <ssml:phoneme>
        <xsl:choose>
          <xsl:when test="@pho:sampa">
            <xsl:attribute name="ssml:alphabet">sampa</xsl:attribute>
            <xsl:attribute name="ssml:ph"><xsl:value-of select="@pho:sampa"/></xsl:attribute>
          </xsl:when>
          <xsl:otherwise>
            <xsl:attribute name="ssml:alphabet">ipa</xsl:attribute>
            <xsl:attribute name="ssml:ph"><xsl:value-of select="@pho:ipa"/></xsl:attribute>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:copy-of select="text()" />
      </ssml:phoneme>
    </xsl:copy>
  </xsl:template>

  <!-- Add extra white spaces around elements that are sometimes badly juxtaposed. -->
  <xsl:template match="dt:abbr|dt:acronym|dt:br|dt:a">
    <xsl:value-of select="' '"/>
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
    <xsl:value-of select="' '"/>
  </xsl:template>

  <!-- Those elements can't have <w> in them according to the spec, so we add the <w> here -->
  <xsl:template match="dt:pagenum|dt:noteref|dt:annoref|dt:linenum">
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:choose>
	<xsl:when test="count(*) = 0">
	  <dt:w><xsl:copy-of select="text()"/></dt:w>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:copy-of select="node()"/>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <!-- Commented because it is only relevant for old DTBooks: -->
  <!-- Wrap links with words in order to make sure they can be matched against lexicon entries. -->
  <!-- (provided that DTBook links cannot contain words, for old versions of the DTBook spec) -->
  <!-- <xsl:template match="dt:a"> -->
  <!--   <xsl:value-of select="' '"/> -->
  <!--   <xsl:copy> -->
  <!--     <xsl:copy-of select="@*"/> -->
  <!--     <xsl:choose> -->
  <!-- 	<xsl:when test="not(ancestor::*[local-name() = 'w'][1])"> -->
  <!-- 	  <dt:w><xsl:apply-templates select="node()"/></dt:w> -->
  <!-- 	</xsl:when> -->
  <!-- 	<xsl:otherwise> -->
  <!-- 	  <xsl:apply-templates select="node()"/> -->
  <!-- 	</xsl:otherwise> -->
  <!--     </xsl:choose> -->
  <!--   </xsl:copy> -->
  <!--   <xsl:value-of select="' '"/> -->
  <!-- </xsl:template> -->

  <!-- Detect Roman numerals. While it would be useful to make this
       code available for other formats than DTBook (i.e. XHTML and
       ZedAi), the algorithm to iterate over the titles of the same
       level can be quite different from one format to another, XHTML
       being less strict than DTBook regarding headers' positioning. -->
  <xsl:variable name="headers" select="('levelhd', 'hd', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6')"/>
  <xsl:template match="dt:w[count(*) = 0]">
    <xsl:variable name="text" select="dt:normalize(text())"/>
    <xsl:copy>
      <xsl:copy-of select="@*"/>
      <xsl:choose>
	<xsl:when test="not(dt:is-numeral($text))">
	  <xsl:apply-templates select="node()"/>
	</xsl:when>
	<xsl:otherwise>
	  <xsl:variable name="header" select="ancestor::*[$headers = local-name()][1]"/>
	  <xsl:choose>
	    <xsl:when test="not($header)">
	      <xsl:apply-templates select="node()"/>
	    </xsl:when>
	    <xsl:otherwise>
	      <xsl:variable name="level" select="$header/ancestor::*[dt:is-level(.)][1]"/>
	      <xsl:choose>
		<xsl:when test="($text = 'i' and
				dt:is-numeral(dt:normalize(dt:title-prefix($level/following-sibling::*[dt:is-level(.)][1])))) or
				dt:is-numeral(dt:normalize(dt:title-prefix($level/preceding-sibling::*[dt:is-level(.)][1])))">
		  <ssml:say-as interpret-as="cardinal">
		    <xsl:value-of select="upper-case($text)"/>  <!-- alternatively, it could be converted to an Arabic numeral -->
		  </ssml:say-as>
		</xsl:when>
		<xsl:otherwise>
		  <xsl:apply-templates select="node()"/>
		</xsl:otherwise>
	      </xsl:choose>
	    </xsl:otherwise>
	  </xsl:choose>
	</xsl:otherwise>
      </xsl:choose>
    </xsl:copy>
  </xsl:template>

  <xsl:function name="dt:normalize">
    <xsl:param name="nodes"/>
    <xsl:analyze-string select="lower-case(string-join($nodes, ''))" regex="^(.+)[).]?$">
      <xsl:matching-substring>
	<xsl:value-of select="regex-group(1)"/>
      </xsl:matching-substring>
      <xsl:non-matching-substring/>
    </xsl:analyze-string>
  </xsl:function>

  <xsl:function name="dt:title-prefix">
    <xsl:param name="level"/>
    <xsl:value-of select="(($level//*[$headers = local-name()])[1]//dt:w)[1]/text()"/>
  </xsl:function>


  <xsl:function name="dt:is-level" as="xs:boolean">
    <xsl:param name="node"/>
    <xsl:value-of select="starts-with(local-name($node), 'level')"/>
  </xsl:function>


  <!-- there is also a similar function in common-utils -->
  <xsl:function name="dt:is-numeral" as="xs:boolean">
    <xsl:param name="normalized-word"/>
    <xsl:value-of select="matches($normalized-word, '^[ivxclm]+$')"/>
  </xsl:function>

</xsl:stylesheet>
