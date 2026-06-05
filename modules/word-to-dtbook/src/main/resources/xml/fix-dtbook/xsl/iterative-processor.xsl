<?xml version="1.0" encoding="UTF-8"?>
<!--
	Iterative processor
		Version
			2008-04-02

		Description
			Processes a node in multiple iterations until the tree stops changing.
			Use apply-templates with mode="startProcessing" on the node where the
			process should begin. Note that care must be taken not to trigger 
			an endless recursion.

			See tidy-externalize-whitespace.xsl for an implementation example.

		Author
			Joel Håkansson, TPB
-->
<xsl:stylesheet version="2.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
		xmlns:xs="http://www.w3.org/2001/XMLSchema"
		xmlns:fn="http://www.w3.org/2005/xpath-functions">
	
	<xsl:template match="/|*" mode="startProcessing">
		<xsl:message terminate="no">Initializing...</xsl:message>
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:call-template name="processChildNodes">
				<xsl:with-param name="node" select="child::node()"/>
			</xsl:call-template>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="*" mode="iteratorStart">
		<xsl:apply-templates select="."/>
	</xsl:template>
	
	<xsl:template name="processChildNodes">
		<xsl:param name="node"/>
		<xsl:param name="count" select="1"/>
		<xsl:message terminate="no"><xsl:value-of select="concat('Processing nodes... (Iteration ', $count, ')')"/></xsl:message>
		<xsl:variable name="tempDoc"><xsl:apply-templates select="$node" mode="iteratorStart"/></xsl:variable>
		<xsl:choose>
			<xsl:when test="deep-equal($tempDoc, $node)">
				<xsl:copy-of select="$node"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="processChildNodes">
					<xsl:with-param name="node" select="$tempDoc"/>
					<xsl:with-param name="count" select="$count + 1"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
