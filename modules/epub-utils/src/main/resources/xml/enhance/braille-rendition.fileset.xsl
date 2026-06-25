<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                version="2.0">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	
	<xsl:param name="braille-rendition.package-document.base" required="yes"/>
	<xsl:param name="content-media-types" required="yes" as="xs:string*"/>
	
	<xsl:template match="/">
		<d:fileset>
			<xsl:apply-templates select="//d:file"/>
		</d:fileset>
	</xsl:template>
	
	<xsl:template match="d:fileset/d:file">
		<xsl:choose>
			<xsl:when test="@media-type=$content-media-types">
				<xsl:variable name="default-href" select="resolve-uri(@href,pf:base-uri(.))"/>
				<xsl:element name="d:file">
					<xsl:attribute name="href" select="replace($default-href,'^(.+)\.x?html|(.+)$','$1$2_braille.xhtml')"/>
					<xsl:attribute name="original-href" select="$default-href"/>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@media-type='application/smil+xml'">
				<xsl:variable name="default-href" select="resolve-uri(@href,pf:base-uri(.))"/>
				<xsl:element name="d:file">
					<xsl:attribute name="href" select="replace($default-href,'^(.+)\.smil|(.+)$','$1$2_braille.smil')"/>
					<xsl:attribute name="original-href" select="$default-href"/>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@media-type='application/oebps-package+xml'">
				<xsl:element name="d:file">
					<xsl:attribute name="href" select="$braille-rendition.package-document.base"/>
					<xsl:attribute name="original-href" select="resolve-uri(@href,pf:base-uri(.))"/>
				</xsl:element>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
