<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                exclude-result-prefixes="#all">

	<!-- Return a fileset of all the resources referenced from a DTBook (i.e. images, CSS and PLS lexicons) -->

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:param name="context.fileset" as="document-node(element(d:fileset))?"/>
	<xsl:param name="context.in-memory" as="document-node()*"/>

    <xsl:variable name="doc-base" select="base-uri(/*)"/>

	<xsl:template match="/">
		<d:fileset>
			<xsl:attribute name="xml:base" select="replace($doc-base,'[^/]+$','')"/>
			<xsl:for-each select="//dtb:link[@rel='stylesheet'][empty(@type) or @type='text/css']/@href[normalize-space()]">
				<xsl:sequence select="f:fileset-entry(.,'text/css')"/>
			</xsl:for-each>
			<xsl:for-each select="//dtb:link[@rel='pronunciation']/@href[normalize-space()]">
				<xsl:sequence select="f:fileset-entry(.,../@type)"/>
			</xsl:for-each>
			<xsl:for-each select="//dtb:*/@src">
				<xsl:sequence select="f:fileset-entry(.,())"/>
			</xsl:for-each>
			<xsl:for-each select="//m:math/@altimg">
				<xsl:sequence select="f:fileset-entry(.,())"/>
			</xsl:for-each>
		</d:fileset>
	</xsl:template>

	<xsl:function name="f:fileset-entry" as="element()?">
		<xsl:param name="uri" as="item()"/>
		<xsl:param name="type" as="xs:string?"/>
		<xsl:variable name="href" select="pf:normalize-uri($uri,false())"/>
		<xsl:if test="$href and (pf:get-scheme($href)='file' or pf:is-relative($href))">
			<xsl:variable name="resolved"
			              select="if ($uri instance of attribute())
			                      then resolve-uri($href,pf:base-uri($uri))
			                      else $href"/>
			<xsl:variable name="file-in-context" as="element(d:file)?"
						  select="$context.fileset//d:file[resolve-uri(@href,base-uri(.))=$resolved][1]"/>
			<d:file href="{pf:relativize-uri($resolved,$doc-base)}">
				<xsl:choose>
					<xsl:when test="$file-in-context[@original-href]">
						<xsl:sequence select="$file-in-context/@original-href"/>
					</xsl:when>
					<xsl:when test="$file-in-context"/>
					<xsl:otherwise>
						<xsl:attribute name="original-href" select="$resolved"/>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:if test="$type">
					<xsl:attribute name="media-type" select="$type"/>
				</xsl:if>
			</d:file>
		</xsl:if>
	</xsl:function>

</xsl:stylesheet>
