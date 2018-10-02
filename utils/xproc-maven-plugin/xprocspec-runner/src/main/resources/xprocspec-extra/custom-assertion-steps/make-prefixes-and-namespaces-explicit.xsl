<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:_xmlns="namespace-node-as-attribute">
	
	<xsl:template match="*">
		<xsl:variable name="element" select="."/>
		<xsl:copy>
			<xsl:if test="not(name()=local-name())">
				<xsl:attribute name="_prefix" select="substring-before(name(), ':')"/>
			</xsl:if>
			<xsl:for-each select="namespace::*">
				<xsl:choose>
					<xsl:when test="name()">
						<xsl:attribute name="_xmlns:{name()}" select="."/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="_xmlns" select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="@*">
		<xsl:sequence select="."/>
		<xsl:if test="not(name()=local-name())">
			<xsl:attribute name="{replace(name(),':','_colon')}" select="namespace-uri()"/>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="text()|comment()|processing-instruction()">
		<xsl:sequence select="."/>
	</xsl:template>
	
</xsl:stylesheet>
