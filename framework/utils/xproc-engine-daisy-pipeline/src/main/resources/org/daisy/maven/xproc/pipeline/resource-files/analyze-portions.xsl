<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data">
  
  <!--
      Transforms a messages XML to a version with corrected portion attributes. The assumption is
      made that there is only one execution thread, i.e. that the time it takes between the begin of
      one step and the next determines the portion of that step.
  -->
  
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:template match="d:message[@portion]">
    <xsl:copy>
      <xsl:apply-templates select="@*"/>
      <xsl:attribute name="portion-actual" select="d:time(.) div d:time(parent::*)"/>
      <xsl:apply-templates/>
    </xsl:copy>
  </xsl:template>
  
  <xsl:function name="d:time">
    <xsl:param name="message"/>
    <xsl:sequence select="if ($message/self::d:messages)
                          then sum($message/*/d:time(.))
                          else if ($message/following::d:message)
                               then number($message/following::d:message[1]/@timeStamp) - number($message/@timeStamp)
                               else if ($message/descendant::d:message)
                               then number(($message/descendant::d:message)[last()]/@timeStamp) - number($message/@timeStamp)
                               else 0"/>
  </xsl:function>
  
</xsl:stylesheet>
