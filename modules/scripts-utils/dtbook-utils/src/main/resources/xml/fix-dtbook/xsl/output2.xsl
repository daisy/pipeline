<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

<xsl:output method="xml" version="1.0" indent="no" encoding="UTF-8" 
        doctype-public="-//NISO//DTD dtbook 2005-1//EN"
        doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-1.dtd" 
        name="v2005-1"/>
        
<xsl:output method="xml" version="1.0" indent="no" encoding="UTF-8" 
        doctype-public="-//NISO//DTD dtbook 2005-2//EN"
        doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-2.dtd" 
        name="v2005-2"/>

<xsl:output method="xml" version="1.0" indent="no" encoding="UTF-8" 
        doctype-public="-//NISO//DTD dtbook 2005-3//EN"
        doctype-system="http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd" 
        name="v2005-3"/>

<xsl:template match="/">
	<!-- Moved the doctype generation in export-doctype.xsl to be called at end of process -->
	<!-- Check version attribute
	<xsl:variable name="version" select="/dtb:dtbook/@version"/>
	
	<xsl:choose>
    	<xsl:when test="$version='2005-1'">
			<xsl:result-document format="v2005-1">
				<xsl:apply-templates />
			</xsl:result-document> 
    	</xsl:when>
    	<xsl:when test="$version='2005-2'">
			<xsl:result-document format="v2005-2">
				<xsl:apply-templates />
			</xsl:result-document>
    	</xsl:when>	
    	<xsl:when test="$version='2005-3'">
			<xsl:result-document format="v2005-3">
				<xsl:apply-templates />
			</xsl:result-document>
    	</xsl:when>	
    	<xsl:otherwise>
    		<xsl:message terminate="yes">
    			<xsl:text>Unrecognized DTBook version: '</xsl:text><xsl:value-of select="$version"/><xsl:text>'</xsl:text>
    		</xsl:message>
    	</xsl:otherwise>
    </xsl:choose> -->
	<xsl:copy>
		<xsl:apply-templates />
	</xsl:copy>
    
  </xsl:template>

</xsl:stylesheet>
