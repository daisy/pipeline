<?xml version="1.0" encoding="UTF-8" ?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:c="http://www.w3.org/ns/xproc-step" 
    xmlns:xs="http://www.w3.org/2001/XMLSchema" version="2.0">
	<xsl:param name="lang" as="xs:string"/>
	<xsl:param name="basename" select="''"/>
	<xsl:template match="/">
	  <c:message>
	    <xsl:value-of
	      select="replace((//c:message[@xml:lang eq $lang], //c:message)[1], '(.)[&#xa;&#xd;]*$', '$1&#xa;')"/>
	  </c:message>
	</xsl:template>
</xsl:stylesheet>