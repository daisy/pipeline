<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:m="http://www.w3.org/1998/Math/MathML"
		exclude-result-prefixes="#all"
		version="2.0">

  <!-- The serialized MathML contains explicit depth information
       because otherwise we coudn't easily parse XML with regexps. -->
  <!-- Depth levels start from 1 rather than 0. -->

  <xsl:function name="d:serialize">
    <xsl:param name="nodes" as="node()*"/>
    <xsl:param name="to-regex"/>
    <xsl:apply-templates select="$nodes" mode="serialize">
      <xsl:with-param name="depth" select="'1'"/>
      <xsl:with-param name="to-regex" select="$to-regex"/>
    </xsl:apply-templates>
  </xsl:function>

  <xsl:template match="text()" mode="serialize">
    <xsl:param name="depth"/>
    <xsl:param name="to-regex"/>
    <xsl:variable name="clean-text" select="normalize-space(.)"/>
    <xsl:value-of select="if ($to-regex='true') then
			  replace($clean-text, '([\[\].*+?$^()|{}])', '\\$1')
			  else $clean-text"/>
  </xsl:template>

  <xsl:template match="m:text" mode="serialize">
    <!-- m:text nodes are handled somewhere else -->
  </xsl:template>

  <xsl:template match="*" mode="serialize">
    <xsl:param name="depth"/>
    <xsl:param name="to-regex"/>
    <!-- issues with the attrs order? -->
    <xsl:value-of
	select="concat(':', $depth, ':', local-name(), '_', string-join(@*, '_'), ' ')"/>
    <xsl:apply-templates select="node()" mode="serialize">
      <xsl:with-param name="depth" select="number($depth)+1"/>
      <xsl:with-param name="to-regex" select="$to-regex"/>
    </xsl:apply-templates>
    <xsl:value-of select="concat('@', $depth, '@', local-name(), ' ')"/>
  </xsl:template>

  <!-- <xsl:template match="d:one-node"> -->
  <!--   <xsl:param name="depth"/> -->
  <!-- How to do it? -->
  <!-- </xsl:template> -->

  <!-- one or more nodes -->
  <xsl:template match="d:group" mode="serialize">
    <xsl:param name="depth"/>
    <xsl:param name="to-regex"/>
    <xsl:value-of select="concat('(:', $depth, ':.+@', $depth, '@[-a-z]+ ', ')')"/>
  </xsl:template>

  <!-- zero or more nodes -->
  <xsl:template match="d:optional-group" mode="serialize">
    <xsl:param name="depth"/>
    <xsl:param name="to-regex"/>
    <xsl:value-of select="'(.*)'"/>
  </xsl:template>

  <!-- match a text node -->
  <xsl:template match="d:text" mode="serialize">
    <xsl:param name="depth"/>
    <xsl:param name="to-regex"/>
    <xsl:variable name="regex" select="if (@regex) then @regex else '[^:]+'"/>
    <xsl:value-of select="concat('(', $regex, ')')"/>
  </xsl:template>

</xsl:stylesheet>
