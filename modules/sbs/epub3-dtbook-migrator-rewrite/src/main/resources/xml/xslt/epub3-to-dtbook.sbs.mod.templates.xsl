<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:brl="http://www.daisy.org/z3986/2009/braille/"
                xmlns:f="http://www.daisy.org/pipeline/modules/nordic-epub3-dtbook-migrator/epub3-to-dtbook.xsl">
	
	<xsl:template match="brl:*">
		<xsl:copy>
			<xsl:sequence select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template priority="1"
	              match="html:span[f:classes(.)='linenum'][not(parent::html:p[f:classes(.)='line'])]"
	              xmlns:f="http://www.daisy.org/pipeline/modules/nordic-epub3-dtbook-migrator/epub3-to-dtbook.xsl">
		<dtb:span>
			<xsl:call-template name="f:attrs"/>
			<xsl:apply-templates select="node()"/>
		</dtb:span>
	</xsl:template>
	
</xsl:stylesheet>
