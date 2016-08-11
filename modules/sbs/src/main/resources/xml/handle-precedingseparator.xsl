<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">
  
  <xsl:template match="dtb:*[@class/tokenize(.,'\s+')='precedingseparator']">
    <dtb:div style="display: block;
                    text-align: center;
                    margin-top: 1;
                    margin-bottom: 1">
      ⠒⠒⠒⠒⠒⠒
    </dtb:div>
    <xsl:next-match/>
  </xsl:template>
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
</xsl:stylesheet>
