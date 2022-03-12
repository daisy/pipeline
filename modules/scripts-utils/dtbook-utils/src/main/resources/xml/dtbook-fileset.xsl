<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                exclude-result-prefixes="#all">

	<!-- Return a fileset of all the resources referenced from a DTBook (i.e. images, CSS and PLS lexicons) -->

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:template match="/">
		<xsl:variable name="base" select="base-uri(/*)"/>
		<d:fileset>
			<xsl:attribute name="xml:base" select="replace($base,'[^/]+$','')"/>
			<xsl:for-each select="//dtb:link[@rel='stylesheet'][empty(@type) or @type='text/css']/@href[normalize-space()]">
				<d:file href="{pf:relativize-uri(resolve-uri(normalize-space(.),pf:base-uri(.)),$base)}">
					<xsl:attribute name="media-type" select="'text/css'"/>
				</d:file>
			</xsl:for-each>
			<xsl:for-each select="//dtb:link[@rel='pronunciation']/@href[normalize-space()]">
				<d:file href="{pf:relativize-uri(resolve-uri(normalize-space(.),pf:base-uri(.)),$base)}">
					<xsl:if test="../@type">
						<xsl:attribute name="media-type" select="../@type"/>
					</xsl:if>
				</d:file>
			</xsl:for-each>
			<xsl:for-each select="//dtb:*/@src">
				<d:file href="{pf:relativize-uri(resolve-uri(.,pf:base-uri(.)),$base)}"/>
			</xsl:for-each>
			<xsl:for-each select="//m:math/@altimg">
				<d:file href="{pf:relativize-uri(resolve-uri(.,pf:base-uri(.)),$base)}"/>
			</xsl:for-each>
		</d:fileset>
	</xsl:template>

</xsl:stylesheet>
