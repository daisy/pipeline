<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	    xmlns:c="http://www.w3.org/ns/xproc-step" 
	    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
	<xsl:template match="c:errors | c:error">
	  <xsl:element name="{replace(name(), 'error', 'message')}">
	    <xsl:copy-of select="@*"/>
	    <xsl:apply-templates/>
	  </xsl:element>
	</xsl:template>
</xsl:stylesheet>