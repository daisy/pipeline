<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
                xmlns:f="http://www.daisy.org/pipeline/modules/nordic-epub3-dtbook-migrator/epub3-to-dtbook.xsl">
	
	<xsl:variable name="normalize-space-in-h" select="false()"/>
	
	<xsl:variable name="allow-links" select="true()"/>
	
	<xsl:variable name="generate-ids" select="false()"/>
	
	<xsl:variable name="transform-longdesc-to" select="'imgref'"/>
	
	<xsl:variable name="supported-list-types" select="('ol','ul','pl')"/>
	
	<xsl:variable name="add-list-depth" select="false()"/>
	
	<xsl:variable name="add-lic" select="false()"/>
	
	<xsl:template name="f:attrs">
		<xsl:call-template name="f:coreattrs"/>
		<xsl:call-template name="f:i18n"/>
		<xsl:copy-of select="@brl:*"/>
	</xsl:template>
	
</xsl:stylesheet>
