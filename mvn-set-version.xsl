<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pom="http://maven.apache.org/POM/4.0.0">
	
	<xsl:param name="VERSION" as="xs:string" required="yes"/>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="/pom:project/pom:version">
		<xsl:copy>
			<xsl:value-of select="$VERSION"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
