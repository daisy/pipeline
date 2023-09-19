<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:ssml="http://www.w3.org/2001/10/synthesis"
    version="2.0">

    <!--  the SSML needs to be serialized because of the starting \voice command -->
    <xsl:output method="text" omit-xml-declaration="yes" indent="no"/>

    <xsl:param name="voice" select="''"/>

    <xsl:variable name="end">
    	<ssml:break time="250ms"/>
    </xsl:variable>

    <xsl:template match="*">
    	<xsl:if test="$voice != ''">
    		<xsl:value-of select="concat('\voice{', $voice, '}')"/>
    	</xsl:if>
    	<xsl:apply-templates mode="serialize" select="."/>
    	<xsl:apply-templates mode="serialize" select="$end"/>
  	</xsl:template>

	<xsl:template match="text()" mode="serialize">
		<xsl:value-of select="."/>
	</xsl:template>

	<xsl:template match="ssml:token" mode="serialize" priority="3">
		<xsl:apply-templates mode="serialize" select="node()"/>
	</xsl:template>

	<xsl:template match="ssml:mark" mode="serialize" priority="3">
	    <!--  we can use any name as long as it is unique -->
		<xsl:value-of select="concat('&lt;mark name=&quot;', generate-id(), '&quot;/>')"/>
	</xsl:template>

	<xsl:template match="ssml:*" mode="serialize" priority="2">
		<xsl:value-of select="concat('&lt;', local-name())"/>
		<xsl:apply-templates select="@*" mode="serialize"/>
		<xsl:value-of select="'>'"/>
		<xsl:if test="local-name() = 's'">
		  <!-- Acapela can fail notifying marks if the sentence doesn't start with a white space. -->
		  <xsl:value-of select="' '"/>
		</xsl:if>
		<xsl:apply-templates mode="serialize" select="node()"/>
		<xsl:value-of select="concat('&lt;/', local-name(), '>')"/>
	</xsl:template>

	<xsl:template match="*" mode="serialize" priority="1">
		<xsl:apply-templates mode="serialize" select="node()"/>
	</xsl:template>

	<xsl:template match="@*" mode="serialize">
		<xsl:value-of select="concat(' ', local-name(), '=&quot;', ., '&quot;')"/>
	</xsl:template>

</xsl:stylesheet>
