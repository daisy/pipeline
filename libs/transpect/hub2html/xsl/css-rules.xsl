<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:html="http://www.w3.org/1999/xhtml"
  xmlns:css="http://www.w3.org/1996/css" 
  xmlns:tr="http://transpect.io"
  xmlns:hub2htm="http://transpect.io/hub2htm" 
  xmlns:csstmp="http://transpect.io/csstmp"  
  exclude-result-prefixes="xs html css hub2htm csstmp"
  xmlns="http://www.w3.org/1999/xhtml"
  version="2.0">

  <xsl:import href="http://transpect.io/xslt-util/colors/xsl/colors.xsl"/>

  <xsl:template match="* | @*" mode="hub2htm:css">
    <xsl:copy>
      <xsl:apply-templates select="@* | node()" mode="#current"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="css:rules" mode="hub2htm:css">
    <style type="text/css">
      <xsl:apply-templates mode="#current"/>
    </style>
  </xsl:template>
  
  <xsl:template match="css:rule" mode="hub2htm:css">
    <xsl:variable name="css-selector" select="@name"/>
    <xsl:variable name="css-atts" as="attribute(*)*">
      <!-- This mode enables selective property filtering in your overriding template -->
      <xsl:apply-templates select="@* except @csstmp:*" mode="hub2htm:css-style-defs"/>
    </xsl:variable>
    <xsl:variable name="css-properties" select="string-join(
      for $i in $css-atts return concat($i/local-name(), ':', $i)
      , ';&#xa;  ')"/>
    <xsl:if test="exists($css-atts)">
      <xsl:value-of select="concat('&#xa;.', $css-selector, '{&#xa;  ', $css-properties, '&#xa;}&#xa;')"></xsl:value-of>  
    </xsl:if>
  </xsl:template>

  <xsl:template match="@*[not(matches(name(), '^css'))]" mode="hub2htm:css-style-defs"/>
  
  <!-- CSS atts in the content are being removed. Remember to call hub2htm:style-overrides
       if you want to transform them into a style attribute -->
  <xsl:template match="@css:*" mode="hub2htm:css"/>
  
  <xsl:template match="@css:*" mode="hub2htm:css-style-overrides hub2htm:css-style-defs">
    <xsl:copy/>
  </xsl:template>

  <xsl:template match="@css:*[contains(., 'device-cmyk')]" mode="hub2htm:css-style-overrides hub2htm:css-style-defs">
    <xsl:attribute name="{name()}" select="tr:int-rgb-colors-to-hex(tr:device-cmyk-to-rgb-int-triple(.))"/>
  </xsl:template>
  
  <xsl:template match="@css:*[contains(., 'device-cmyk') and contains(., '-gradient')]" mode="hub2htm:css-style-overrides hub2htm:css-style-defs" priority="2">
    <!-- e.g. linear-gradient(device-cmyk(0.96,0.69,0,0), device-cmyk(0.75,0.05,1,0)) -->
    <xsl:variable name="regex" as="xs:string" select="'device-cmyk\s*\(\s*(0|0?\.\d+|1(\.0*)?)\s*,\s*(0|0?\.\d+|1(\.0*)?)\s*,\s*(0|0?\.\d+|1(\.0*)?)\s*,\s*(0|0?\.\d+|1(\.0*)?)\s*\)'"/>
    <xsl:attribute name="{name()}">
      <xsl:variable name="color-values">
        <xsl:analyze-string select="." regex="{$regex}">
          <xsl:matching-substring>
            <xsl:value-of select="tr:int-rgb-colors-to-hex(tr:device-cmyk-to-rgb-int-triple(.))"/>
          </xsl:matching-substring>
          <xsl:non-matching-substring>
            <xsl:value-of select="."/>
          </xsl:non-matching-substring>
        </xsl:analyze-string>
        </xsl:variable>
      <xsl:value-of select="$color-values"/>
    </xsl:attribute>
   
  </xsl:template>
  
  <!-- this give a hook to generate new ad-hoc styling for, e.g., @role
  By default, the non-css:* attributes will be discarded. -->
  <xsl:template match="@*" mode="hub2htm:css-style-overrides"/>
  
  <xsl:param name="css-target-version" select="'CSS3'" as="xs:string"/>
  
  <xsl:template match="@css:text-decoration-line[matches($css-target-version, '^CSS2')]" 
                mode="hub2htm:css-style-overrides hub2htm:css-style-defs">
    <xsl:attribute name="css:text-decoration" select="."/>
  </xsl:template>
  
  <xsl:template match="@css:direction[matches($css-target-version, '^CSS2')]" mode="hub2htm:css-style-overrides">
    <xsl:attribute name="dir" select="."/>
  </xsl:template>
  
  <xsl:variable name="remove-css2-attribs-regex"
                select="'^(css:text-decoration(-offset|-width|-color|-style|-skip)?|css:text-shadow|css:text-align-last)$'"
                
                as="xs:string"/>
   
  <xsl:template match="@*[matches(name(), $remove-css2-attribs-regex)][starts-with($css-target-version, 'CSS2')]"
                mode="hub2htm:css-style-overrides hub2htm:css-style-defs" priority="2"/>
   
  <xsl:template match="@css:font-family[contains(., '&#x20;')]" mode="hub2htm:css-style-overrides hub2htm:css-style-defs">
    <xsl:attribute name="{name()}" select="concat('&quot;', ., '&quot;')"/>
  </xsl:template>

  <!-- not sure whether this is still being used. Probably replaced by css:remaining-atts -->
  <xsl:function name="hub2htm:style-overrides" as="attribute()*">
    <xsl:param name="input" as="element(*)"/>
    <xsl:variable name="atts" as="attribute(*)*">
      <!-- This mode enables selective property filtering in your overriding template -->
      <xsl:apply-templates select="$input/@*" mode="hub2htm:css-style-overrides"/>
    </xsl:variable>
    <xsl:variable name="css-atts" as="attribute(*)*" 
      select="$atts[namespace-uri() = 'http://www.w3.org/1996/css']"/>
    <xsl:if test="exists($css-atts)">
      <xsl:attribute name="style"
        select="string-join(
        for $a in $css-atts[not(starts-with(name(), 'pseudo'))] 
        return concat(local-name($a), ': ', $a),
        '; '
        )"
      />
    </xsl:if>
    <xsl:sequence select="$atts except $css-atts"/>
  </xsl:function>
  
  <xsl:function name="css:display-type" as="xs:string">
    <xsl:param name="rule" as="element(css:rule)"/>
    <xsl:choose>
      <xsl:when test="$rule/@layout-type = 'para'"><xsl:sequence select="'block'"/></xsl:when>
      <xsl:when test="$rule/@layout-type = 'inline'"><xsl:sequence select="'inline'"/></xsl:when>
      <xsl:when test="$rule/@layout-type = 'table'"><xsl:sequence select="'table'"/></xsl:when>
      <xsl:when test="$rule/@layout-type = 'cell'"><xsl:sequence select="'table-cell'"/></xsl:when>
      <xsl:otherwise><xsl:sequence select="'block'"/></xsl:otherwise>
    </xsl:choose>
  </xsl:function>
  
</xsl:stylesheet>
