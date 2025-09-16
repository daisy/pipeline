<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

<xsl:output method="xml" indent="no" encoding="UTF-8"/>

<xsl:template match="/">
	<!-- Moved to export-doctype xsl
	<xsl:variable name="version" select="//dtb:dtbook/@version"/>

	<xsl:choose>
    	<xsl:when test="$version='2005-1'">
    		<xsl:call-template name="print-doctype">
    			<xsl:with-param name="public" select="'-//NISO//DTD dtbook 2005-1//EN'"/>
    			<xsl:with-param name="system" select="'http://www.daisy.org/z3986/2005/dtbook-2005-1.dtd'"/>
    			<xsl:with-param name="root" select="'dtbook'"/>
    			<xsl:with-param name="internal" select="''"/>
    		</xsl:call-template>
    	</xsl:when>
    	<xsl:when test="$version='2005-2'">
    		<xsl:call-template name="print-doctype">
    			<xsl:with-param name="public" select="'-//NISO//DTD dtbook 2005-2//EN'"/>
    			<xsl:with-param name="system" select="'http://www.daisy.org/z3986/2005/dtbook-2005-2.dtd'"/>
    			<xsl:with-param name="root" select="'dtbook'"/>
    			<xsl:with-param name="internal" select="''"/>
    		</xsl:call-template>
    	</xsl:when>	
    	<xsl:when test="$version='2005-3'">
    		<xsl:call-template name="print-doctype">
    			<xsl:with-param name="public" select="'-//NISO//DTD dtbook 2005-3//EN'"/>
    			<xsl:with-param name="system" select="'http://www.daisy.org/z3986/2005/dtbook-2005-3.dtd'"/>
    			<xsl:with-param name="root" select="'dtbook'"/>
    			<xsl:with-param name="internal" select="''"/>
    		</xsl:call-template>
    	</xsl:when>	
    	<xsl:otherwise>
    		<xsl:message terminate="yes">
    			<xsl:text>Unrecognized DTBook version: '</xsl:text><xsl:value-of select="$version"/><xsl:text>'</xsl:text>
    		</xsl:message>
    	</xsl:otherwise>
    </xsl:choose>-->
    
    <!-- Process document -->
	<xsl:copy>
		<xsl:apply-templates/>
	</xsl:copy>
    
  </xsl:template>

<!-- 
  print-doctype - Prints a doctype declaration
  
    parameters:
      public   - The PUBLIC identifier (or an empty string if unspecified)
      system   - The SYSTEM identifier
      root     - The name of the root element
      internal - The internal DTD subset (or an empty string if unspecified) 
 -->
<xsl:template name="print-doctype">
	<xsl:param name="public"/>
	<xsl:param name="system"/>
	<xsl:param name="root"/>
	<xsl:param name="internal"/>
	
	<xsl:text disable-output-escaping="yes">
&lt;!DOCTYPE </xsl:text>
	<xsl:value-of select="$root"/>
	
	<xsl:choose>
		<xsl:when test="$public!='' and $system!='' and $root!=''">  	
			<xsl:text> PUBLIC "</xsl:text>
			<xsl:value-of select="$public"/>
			<xsl:text>" "</xsl:text>  
			<xsl:value-of select="$system"/>
			<xsl:text>"</xsl:text>
		</xsl:when>
		<xsl:when test="$system!='' and $root!=''">
			<xsl:text> SYSTEM "</xsl:text>
			<xsl:value-of select="$system"/>
			<xsl:text>"</xsl:text>
		</xsl:when>
	</xsl:choose>
	
	<xsl:if test="$internal!=''">
		<xsl:text> [</xsl:text>
		<xsl:value-of disable-output-escaping="yes" select="$internal"/>
		<xsl:text>] </xsl:text>
	</xsl:if>
	<xsl:text disable-output-escaping="yes">&gt;
</xsl:text>
	
</xsl:template>

</xsl:stylesheet>
