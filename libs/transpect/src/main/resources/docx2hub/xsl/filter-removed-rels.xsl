<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0" xmlns:c="http://www.w3.org/ns/xproc-step"
  xmlns:cx="http://xmlcalabash.com/ns/extensions"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:template match="node() | @*">
    <xsl:copy>
      <xsl:apply-templates select="@*, node()"/>
    </xsl:copy>
  </xsl:template>
  <xsl:variable name="removed-rels" as="element(rel:Relationship)*"
    select="collection()[2]//rel:Relationship[@remove = 'yes']"/>
  <xsl:template match="c:entry">
    <xsl:choose>
      <xsl:when test="some $t in $removed-rels/@Target satisfies (ends-with(@name, $t))"/>
      <xsl:otherwise>
        <xsl:next-match/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>