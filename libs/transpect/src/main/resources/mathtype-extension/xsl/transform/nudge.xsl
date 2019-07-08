<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  exclude-result-prefixes="xs"
  version="2.0">
  <xsl:template match="nudge">
    <xsl:processing-instruction name="mtef2mml" select="'element nudge not supported in mathml. values dx:', dx, 'dy:', dy"/>
  </xsl:template>
</xsl:stylesheet>