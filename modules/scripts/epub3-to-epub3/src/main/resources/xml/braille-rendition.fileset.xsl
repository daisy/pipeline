<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                version="2.0">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	
	<xsl:variable name="target.base.fileset" select="collection()[1]"/>
	<xsl:variable name="default-rendition.package-document" select="collection()[2]"/>
	
	<xsl:variable name="target.base" select="base-uri($target.base.fileset/d:fileset)"/>
	
	<xsl:template match="/d:fileset">
		<xsl:copy>
			<xsl:sequence select="@*|d:file"/>
			<xsl:apply-templates select="$default-rendition.package-document//opf:manifest/opf:item"/>
		</xsl:copy>
	</xsl:template>
	
	<xsl:template match="opf:manifest/opf:item">
		<xsl:variable name="original-href" select="pf:relativize-uri(resolve-uri(@href,base-uri(.)),$target.base)"/>
		<xsl:element name="d:file">
			<xsl:choose>
				<xsl:when test="@media-type='application/xhtml+xml'">
					<xsl:attribute name="href" select="replace($original-href,'^(.+)\.x?html|(.+)$','$1$2_braille.xhtml')"/>
				</xsl:when>
				<xsl:when test="@media-type='application/smil+xml'">
					<xsl:attribute name="href" select="replace($original-href,'^(.+)\.smil|(.+)$','$1$2_braille.smil')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="href" select="$original-href"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:attribute name="original-href" select="$original-href"/>
			<xsl:sequence select="@media-type"/>
		</xsl:element>
	</xsl:template>
	
</xsl:stylesheet>
