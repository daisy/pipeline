<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:axs="http://www.w3.org/2001/XMLSchema"
	xmlns:aobfl="http://www.daisy.org/ns/2011/obfl"
	xmlns:dotify="http://brailleapps.github.io/ns/dotify"
	xmlns:tmp="http://brailleapps.github.io/ns/dotify/result"
	exclude-result-prefixes="dotify tmp">
<!--
	<xsl:import href="dtbook_table_grid.xsl"/>-->
	<xsl:param name="debug" select="false()" as="axs:boolean"/>
	<xsl:param name="rowspanName" select="'row-span'" as="axs:string"/>
	<xsl:param name="colspanName" select="'col-span'" as="axs:string"/>
	
	<xsl:template match="node()">
		<xsl:copy>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="dotify:node">
		<aobfl:xml-processor-result>
			<xsl:apply-templates/>
		</aobfl:xml-processor-result>
	</xsl:template>
	
	<xsl:template match="aobfl:table">
		<xsl:variable name="grid">
			<xsl:apply-templates select="." mode="makeGrid"/>
		</xsl:variable>
		<xsl:variable name="sortedGrid">
			<xsl:for-each select="$grid/tmp:cell">
				<xsl:sort select="@row"/>
				<xsl:sort select="@col"/>
				<xsl:copy-of select="."/>
			</xsl:for-each>
		</xsl:variable>
		<aobfl:block>
			<xsl:apply-templates select="$sortedGrid/tmp:cell[@col=1]">
				<xsl:with-param name="table" select="."/>
			</xsl:apply-templates>
		</aobfl:block>
	</xsl:template>

	<xsl:template match="tmp:cell[@col=1]">
		<xsl:param name="table" required="yes"/>
		<aobfl:block>
			<xsl:call-template name="copyCell">
				<xsl:with-param name="cell" select="."/>
				<xsl:with-param name="table" select="$table"/>
			</xsl:call-template>
			<xsl:variable name="cr" select="@row"/>
			<xsl:apply-templates select="following-sibling::tmp:cell[@row=$cr][1]" mode="thisRow">
				<xsl:with-param name="table" select="$table"/>
			</xsl:apply-templates>
		</aobfl:block>
	</xsl:template>

	<xsl:template match="tmp:cell[@col!=1]" mode="thisRow">
		<xsl:param name="table" required="yes"/>
		<aobfl:block margin-left="2">
			<xsl:call-template name="copyCell">
				<xsl:with-param name="cell" select="."/>
				<xsl:with-param name="table" select="$table"/>
			</xsl:call-template>
			<xsl:apply-templates select="following-sibling::tmp:cell[1]" mode="thisRow">
				<xsl:with-param name="table" select="$table"/>
			</xsl:apply-templates>
		</aobfl:block>
	</xsl:template>
	
	<xsl:template name="copyCell">
		<xsl:param name="cell" required="yes"/>
		<xsl:param name="table" required="yes"/>
		<!-- Only copy contents once, otherwise leave empty -->
		<xsl:if test="$cell/@col-offset=0 and $cell/@row-offset=0">
			<xsl:variable name="node" select="$table//*[generate-id()=$cell/@id]"/>
			<xsl:if test="count($node)!=1">Error in stylesheet.</xsl:if>
			<xsl:copy-of select="$node/node()"/>
			<xsl:if test="count($node/node())=0">&#x2014;</xsl:if>
		</xsl:if>
	</xsl:template>
	
	<!-- dtbook_table_grid.xsl -->
	<!-- 
		This returns a grid view of the table where each coordinate has an element.
	-->
	<xsl:template match="*:table" mode="makeGrid">
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
			<xsl:apply-templates select="descendant::*[self::*:td or self::*:th][last()]" mode="gridBuilderOuter">
				<xsl:with-param name="table-id" select="generate-id()"/>
			</xsl:apply-templates>
		</xsl:variable>
		<!-- TODO: Check that count (@row-@col distinct values) = count(*) -->
		<xsl:copy-of select="$result"/>
		<tmp:summary>
			<xsl:attribute name="grid-width" select="dotify:calculateGridWidth($result)"/>
			<xsl:copy-of select="dotify:getValidSplitPoints($result)"/>
		</tmp:summary>
	</xsl:template>
	
	<xsl:template match="*[self::*:td or self::*:th]" mode="gridBuilderOuter">
		<xsl:param name="table-id" required="yes"/>
		<xsl:variable name="grid">
			<xsl:apply-templates select="preceding::*[self::*:td or self::*:th][1][ancestor::*:table[generate-id()=$table-id]]" mode="gridBuilderOuter">
				<xsl:with-param name="table-id" select="$table-id"/>
			</xsl:apply-templates>
		</xsl:variable>
		<xsl:copy-of select="$grid"/>
		<xsl:variable name="gy" select="ancestor::*:tr/count(preceding-sibling::*:tr)+1"/>
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
	
	<xsl:template match="*[self::*:td or self::*:th]" mode="gridBuilderInner">
		<xsl:param name="i" select="1"/> <!-- current_column + (current_row-1) * column_count -->
		<xsl:param name="gx" select="1"/>
		<xsl:param name="gy" select="1"/>
		<xsl:variable name="rowspan">
			<xsl:choose><xsl:when test="@*[name()=$rowspanName]"><xsl:value-of select="@*[name()=$rowspanName]"/></xsl:when><xsl:otherwise>1</xsl:otherwise></xsl:choose>
		</xsl:variable>
		<xsl:variable name="colspan">
			<xsl:choose><xsl:when test="@*[name()=$colspanName]"><xsl:value-of select="@*[name()=$colspanName]"/></xsl:when><xsl:otherwise>1</xsl:otherwise></xsl:choose>
		</xsl:variable>
		<xsl:if test="$i&lt;=$rowspan*$colspan">
			<!-- unpack i -->
			<xsl:variable name="cr" select="floor(($i - 1) div $colspan)"/>
			<xsl:variable name="cc" select="(($i - 1) mod $colspan)"/>
			<tmp:cell row="{$gy+$cr}" col="{$gx+$cc}" row-offset="{$cr}" col-offset="{$cc}" id="{generate-id()}" rowspan="{$rowspan}" colspan="{$colspan}" heading="{count(self::*:th)=1}">
				<xsl:if test="$debug">
					<xsl:attribute name="text" select="text()"/>
				</xsl:if>
			</tmp:cell> 
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
			<xsl:when test="$grid/tmp:cell[@row=$gy and @col=$gx]">
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
	<xsl:function name="dotify:isValidSplitPoint">
		<xsl:param name="grid"/>
		<xsl:param name="split"/>
		<xsl:choose>
			<xsl:when test="count($grid/tmp:cell[@col=$split])=count($grid/tmp:cell[@col=$split and @col-offset=0])">true</xsl:when>
			<xsl:otherwise>false</xsl:otherwise>
		</xsl:choose>
	</xsl:function>
	
	<!-- Function wrapper for getValidSplitPoints template -->
	<xsl:function name="dotify:getValidSplitPoints">
		<xsl:param name="grid"/>
		<xsl:call-template name="getValidSplitPoints">
			<xsl:with-param name="grid" select="$grid"/>
		</xsl:call-template>
	</xsl:function>
	
	<!-- Returns all valid split points-->
	<xsl:template name="getValidSplitPoints">
		<xsl:param name="grid" required="yes"/>
		<xsl:param name="split" select="1"/>
		<xsl:choose>
			<xsl:when test="count($grid/tmp:cell[@col=$split])=count($grid/tmp:cell[@col=$split and @col-offset=0])"><tmp:split-point index="{$split}"/></xsl:when>
			<xsl:otherwise></xsl:otherwise>
		</xsl:choose>
		<xsl:if test="count($grid/tmp:cell[@col=$split+1])>0">
			<xsl:call-template name="getValidSplitPoints">
				<xsl:with-param name="grid" select="$grid"/>
				<xsl:with-param name="split" select="$split+1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>
	
	<!-- Returns the grid width -->
	<xsl:function name="dotify:calculateGridWidth">
		<xsl:param name="grid"/>
		<xsl:value-of select="count($grid/tmp:cell[@row=1])"/>
	</xsl:function>
	
	<!-- Returns a print out of the grid information -->
	<xsl:function name="dotify:gridAsString">
		<xsl:param name="grid"/>
		<xsl:for-each select="$grid/tmp:cell"><xsl:for-each select="@*"><xsl:value-of select="name()"/>=<xsl:value-of select="."/>
			<xsl:text>&#x09;</xsl:text>
		</xsl:for-each>
			<xsl:text>&#x0a;</xsl:text>
		</xsl:for-each>
	</xsl:function>
	
</xsl:stylesheet>