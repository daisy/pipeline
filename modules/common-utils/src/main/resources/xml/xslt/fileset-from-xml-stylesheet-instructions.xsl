<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all" >

	<xsl:template match="/">
		<xsl:copy>
			<d:fileset>
				<xsl:for-each select="/*/d:xml-stylesheet-instruction[@href]">
					<d:file href="{@href}">
						<xsl:choose>
							<xsl:when test="@type">
								<xsl:attribute name="media-type" select="@type"/>
							</xsl:when>
							<!--
							    media-type detection (could also be done with px:mediatype-detect)
							-->
							<xsl:when test="matches(@href,'\.xslt?$')">
								<xsl:attribute name="media-type" select="'text/xsl'"/>
							</xsl:when>
							<xsl:when test="matches(@href,'\.css$')">
								<xsl:attribute name="media-type" select="'text/css'"/>
							</xsl:when>
							<xsl:when test="matches(@href,'\.scss$')">
								<xsl:attribute name="media-type" select="'text/x-scss'"/>
							</xsl:when>
						</xsl:choose>
						<xsl:if test="@media">
							<xsl:attribute name="stylesheet-media" select="@media"/>
						</xsl:if>
					</d:file>
				</xsl:for-each>
			</d:fileset>
		</xsl:copy>
	</xsl:template>

</xsl:stylesheet>
