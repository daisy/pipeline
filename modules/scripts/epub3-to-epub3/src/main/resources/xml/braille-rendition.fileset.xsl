<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                version="2.0">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:param name="content-media-types" required="yes"/>
	
	<xsl:variable name="epub.in.fileset" select="collection()[2]"/>
	
	<xsl:variable name="_content-media-types" as="xs:string*" select="tokenize($content-media-types,'\s+')[not(.='')]"/>
	
	<xsl:template match="/">
		<d:fileset>
			<xsl:apply-templates select="//opf:manifest/opf:item"/>
		</d:fileset>
	</xsl:template>
	
	<xsl:template match="opf:manifest/opf:item">
		<xsl:variable name="default-href" select="resolve-uri(@href,pf:base-uri(.))"/>
		<xsl:variable name="original-href"
		              select="($epub.in.fileset//d:file[resolve-uri(@href,pf:base-uri(.))=$default-href]/@original-href
		                                       /resolve-uri(.,pf:base-uri(parent::*)),
		                       $default-href)[1]"/>
		<xsl:element name="d:file">
			<xsl:choose>
				<xsl:when test="@media-type=$_content-media-types">
					<xsl:attribute name="href" select="replace($default-href,'^(.+)\.x?html|(.+)$','$1$2_braille.xhtml')"/>
					<xsl:attribute name="default-href" select="$default-href"/>
				</xsl:when>
				<xsl:when test="@media-type='application/smil+xml'">
					<xsl:attribute name="href" select="replace($default-href,'^(.+)\.smil|(.+)$','$1$2_braille.smil')"/>
					<xsl:attribute name="default-href" select="$default-href"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="href" select="$default-href"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:attribute name="original-href" select="$original-href"/>
			<xsl:sequence select="@media-type"/>
		</xsl:element>
	</xsl:template>
	
</xsl:stylesheet>
