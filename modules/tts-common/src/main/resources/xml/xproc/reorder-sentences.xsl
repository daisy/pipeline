<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:ssml="http://www.w3.org/2001/10/synthesis"
		exclude-result-prefixes="#all"
		version="2.0">

  <xsl:param name="ids-in-order" as="xs:string*" required="yes"/>

  <xsl:key name="sentences" match="ssml:s" use="@id"/>
  <xsl:key name="ids" match="*[@id]" use="@id"/>

  <xsl:variable name="ssml-docs">
    <ssml:all>
      <xsl:sequence select="collection()"/>
    </ssml:all>
  </xsl:variable>

  <xsl:variable name="ids-doc">
    <tmp:ids xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp">
      <xsl:for-each select="$ids-in-order">
	<tmp:element id="{.}"/>
      </xsl:for-each>
    </tmp:ids>
  </xsl:variable>

  <xsl:template match="/">
    <ssml:speak version="1.1">
      <xsl:for-each select="$ids-in-order">
	<xsl:variable name="id" select="."/>
	<xsl:copy-of select="key('sentences',$id,$ssml-docs)"/>
      </xsl:for-each>

      <!-- TODO: write that on the secondary port? -->
      <xsl:for-each select="$ssml-docs/ssml:all/ssml:speak/ssml:s">
	<xsl:if test="not(key('ids',@id,$ids-doc))">
	  <xsl:copy-of select="."/>
	</xsl:if>
      </xsl:for-each>
    </ssml:speak>
  </xsl:template>

</xsl:stylesheet>
