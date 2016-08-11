<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet
    version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
    xmlns:my="http://my-functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:math="http://www.w3.org/1998/Math/MathML"
    exclude-result-prefixes="dtb my" extension-element-prefixes="my">

  <!-- =========================== -->
  <!-- Queries for block vs inline -->
  <!-- =========================== -->
  
  <xsl:function name="my:is-block-element" as="xs:boolean">
    <xsl:param name="node" as="node()"/>
    <xsl:apply-templates select="$node" mode="is-block-element"/>
  </xsl:function>
  
  <xsl:template match="node()" as="xs:boolean" mode="is-block-element" priority="10">
    <xsl:sequence select="false()"/>
  </xsl:template>
  
  <xsl:template match="dtb:*|math:*" as="xs:boolean" mode="is-block-element" priority="11">
    <xsl:sequence select="false()"/>
  </xsl:template>
  
  <xsl:template match="dtb:samp|dtb:cite" as="xs:boolean" mode="is-block-element" priority="12">
    <xsl:sequence select="if (parent::*[self::dtb:p|self::dtb:li|self::dtb:td|self::dtb:th]) then false() else true()"/>
  </xsl:template>

  <xsl:template match="dtb:h1|dtb:h2|dtb:h3|dtb:h4|dtb:h5|dtb:h6|dtb:p|dtb:list|dtb:li|dtb:author|dtb:byline|dtb:line|dtb:imggroup|dtb:blockquote" as="xs:boolean" mode="is-block-element" priority="12">
    <xsl:sequence select="true()"/>
  </xsl:template>

  <xsl:function name="my:following-textnode-within-block" as="text()?">
    <xsl:param name="context"/>
    <xsl:sequence select="$context/following::text()[1] intersect $context/ancestor-or-self::*[my:is-block-element(.)][1]//text()"/>
  </xsl:function>
  
  <xsl:function name="my:preceding-textnode-within-block" as="text()?">
    <xsl:param name="context"/>
    <xsl:sequence select="$context/preceding::text()[1] intersect $context/ancestor-or-self::*[my:is-block-element(.)][1]//text()"/>
  </xsl:function>
  
  <xsl:function name="my:isLower" as="xs:boolean">
    <xsl:param name="char"/>
    <xsl:value-of select="$char=lower-case($char)"/>
  </xsl:function>
  
  <xsl:function name="my:isLetter" as="xs:boolean">
    <xsl:param name="char"/>
    <xsl:value-of select=" matches($char, '\p{L}')"/>
  </xsl:function>
  
  <!-- This function is only properly defined for alphabetic characters 
       For all others the function returns always true. -->
  <xsl:function name="my:isUpper" as="xs:boolean">
    <xsl:param name="char"/>
    <xsl:value-of select="$char=upper-case($char)"/>
  </xsl:function>
  
  <xsl:function name="my:isNumber" as="xs:boolean">
    <xsl:param name="number"/>
    <xsl:value-of select="number($number)=number($number)"/>
  </xsl:function>
  
  <xsl:function name="my:isPercent" as="xs:boolean">
    <xsl:param name="number"/>
    <xsl:value-of select="matches($number, '([%‰°])')"/>
  </xsl:function>
  
  <xsl:function name="my:isExponent" as="xs:boolean">
    <xsl:param name="number"/>
    <xsl:value-of select="matches($number, '([²³])')"/>
  </xsl:function>
  
  <xsl:function name="my:isFraction" as="xs:boolean">
    <xsl:param name="number"/>
    <xsl:value-of select="matches($number, '([¼½¾⅓⅔⅕⅖⅗⅘⅙⅚⅛⅜⅝⅞])')"/>
  </xsl:function>
  
  <xsl:function name="my:isNumberLike" as="xs:boolean">
    <xsl:param name="number"/>
    <xsl:value-of select="my:isNumber($number) or my:isPercent($number) or my:isFraction($number) or my:isExponent($number)"/>
  </xsl:function>
  
  <xsl:function name="my:hasSameCase" as="xs:boolean">
    <xsl:param name="a"/>
    <xsl:param name="b"/>
    <xsl:value-of
      select="(my:isLower($a) and my:isLower($b)) or (my:isUpper($a) and my:isUpper($b))"/>
  </xsl:function>
  
  <!-- TODO: solve split that works even if # in string 1. write failing test, 2. solve -->
  <xsl:function name="my:tokenizeByCase" as="item()*">
    <xsl:param name="string"/>
    <xsl:sequence select="tokenize(replace($string,'(\p{Lu}+|\p{Ll}+)', '$1#'), '#')[.]"/>
  </xsl:function>
  
  <xsl:function name="my:tokenizeForAbbr" as="item()*">
    <xsl:param name="string"/>
    <xsl:sequence select="tokenize(replace($string,'(\p{L}+|\P{L}+)', '$1#'), '#')[.]"/>
  </xsl:function>
  
  <xsl:function name="my:containsDot" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="contains($string,'.')"/>
  </xsl:function>
  
  <xsl:function name="my:starts-with-number" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="matches($string, '^\d')"/>
  </xsl:function>
  
  <xsl:function name="my:ends-with-number" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="matches($string, '\d$')"/>
  </xsl:function>

  <xsl:function name="my:ends-with-non-word" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="empty($string) or matches($string, '\W$')"/>
  </xsl:function>

  <xsl:function name="my:ends-with-word" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="not(empty($string)) and matches($string, '\w$')"/>
  </xsl:function>

  <xsl:function name="my:starts-with-non-word" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="empty($string) or matches($string, '^\W')"/>
  </xsl:function>

  <xsl:function name="my:starts-with-word" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="not(empty($string)) and matches($string, '^\w')"/>
  </xsl:function>

  <xsl:function name="my:starts-with-non-whitespace" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="not(empty($string)) and matches($string, '^\S')"/>
  </xsl:function>

  <xsl:function name="my:ends-with-non-whitespace" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="not(empty($string)) and matches($string, '\S$')"/>
  </xsl:function>

  <xsl:function name="my:starts-with-punctuation" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="matches($string, '^\p{P}')"/>
  </xsl:function>

  <xsl:function name="my:starts-with-punctuation-word" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="matches($string, '^(\p{P}|[-/])+\W')"/>
  </xsl:function>

  <xsl:function name="my:ends-with-punctuation-word" as="xs:boolean">
    <xsl:param name="string"/>
    <xsl:value-of select="matches($string, '\W([-/]|\p{P})+$')"/>
  </xsl:function>

  <xsl:function name="my:filter-hyphenation" as="xs:string">
    <xsl:param name="string"/>
    <xsl:value-of select="if ($hyphenation='true') then $string else translate($string, '­', '')"/>
  </xsl:function>

  <xsl:function name="my:has-brl-class" as="xs:boolean">
    <xsl:param name="context"/>
    <xsl:sequence select="$context/@brl:class or starts-with($context/@class, 'brl:')"/>
  </xsl:function>

  <xsl:function name="my:get-brl-class" as="xs:string">
    <xsl:param name="context"/>
    <xsl:sequence select="if ($context/@brl:class) then
			  $context/@brl:class else
			  if (starts-with($context/@class, 'brl:')) then
			  replace($context/@class, '^brl:', '')
			  else ''"/>
  </xsl:function>

  <xsl:function name="my:insert-element-changed-comment" as="xs:string">
    <xsl:param name="element" as="xs:string"/>
    <xsl:value-of select="concat('&#10;', 'xxx Was originally a ', $element, '&#10;')"/>
  </xsl:function>

</xsl:stylesheet>
