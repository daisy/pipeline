<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                version="2.0">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	
	<xsl:param name="epub-base" required="yes" as="xs:string"/>
	<xsl:param name="ebraille-compatibility" required="yes" as="xs:string?"/>
	<xsl:param name="content-media-types" required="yes" as="xs:string*"/>
	
	<xsl:variable name="original-base" as="xs:string"
	              select="pf:longest-common-uri(//d:file/resolve-uri(@href,base-uri(.)))"/>

	<!--
	    FIXME: assuming that the input does not have a "braille" folder yet
	    (note that for eBraille output, the original default rendition is moved to another folder,
	    so there can be no conflict as long as the input has only one rendition)
	-->
	<xsl:variable name="braille-base" as="xs:string"
	              select="if ($ebraille-compatibility)
	                      then resolve-uri('ebraille/',$epub-base)
	                      else resolve-uri('braille/',$epub-base)"/>

	<xsl:template match="/">
		<d:fileset>
			<xsl:apply-templates select="//d:file"/>
		</d:fileset>
	</xsl:template>
	
	<xsl:template match="d:fileset/d:file">
		<xsl:choose>
			<xsl:when test="@media-type=($content-media-types,'application/smil+xml','application/x-dtbncx+xml')">
				<xsl:variable name="original-href" select="resolve-uri(@href,pf:base-uri(.))"/>
				<xsl:variable name="braille-href" select="if ($ebraille-compatibility and @role='nav')
				                                          then resolve-uri('index.xhtml',$epub-base)
				                                          else resolve-uri(pf:relativize-uri($original-href,$original-base),
				                                                           $braille-base)"/>
				<xsl:element name="d:file">
					<xsl:attribute name="href" select="$braille-href"/>
					<xsl:attribute name="original-href" select="$original-href"/>
				</xsl:element>
			</xsl:when>
			<xsl:when test="@media-type='application/oebps-package+xml'">
				<xsl:variable name="original-href" select="resolve-uri(@href,pf:base-uri(.))"/>
				<xsl:variable name="braille-href" select="if ($ebraille-compatibility)
				                                          then resolve-uri('package.opf',$epub-base)
				                                          else resolve-uri('package.opf',$braille-base)"/>
				<xsl:element name="d:file">
					<xsl:attribute name="href" select="$braille-href"/>
					<xsl:attribute name="original-href" select="$original-href"/>
				</xsl:element>
			</xsl:when>
		</xsl:choose>
	</xsl:template>
	
</xsl:stylesheet>
