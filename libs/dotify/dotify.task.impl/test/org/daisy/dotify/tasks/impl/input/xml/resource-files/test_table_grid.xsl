<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="dtb xs">

	<!-- Note: This import will only work in the test setup. -->
	<xsl:import href="../../../../../../../../../main/org/daisy/dotify/tasks/impl/input/xml/resource-files/common/xslt-files/split-table.xsl"/>
	<xsl:param name="table-split-columns" select="10"/>
	
	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	
	<xsl:template match="*|processing-instruction()|comment()">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dtb:table">
		<xsl:apply-templates select="." mode="splitTable">
			<xsl:with-param name="maxColumns" select="$table-split-columns"/>
		</xsl:apply-templates>
	</xsl:template>

</xsl:stylesheet>