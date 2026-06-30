<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                version="2.0">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	
	<xsl:param name="epub-base" required="yes" as="xs:string"/>
	<xsl:param name="content-media-types" required="yes" as="xs:string*"/>
	
	<xsl:variable name="original-base" as="xs:string"
	              select="pf:longest-common-uri(
	                        //d:file[not(@media-type='application/oebps-package+xml')]
	                         /resolve-uri(@href,base-uri(.)))"/>

	<!--
	    FIXME: assuming that the input does not have a "braille" folder
	-->
	<xsl:variable name="braille-base" as="xs:string" select="resolve-uri('braille/',$epub-base)"/>

	<xsl:template match="/">
		<d:fileset>
			<xsl:apply-templates select="//d:file"/>
		</d:fileset>
	</xsl:template>
	
	<xsl:template match="d:fileset/d:file">
		<xsl:choose>
			<xsl:when test="@media-type=($content-media-types,'application/smil+xml')">
				<xsl:variable name="original-href" select="resolve-uri(@href,pf:base-uri(.))"/>
				<xsl:variable name="braille-href" select="resolve-uri(pf:relativize-uri($original-href,$original-base),
				                                                      $braille-base)"/>
				<xsl:element name="d:file">
					<xsl:attribute name="href" select="$braille-href"/>
					<xsl:attribute name="original-href" select="$original-href"/>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@media-type='application/oebps-package+xml'">
				<xsl:variable name="original-href" select="resolve-uri(@href,pf:base-uri(.))"/>
				<xsl:variable name="braille-href" select="resolve-uri('package.opf',$braille-base)"/>
				<xsl:element name="d:file">
					<xsl:attribute name="href" select="$braille-href"/>
					<xsl:attribute name="original-href" select="$original-href"/>
				</xsl:element>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
