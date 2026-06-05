<?xml version="1.0" encoding="UTF-8"?>
<!--
	Level tools
		Version
			2007-09-27

		Description
			Level tools inserts levels by using the templates "addRootStructure",
			"addSubStructure" and "insertLevel".

			* "addRootStructure" is used when adding level1 nodes
			* "addSubStructure" is used when adding levelx (x>1) nodes
			* "insertLevel" is used by the other two to execute the level insertion.

			Known bug: Level tools does not respond correctly when a comment or 
			processing-instruction node is the first child node.

		Nodes
			levelx

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

	<xsl:template name="addRootStructure">
		<xsl:for-each select="node()">
			<xsl:variable name="first" select="self::* and count(preceding-sibling::node())=count(preceding-sibling::text()[normalize-space()=''])"/>
			<xsl:variable name="last" select="self::* and count(following-sibling::node())=count(following-sibling::text()[normalize-space()=''])"/>
			<xsl:call-template name="insertLevel">
				<xsl:with-param name="first" select="$first"/>
				<xsl:with-param name="last" select="$last"/>
				<xsl:with-param name="level" select="1"/>
			</xsl:call-template>
		</xsl:for-each>	
	</xsl:template>

	<xsl:template name="addSubStructure">
		<xsl:param name="level"/>
		<xsl:apply-templates select="node()[
			not(
				self::dtb:*[
					number(substring-after(name(), 'h'))>=$level 
					or 
					number(substring-after(name(), 'level'))>=$level
				]
			) and not(
				preceding-sibling::*[
					self::dtb:*[
						number(substring-after(name(), 'h'))>=$level 
						or 
						number(substring-after(name(), 'level'))>=$level
					]
				]
			)
		]"/>
		<xsl:for-each select="node()[
			self::dtb:*[
					number(substring-after(name(), 'h'))>=$level 
					or 
					number(substring-after(name(), 'level'))>=$level
			]
			or preceding-sibling::*[
				self::dtb:*[
					number(substring-after(name(), 'h'))>=$level 
					or 
					number(substring-after(name(), 'level'))>=$level
				]
			]
		]">
			<xsl:variable name="first" select="not(preceding-sibling::*[
				self::dtb:*[
					number(substring-after(name(), 'h'))>=$level 
					or 
					number(substring-after(name(), 'level'))>=$level
				]])"/>
			<xsl:variable name="last" select="self::* and count(following-sibling::node())=count(following-sibling::text()[normalize-space()=''])"/>
			<xsl:call-template name="insertLevel">
				<xsl:with-param name="first" select="$first"/>
				<xsl:with-param name="last" select="$last"/>
				<xsl:with-param name="level" select="$level"/>
			</xsl:call-template>
		</xsl:for-each>	
	</xsl:template>

	<xsl:template name="insertLevel">
		<xsl:param name="first"/>
		<xsl:param name="last"/>
		<xsl:param name="level"/>

		<xsl:choose>
			<xsl:when test="self::dtb:*[name()=concat('level', $level)]">
				<xsl:message terminate="no">Corrected a level</xsl:message>
				<xsl:if test="not($first) and not(preceding-sibling::*[1][self::dtb:*[name()=concat('level', $level)]])">
					<xsl:value-of select="concat('&lt;/level', $level, '&gt;')" disable-output-escaping="yes"/>
				</xsl:if>
				<xsl:apply-templates select="."/>
				<xsl:if test="not($last) and not(following-sibling::*[1][self::dtb:*[name()=concat('level', $level)]])">
					<xsl:value-of select="concat('&lt;level', $level, '&gt;')" disable-output-escaping="yes"/>
				</xsl:if>
			</xsl:when>
			<xsl:when test="$first">
				<xsl:message terminate="no">Corrected a level</xsl:message>
				<xsl:value-of select="concat('&lt;level', $level, '&gt;')" disable-output-escaping="yes"/>
				<xsl:apply-templates select="."/>
			</xsl:when>
			<xsl:when test="$last">
				<xsl:message terminate="no">Corrected a level</xsl:message>
				<xsl:apply-templates select="."/>
				<xsl:value-of select="concat('&lt;/level', $level, '&gt;')" disable-output-escaping="yes"/>
			</xsl:when>
			<xsl:when test="self::dtb:*[name()=concat('h', $level)] and preceding-sibling::dtb:*[number(substring-after(name(), 'h'))>=$level]">
				<xsl:if test="not(preceding-sibling::*[1][self::dtb:*[name()=concat('level', $level)]])">
					<xsl:value-of select="concat('&lt;/level', $level, '&gt;&lt;level', $level, '&gt;')" disable-output-escaping="yes"/>
					<xsl:message terminate="no">Corrected a level</xsl:message>
				</xsl:if>
				<xsl:apply-templates select="."/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="."/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>