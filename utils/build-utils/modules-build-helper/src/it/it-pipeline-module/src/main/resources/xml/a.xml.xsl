<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
	
	<xsl:output method="html"/>
	
	<xsl:template match="/">
		<html>
			<body>
				<xsl:apply-templates select="*">
					<xsl:with-param name="level" select="1"/>
				</xsl:apply-templates>
			</body>
		</html>
	</xsl:template>
	
	<xsl:template match="*">
		<xsl:param name="level" required="yes"/>
		<xsl:element name="h{$level}">
			<xsl:value-of select="local-name(.)"/>
		</xsl:element>
		<xsl:apply-templates>
			<xsl:with-param name="level" select="$level + 1"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="text()">
		<xsl:element name="p">
			<xsl:sequence select="."/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="@*"/>
	
</xsl:stylesheet>
