<?xml version="1.0" encoding="UTF-8"?>
<!--
	XML to Flow

	Description
	Provides a heuristic default Flow rendering for any XML file.
	
	Parameters
		page-width
		page-height
		inner-margin
		outer-margin
		row-spacing
		duplex

	Format (input -> output)
		XML -> Flow
   
	Author: Joel Håkansson
	Version: 2010-03-11
 -->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns="http://www.daisy.org/ns/2011/obfl">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="no"/>
	<xsl:param name="page-width" select="28" as="xs:integer"/>
	<xsl:param name="page-height" select="29" as="xs:integer"/>
	<xsl:param name="inner-margin" select="0" as="xs:integer"/>
	<xsl:param name="outer-margin" select="0" as="xs:integer"/>
	<xsl:param name="row-spacing" select="1" as="xs:decimal"/>
	<xsl:param name="duplex" select="true()" as="xs:boolean"/>

	<xsl:template match="/">
		<obfl version="2011-1">
			<layout-master name="plain" page-width="{$page-width}" 
							page-height="{$page-height}" inner-margin="{$inner-margin}"
							outer-margin="{$outer-margin}" row-spacing="{$row-spacing}" duplex="{$duplex}">
				<default-template>
					<header></header>
					<footer></footer>
				</default-template>
			</layout-master>
			<sequence initial-page-number="1" master="plain"><xsl:apply-templates/></sequence>
		</obfl>
	</xsl:template>

	<xsl:template match="*">
		<xsl:choose>
			<xsl:when test="parent::*/text()[not(normalize-space()='')] or count(descendant::text()[not(normalize-space()='')])=0">
				<xsl:apply-templates/>
			</xsl:when>
			<xsl:otherwise>
				<block margin-bottom="1"><xsl:apply-templates/></block></xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="text()[normalize-space()='']"/>
	
</xsl:stylesheet>
