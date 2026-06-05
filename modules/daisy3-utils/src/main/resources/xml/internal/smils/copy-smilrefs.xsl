<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
		xmlns:d="http://www.daisy.org/ns/pipeline/data"
		xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
		exclude-result-prefixes="#all">

  <xsl:key name="audio-loc" match="*[@id]" use="@id"/>

  <xsl:template match="@id">
    <xsl:next-match/>
    <xsl:variable name="model" select="key('audio-loc',.,collection()[/d:audio-order])[1]"/>
    <xsl:if test="$model/@smilref">
      <xsl:attribute name="smilref" namespace="{if (namespace-uri(..)='http://www.daisy.org/z3986/2005/dtbook/')
						then ''
						else 'http://www.daisy.org/z3986/2005/dtbook/'}">
	<!-- According to the specifications, @smilref must have the
	     DTBook namespace for elements that belong to
	     extensions. -->
	<xsl:value-of select="$model/@smilref"/>
      </xsl:attribute>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@pxi:no-smilref"/>

  <xsl:template match="@*|*">
    <xsl:copy copy-namespaces="no">
      <xsl:for-each select="namespace::*[not(.='http://www.daisy.org/ns/pipeline/xproc/internal')]">
	<xsl:sequence select="."/>
      </xsl:for-each>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

  <xsl:template match="text()|processing-instruction()|comment()">
    <xsl:sequence select="."/>
  </xsl:template>

</xsl:stylesheet>
