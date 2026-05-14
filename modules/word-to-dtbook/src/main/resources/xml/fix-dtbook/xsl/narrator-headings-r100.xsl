<?xml version="1.0" encoding="UTF-8"?>
<!--
    
    Version
    2008-05-09
    
    Description
    Prepare a dtbook to the Narrator schematron rules:
    - Rule 100: Every document needs at least one heading on level 1
    This fix assumes headings are not empty (e.g. empty headings were
    removed by a previous fix)
    
    Nodes
    frontmatter|bodymatter|rearmatter
    
    Namespaces
    (x) "http://www.daisy.org/z3986/2005/dtbook/"
    
    Doctype
    (x) DTBook
    
    Author
    Romain Deltour, DAISY
	
-->
<xsl:stylesheet
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
		xmlns="http://www.daisy.org/z3986/2005/dtbook/" version="2.0" exclude-result-prefixes="dtb">
	
	<xsl:include href="recursive-copy.xsl"/>
	<xsl:include href="output.xsl"/>
	<xsl:include href="dummy-headings-tools.xsl"/>
	
	<xsl:template match="dtb:frontmatter[not(following-sibling::dtb:*)]">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="dtb:level1/dtb:h1 or dtb:level/dtb:hd">
					<xsl:apply-templates/>
				</xsl:when>
				<xsl:when test="dtb:level1">
					<xsl:apply-templates mode="addH1"/>
				</xsl:when>
				<xsl:when test="dtb:level">
					<xsl:apply-templates mode="addHd"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
					<xsl:apply-templates select="." mode="addLevelWithHeading">
						<xsl:with-param name="level" select="1"/>
					</xsl:apply-templates>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dtb:rearmatter[not(preceding-sibling::dtb:bodymatter)]">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="dtb:level1/dtb:h1 or dtb:level/dtb:hd">
					<xsl:apply-templates/>
				</xsl:when>
				<xsl:when test="dtb:level1">
					<xsl:apply-templates mode="addH1"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates mode="addHd"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="dtb:bodymatter">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="dtb:level1/dtb:h1 or dtb:level/dtb:hd 
					or preceding-sibling::dtb:frontmatter[dtb:level1/dtb:h1 or dtb:level/dtb:hd]
					or following-sibling::dtb:rearmatter[dtb:level1/dtb:h1 or dtb:level/dtb:hd]">
					<xsl:apply-templates/>
				</xsl:when>
				<xsl:when test="dtb:level1">
					<xsl:apply-templates mode="addH1"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates mode="addHd"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dtb:level1[1]" mode="addH1">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="." mode="addHeading">
				<xsl:with-param name="level" select="1"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="node()" mode="addH1">
		<xsl:apply-templates select="." />
	</xsl:template>
	
	<xsl:template match="dtb:level[1]" mode="addHd">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="." mode="addHeading">
				<xsl:with-param name="level" select="'d'"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="node()" mode="addHd">
		<xsl:apply-templates select="." />
	</xsl:template>
	 
</xsl:stylesheet>
