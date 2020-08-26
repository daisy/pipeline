<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    version="2.0">
    
    <!--  the SSML is serialized because AT&T doesn't play well with redeclarations
    		of namespaces, though it should be better to improve the automatic
    		serialization so to deal with redundant namespaces.  -->
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/> 
    
    <xsl:param name="voice" select="''"/>
    <xsl:param name="ending-mark" select="''"/>
    
    <xsl:variable name="end">
    	<xsl:if test="$ending-mark != ''">
    		<ssml:mark name="{$ending-mark}"/>
    	</xsl:if>
    	<!--  it is important that the break comes after the mark because it seems that AT&T
  		  doesn't see the mark if it comes after, using particular voices like alain16 or francesca16.-->
    	<ssml:break time="250ms"/>
    </xsl:variable>
    
    <xsl:variable name="voice-node">
    	<ssml:voice name="{$voice}"/>
    </xsl:variable>
    
     <xsl:variable name="speak-node">
    	<ssml:speak version="1.0"/>
    </xsl:variable>
    
    <xsl:template match="*">
    	<xsl:apply-templates mode="serialize-begin" select="$speak-node"/>
    	<xsl:if test="$voice != ''">
    		<xsl:apply-templates mode="serialize-begin" select="$voice-node"/>
    	</xsl:if>
    	<xsl:apply-templates mode="serialize" select="."/>
    	<xsl:apply-templates mode="serialize" select="$end"/>
    	<xsl:if test="$voice != ''">
    		<xsl:apply-templates mode="serialize-end" select="$voice-node"/>
    	</xsl:if>
    	<xsl:apply-templates mode="serialize-end" select="$speak-node"/>
  	</xsl:template>

	<xsl:template match="text()" mode="serialize">
		<xsl:value-of select="."/>
	</xsl:template>
	
	<xsl:template match="ssml:token|ssml:s|ssml:speak" mode="serialize" priority="3">
		<xsl:apply-templates mode="serialize" select="node()"/>
	</xsl:template>
	
	<xsl:template match="ssml:*" mode="serialize" priority="2">
		<xsl:apply-templates mode="serialize-begin" select="."/>
		<xsl:apply-templates mode="serialize" select="node()"/>
		<xsl:apply-templates mode="serialize-end" select="."/>
	</xsl:template>
	
	<xsl:template match="*" mode="serialize-begin">
		<xsl:value-of select="concat('&lt;', local-name())"/>
		<xsl:apply-templates select="@*" mode="serialize"/>
		<xsl:value-of select="'>'"/>
	</xsl:template>
	
	<xsl:template match="*" mode="serialize-end">
		<xsl:value-of select="concat('&lt;/', local-name(), '>')"/>
	</xsl:template>
	
	<xsl:template match="*" mode="serialize" priority="1">
		<xsl:apply-templates mode="serialize" select="node()"/>
	</xsl:template>
	
	<xsl:template match="@*" mode="serialize">
		<xsl:value-of select="concat(' ', local-name(), '=&quot;', ., '&quot;')"/>
	</xsl:template>

</xsl:stylesheet>