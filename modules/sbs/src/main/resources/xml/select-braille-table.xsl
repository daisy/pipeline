<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
    xmlns:my="http://my-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="dtb my" extension-element-prefixes="my">

  <!-- =============== -->
  <!-- Table selection -->
  <!-- =============== -->

  <xsl:param name="contraction-grade" required="yes"/>
  <xsl:param name="hyphenation">false</xsl:param>
  <xsl:param name="enable_capitalization" select="false()"/>
  <xsl:param name="accented-letters">de-accents-ch</xsl:param>
  <xsl:param name="use_local_dictionary" select="false()"/>
  <xsl:param name="document-identifier"></xsl:param>
  <xsl:param name="TABLE_BASE_URI"></xsl:param>
  
  <xsl:function name="my:get-contraction" as="xs:string">
    <xsl:param name="context"/>
    <xsl:sequence
	select="if ($context/ancestor-or-self::dtb:span[@brl:grade and @brl:grade &lt; $contraction-grade])
		then $context/ancestor-or-self::dtb:span/@brl:grade
		else if (lang('de',$context))
		then string($contraction-grade)
		else '0'"/>
  </xsl:function>

  <xsl:function name="my:get-tables" as="xs:string">
    <xsl:param name="ctx"/>
    <xsl:param name="context"/>
    <xsl:call-template name="my:get-tables">
      <xsl:with-param name="ctx" select="$ctx"/>
      <xsl:with-param name="context" select="$context"/>
    </xsl:call-template>
  </xsl:function>
  
  <xsl:template name="my:get-tables" as="xs:string">
    <xsl:param name="ctx" required="no" select="."/>
    <xsl:param name="context" required="no" select="local-name()"/>
    <xsl:param name="hyphenation" as="xs:boolean" tunnel="yes" select="$hyphenation='true'"/>
    <!-- handle explicit setting of the contraction -->
    <xsl:variable name="actual_contraction" select="my:get-contraction($ctx)"/>
    <xsl:variable name="result">
    <xsl:value-of
	select="concat($TABLE_BASE_URI,
		string-join((
		'sbs.dis',
		'sbs-de-core6.cti',
		'sbs-de-accents.cti',
		'sbs-special.cti',
		'sbs-whitespace.mod',
		if ($context = 'v-form' or $context = 'name_capitalized' or ($actual_contraction != '2' and $enable_capitalization = true())) then 'sbs-de-capsign.mod' else '',
		if ($actual_contraction = '2' and not($context=('num_roman','abbr','date_month','date_day','name_capitalized'))) then 'sbs-de-letsign.mod' else '',
		if (not($context = 'date_month' or $context = 'denominator' or $context = 'index' or $context = 'linenum')) then 'sbs-numsign.mod' else '',
		if ($context = 'num_ordinal' or $context = 'date_day' or $context = 'denominator' or $context = 'index') then 'sbs-litdigit-lower.mod' else 'sbs-litdigit-upper.mod',
		if ($context != 'date_month' and $context != 'date_day') then 'sbs-de-core.mod' else '',
		if ($context = 'name_capitalized' or $context = 'num_roman' or ($context = 'abbr' and not(my:containsDot($ctx))) or ($actual_contraction &lt;= '1' and $context != 'date_day' and $context != 'date_month')) then 'sbs-de-g0-core.mod' else '',
		if ($actual_contraction = '1' and $context != 'num_roman' and ($context != 'name_capitalized' and ($context != 'abbr' or my:containsDot($ctx)) and $context != 'date_month' and $context != 'date_day')) then string-join((if ($use_local_dictionary = true()) then concat('sbs-de-g1-white-',$document-identifier,'.mod,') else '', 'sbs-de-g1-white.mod', 'sbs-de-g1-core.mod')[. != ''],',') else ''
		)[. != ''], ','))"/>
    <xsl:text>,</xsl:text>
    <xsl:if test="$actual_contraction = '2' and $context != 'num_roman'">
      <xsl:if test="$context = 'place'">
        <xsl:if test="$use_local_dictionary = true()">
          <xsl:value-of select="concat('sbs-de-g2-place-white-',$document-identifier,'.mod,')"/>
        </xsl:if>
        <xsl:text>sbs-de-g2-place-white.mod,</xsl:text>
        <xsl:text>sbs-de-g2-place.mod,</xsl:text>
      </xsl:if>
      <xsl:if test="$context = 'place' or $context = 'name'">
        <xsl:if test="$use_local_dictionary = true()">
          <xsl:value-of select="concat('sbs-de-g2-name-white-',$document-identifier,'.mod,')"/>
        </xsl:if>
        <xsl:text>sbs-de-g2-name-white.mod,</xsl:text>
        <xsl:text>sbs-de-g2-name.mod,</xsl:text>
      </xsl:if>
      <xsl:if
        test="$context != 'name' and $context != 'name_capitalized' and $context != 'place' and ($context != 'abbr' or  my:containsDot($ctx)) and $context != 'date_day' and $context != 'date_month'">
        <xsl:if test="$use_local_dictionary = true()">
          <xsl:value-of select="concat('sbs-de-g2-white-',$document-identifier,'.mod,')"/>
        </xsl:if>
        <xsl:text>sbs-de-g2-white.mod,</xsl:text>
        <xsl:text>sbs-de-g2-core.mod,</xsl:text>
      </xsl:if>
    </xsl:if>
    <xsl:choose>
      <xsl:when test="not($hyphenation)">
        <xsl:text>sbs-de-hyph-none.mod,</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="lang('de-1901',$ctx)">
            <xsl:text>sbs-de-hyph-old.mod,</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>sbs-de-hyph-new.mod,</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="$context != 'date_month' and $context != 'date_day'">
      <xsl:choose>
        <xsl:when test="$ctx/ancestor-or-self::dtb:span[@brl:accents = 'reduced']">
          <xsl:text>sbs-de-accents-reduced.mod,</xsl:text>
        </xsl:when>
        <xsl:when test="$ctx/ancestor-or-self::dtb:span[@brl:accents = 'detailed']">
          <xsl:text>sbs-de-accents.mod,</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <!-- no local accents are defined -->
          <xsl:choose>
            <xsl:when test="$accented-letters = 'de-accents-ch'">
              <xsl:text>sbs-de-accents-ch.mod,</xsl:text>
            </xsl:when>
            <xsl:when test="$accented-letters = 'de-accents-reduced'">
              <xsl:text>sbs-de-accents-reduced.mod,</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:text>sbs-de-accents.mod,</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
    <xsl:text>sbs-special.mod</xsl:text>
  </xsl:variable>
  <xsl:sequence select="$result"/>
  </xsl:template>

</xsl:stylesheet>
