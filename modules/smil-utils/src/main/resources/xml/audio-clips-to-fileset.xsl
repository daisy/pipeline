<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all"
                version="2.0">

  <xsl:template match="/">
    <d:fileset>
      <xsl:for-each-group select="//d:clip" group-by="@src">
        <d:file href="{current-grouping-key()}"/>
      </xsl:for-each-group>
    </d:fileset>
  </xsl:template>

</xsl:stylesheet>
