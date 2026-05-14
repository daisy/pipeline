<?xml version="1.0" encoding="utf-8"?>
<!--
	Add levels
		Version
			2008-04-10

		Description
			Add levels where needed.
		
		Preconditions
			Levelnormalizer
			Levelsplitter

		Nodes
			level1, level2, level3, level4, level5, level6

		Namespaces
			(x) "http://www.daisy.org/z3986/2005/dtbook/"

		Doctype
			(x) DTBook

		Author
			Joel Håkansson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/">

	<xsl:include href="recursive-copy2.xsl"/>
	<xsl:include href="output2.xsl"/>
	
	<xsl:variable name="maxLevel" select="6"/>
	
	<xsl:template match="dtb:level1|dtb:level2|dtb:level3|dtb:level4|dtb:level5|dtb:level6">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:call-template name="scanNodes">
				<xsl:with-param name="currentLevel" select="number(substring-after(name(), 'level'))"/>
				<xsl:with-param name="node" select="node()"/>
			</xsl:call-template>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="dtb:bodymatter|dtb:rearmatter">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:call-template name="scanNodes">
				<xsl:with-param name="currentLevel" select="0"/>
				<xsl:with-param name="node" select="node()"/>
			</xsl:call-template>
		</xsl:copy>
	</xsl:template>

	<xsl:template name="scanNodes">
		<xsl:param name="currentLevel"/>
		<xsl:param name="targetLevel" select="$currentLevel + 1"/>
		<xsl:param name="node"/>
		<xsl:choose>
			<xsl:when test="$currentLevel>$maxLevel or $targetLevel>$maxLevel">
				<xsl:apply-templates select="$node"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each-group select="$node" group-starting-with="dtb:*[number(substring-after(name(), 'h'))=$targetLevel or number(substring-after(name(), 'level'))=$targetLevel]">
					<xsl:choose>
						<xsl:when test="current-group()[1][number(substring-after(name(), 'h'))=$targetLevel]">
							<xsl:call-template name="addLevels">
								<xsl:with-param name="currentLevel" select="$currentLevel"/>
								<xsl:with-param name="targetLevel" select="$targetLevel"/>
								<xsl:with-param name="node" select="current-group()"/>
							</xsl:call-template>
						</xsl:when>
						<xsl:otherwise>
							<xsl:call-template name="scanNodes">
								<xsl:with-param name="currentLevel" select="$currentLevel"/>
								<xsl:with-param name="targetLevel" select="$targetLevel + 1"/>
								<xsl:with-param name="node" select="current-group()"/>
							</xsl:call-template>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each-group>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="addLevels">
		<xsl:param name="currentLevel"/>
		<xsl:param name="targetLevel"/>
		<xsl:param name="node"/>
		<xsl:choose>
			<xsl:when test="$currentLevel=$targetLevel">
				<xsl:call-template name="scanNodes">
					<xsl:with-param name="currentLevel" select="$currentLevel"/>
					<xsl:with-param name="node" select="$node"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message terminate="no"><xsl:value-of select="concat('Added a level', $currentLevel + 1)"/></xsl:message>
				<xsl:element name="level{$currentLevel + 1}" namespace="http://www.daisy.org/z3986/2005/dtbook/">
					<xsl:call-template name="addLevels">
						<xsl:with-param name="currentLevel" select="$currentLevel + 1"/>
						<xsl:with-param name="targetLevel" select="$targetLevel"/>
						<xsl:with-param name="node" select="$node"/>
					</xsl:call-template>
				</xsl:element>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
		
</xsl:stylesheet>
