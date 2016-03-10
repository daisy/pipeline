<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
	xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/" 
	xmlns:xs="http://www.w3.org/2001/XMLSchema"
	exclude-result-prefixes="dtb xs">

	<xsl:output method="xml" encoding="utf-8" indent="no"/>
	<xsl:param name="debug" select="false()" as="xs:boolean"/>
	
	<!-- 
		Splits a DTBook table into several parts, depending on the number of columns.
		It is safe to use for smaller tables (which will be left untouched). 
	-->
	<xsl:template match="dtb:table" mode="splitTable">
		<xsl:param name="maxColumns" select="10" as="xs:integer"/>
		<xsl:variable name="grid">
			<xsl:apply-templates select="." mode="makeGrid"/>
		</xsl:variable>
		<xsl:choose>
			<xsl:when test="$grid/summary/@grid-width>$maxColumns">
				<xsl:variable name="sections" select="ceiling($grid/summary/@grid-width div $maxColumns)"/>
				<xsl:variable name="size" select="ceiling($grid/summary/@grid-width div $sections)"/>
				<xsl:apply-templates select="." mode="tableSplitIterator">
					<xsl:with-param name="grid" select="$grid"/>
					<xsl:with-param name="start" select="1"/>
					<xsl:with-param name="size" select="$size"/>
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise><xsl:copy-of select="."/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="dtb:table" mode="verifySplitPoints">
		<xsl:param name="grid" required="yes"/>
		<xsl:param name="start" select="1"/>
		<xsl:param name="size" select="1"/>
		<xsl:variable name="valid" as="xs:boolean">
			<xsl:call-template name="isValidSplitPoint">
				<xsl:with-param name="grid" select="$grid"/>
				<xsl:with-param name="split" select="$start"/>
			</xsl:call-template>
		</xsl:variable>
		<!-- TODO: Support adjusting of the split point in case of conflicting colspan -->
		<xsl:if test="not($valid)">
			<xsl:message terminate="yes">Cannot split table.</xsl:message>
		</xsl:if>
		<xsl:variable name="end" select="$start+$size"/>
		<xsl:variable name="grid-width" as="xs:integer">
			<xsl:call-template name="getGridWidth">
				<xsl:with-param name="grid" select="$grid"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:if test="$start&lt;=$grid-width">
			<xsl:apply-templates select="." mode="makeSplitTable">
				<xsl:with-param name="grid" select="$grid"/>
				<xsl:with-param name="start" select="$start"/>
				<xsl:with-param name="end" select="$end"/>
			</xsl:apply-templates>
			<xsl:apply-templates select="." mode="tableSplitIterator">
				<xsl:with-param name="grid" select="$grid"/>
				<xsl:with-param name="start" select="$end"/>
				<xsl:with-param name="size" select="$size"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="*[self::dtb:td or self::dtb:th]" mode="makeSplitTable">
		<xsl:param name="grid" required="yes"/>
		<xsl:param name="start" required="yes"/>
		<xsl:param name="end" required="yes"/>
		<xsl:variable name="td-id" select="generate-id(.)"/>
		<xsl:if test="$grid/cell[@id=$td-id and @col>=$start and @col&lt;$end]">
			<xsl:variable name="cellStart" select="$grid/cell[@id=$td-id and @col=$start][1]"/> <!-- [1] if rowspan > 1 -->
			<xsl:variable name="cellEnd" select="$grid/cell[@id=$td-id and @col=$end - 1][1]"/> <!-- [1] if rowspan > 1 -->
			<xsl:copy>
				<xsl:copy-of select="@*[not(name()='colspan')]"/>
				<xsl:choose>
					<xsl:when test="$cellStart and $cellEnd">
						<xsl:attribute name="colspan"><xsl:value-of select="$end - $start"/></xsl:attribute>
					</xsl:when>
					<xsl:when test="$cellStart">
						<xsl:attribute name="colspan"><xsl:value-of select="(if (@colspan) then @colspan else 1) - $cellStart/@col-offset"/></xsl:attribute>
					</xsl:when>
					<xsl:when test="$cellEnd">
						<xsl:attribute name="colspan"><xsl:value-of select="$cellEnd/@col-offset+1"/></xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="@colspan">
							<xsl:attribute name="colspan"><xsl:value-of select="@colspan"/></xsl:attribute>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:apply-templates select="node()" mode="makeSplitTable">
					<xsl:with-param name="grid" select="$grid"/>
					<xsl:with-param name="start" select="$start"/>
					<xsl:with-param name="end" select="$end"/>
				</xsl:apply-templates>
			</xsl:copy>
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="*|processing-instruction()|comment()" mode="makeSplitTable">
		<xsl:param name="grid" required="yes"/>
		<xsl:param name="start" required="yes"/>
		<xsl:param name="end" required="yes"/>
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates select="node()" mode="makeSplitTable">
				<xsl:with-param name="grid" select="$grid"/>
				<xsl:with-param name="start" select="$start"/>
				<xsl:with-param name="end" select="$end"/>
			</xsl:apply-templates>
		</xsl:copy>
	</xsl:template>

	<!-- 
		This returns a grid view of the table where each coordinate has an element.
	-->
	<xsl:template match="dtb:table" mode="makeGrid">
		<!-- Don't allow tables where a row's cells have a uniform rowspan > 1 -->
		<!-- FIXME: This doesn't work, beccause it may give false positives 
		<xsl:for-each select="descendant::dtb:tr">
			<xsl:variable name="maxRowSpan">
				<xsl:choose>
					<xsl:when test="dtb:td/@rowspan"><xsl:value-of select="max(dtb:td/@rowspan)"/></xsl:when>
					<xsl:otherwise>1</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:if test="$maxRowSpan>1 and count(dtb:td)=count(dtb:td[@rowspan=$maxRowSpan])">
				<xsl:message terminate="yes">Table has a row with uniform rowspan > 1</xsl:message>
			</xsl:if>
		</xsl:for-each>-->
		<xsl:variable name="result">
			<xsl:apply-templates select="descendant::*[self::dtb:td or self::dtb:th][last()]" mode="gridBuilderOuter">
				<xsl:with-param name="table-id" select="generate-id()"/>
			</xsl:apply-templates>
		</xsl:variable>
		<!-- TODO: Check that count (@row-@col distinct values) = count(*) -->
		<xsl:copy-of select="$result"/>
		<summary>
			<xsl:attribute name="grid-width">
				<xsl:call-template name="getGridWidth">
					<xsl:with-param name="grid" select="$result"/>
				</xsl:call-template>
			</xsl:attribute>
			<xsl:call-template name="getValidSplitPoints">
				<xsl:with-param name="grid" select="$result"/>
			</xsl:call-template>
		</summary>
	</xsl:template>
	
	<xsl:template match="*[self::dtb:td or self::dtb:th]" mode="gridBuilderOuter">
		<xsl:param name="table-id" required="yes"/>
		<xsl:variable name="grid">
			<xsl:apply-templates select="preceding::*[self::dtb:td or self::dtb:th][1][ancestor::dtb:table[generate-id()=$table-id]]" mode="gridBuilderOuter">
				<xsl:with-param name="table-id" select="$table-id"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:copy-of select="$grid"/>
		<xsl:variable name="gy" select="ancestor::dtb:tr/count(preceding-sibling::dtb:tr)+1"/>
		<xsl:variable name="gx">
			<xsl:call-template name="findGridX">
				<xsl:with-param name="grid" select="$grid"/>
				<xsl:with-param name="gy" select="$gy"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:apply-templates select="." mode="gridBuilderInner">
			<xsl:with-param name="gx" select="$gx"/>
			<xsl:with-param name="gy" select="$gy"/>
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template match="*[self::dtb:td or self::dtb:th]" mode="gridBuilderInner">
		<xsl:param name="i" select="1"/> <!-- current_column + (current_row-1) * column_count -->
		<xsl:param name="gx" select="1"/>
		<xsl:param name="gy" select="1"/>
		<xsl:variable name="rowspan">
			<xsl:choose><xsl:when test="@rowspan"><xsl:value-of select="@rowspan"/></xsl:when><xsl:otherwise>1</xsl:otherwise></xsl:choose>
		</xsl:variable>
		<xsl:variable name="colspan">
			<xsl:choose><xsl:when test="@colspan"><xsl:value-of select="@colspan"/></xsl:when><xsl:otherwise>1</xsl:otherwise></xsl:choose>
		</xsl:variable>
		<xsl:if test="$i&lt;=$rowspan*$colspan">
			<!-- unpack i -->
			<xsl:variable name="cr" select="floor(($i - 1) div $colspan)"/>
			<xsl:variable name="cc" select="(($i - 1) mod $colspan)"/>
			<cell row="{$gy+$cr}" col="{$gx+$cc}" row-offset="{$cr}" col-offset="{$cc}" id="{generate-id()}" rowspan="{$rowspan}" colspan="{$colspan}" heading="{count(self::dtb:th)=1}">
				<xsl:if test="$debug">
					<xsl:attribute name="text" select="text()"/>
				</xsl:if>
			</cell> 
			<xsl:apply-templates select="." mode="gridBuilderInner">
				<xsl:with-param name="i" select="$i+1"/>
				<xsl:with-param name="gx" select="$gx"/>
				<xsl:with-param name="gy" select="$gy"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>

	<xsl:template name="findGridX">
		<xsl:param name="gx" select="1"/>
		<xsl:param name="gy" select="1"/>
		<xsl:param name="grid"/>
		<xsl:choose>
			<xsl:when test="$grid/cell[@row=$gy and @col=$gx]">
				<xsl:call-template name="findGridX">
					<xsl:with-param name="gx" select="$gx+1"/>
					<xsl:with-param name="gy" select="$gy"/>
					<xsl:with-param name="grid" select="$grid"/> 
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise><xsl:value-of select="$gx"/></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<!-- Returns true if a column is a valid split point -->
	<xsl:template name="isValidSplitPoint">
		<xsl:param name="grid" required="yes"/>
		<xsl:param name="split" required="yes"/>
		<xsl:choose>
			<xsl:when test="count($grid/cell[@col=$split])=count($grid/cell[@col=$split and @col-offset=0])">true</xsl:when>
			<xsl:otherwise>false</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="getValidSplitPoints">
		<xsl:param name="grid" required="yes"/>
		<xsl:param name="split" select="1"/>
		<xsl:choose>
			<xsl:when test="count($grid/cell[@col=$split])=count($grid/cell[@col=$split and @col-offset=0])"><split-point index="{$split}"/></xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
		<xsl:if test="count($grid/cell[@col=$split+1])>0">
			<xsl:call-template name="getValidSplitPoints">
				<xsl:with-param name="grid" select="$grid"/>
				<xsl:with-param name="split" select="$split+1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<!-- Gets the grid width -->
	<xsl:template name="getGridWidth">
		<xsl:param name="grid" required="yes"/>
		<xsl:value-of select="count($grid/cell[@row=1])"/>
	</xsl:template>

</xsl:stylesheet>