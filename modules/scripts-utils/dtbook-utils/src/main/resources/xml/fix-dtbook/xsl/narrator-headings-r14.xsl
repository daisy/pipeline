<?xml version="1.0" encoding="UTF-8"?>
<!--
    
    Version
    2008-05-07
    
    Description
    Prepare a dtbook to the Narrator schematron rules:
    - Rule 14: Don't allow <h x+1> in <level x+1> unless <h x> in <level x> is present
    This fix assumes headings are not empty (e.g. empty headings were
    removed by a previous fix)
    
    Nodes
    levelx
    
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
	
	<xsl:template match="dtb:level1">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="not(dtb:h1) and descendant::*[self::dtb:h2 or self::dtb:h3 or self::dtb:h4 or self::dtb:h5 or self::dtb:h6]">
					<xsl:apply-templates select="." mode="addHeading">
						<xsl:with-param name="level" select="1"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dtb:level2">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="not(dtb:h2) and descendant::*[self::dtb:h3 or self::dtb:h4 or self::dtb:h5 or self::dtb:h6]">
					<xsl:apply-templates select="." mode="addHeading">
						<xsl:with-param name="level" select="2"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	
	
	<xsl:template match="dtb:level3">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="not(dtb:h3) and descendant::*[self::dtb:h4 or self::dtb:h5 or self::dtb:h6]">
					<xsl:apply-templates select="." mode="addHeading">
						<xsl:with-param name="level" select="3"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	
	<xsl:template match="dtb:level4">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="not(dtb:h4) and descendant::*[self::dtb:h5 or self::dtb:h6]">
					<xsl:apply-templates select="." mode="addHeading">
						<xsl:with-param name="level" select="4"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	
	<xsl:template match="dtb:level5">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="not(dtb:h5) and descendant::*[self::dtb:h6]">
					<xsl:apply-templates select="." mode="addHeading">
						<xsl:with-param name="level" select="5"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
	
	
	<xsl:template match="dtb:level">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:choose>
				<xsl:when test="not(dtb:hd) and descendant::*[self::dtb:hd]">
					<xsl:apply-templates select="." mode="addHeading">
						<xsl:with-param name="level" select="'d'"/>
					</xsl:apply-templates>
				</xsl:when>
				<xsl:otherwise>
					<xsl:apply-templates/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:copy>
	</xsl:template>
 
</xsl:stylesheet>
