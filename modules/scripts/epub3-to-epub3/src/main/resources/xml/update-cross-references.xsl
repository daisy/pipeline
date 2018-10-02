<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:smil="http://www.w3.org/ns/SMIL"
                xmlns="http://www.w3.org/1999/xhtml"
                exclude-result-prefixes="#all"
                version="2.0">
	
	<xsl:include href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	
	<xsl:variable name="braille-rendition.fileset" select="collection()[2]"/>
	
	<xsl:variable name="base" as="xs:anyURI" select="base-uri(/*)"/>
	<xsl:variable name="original-base" as="xs:anyURI"
	              select="$braille-rendition.fileset//d:file[resolve-uri(@href,base-uri(.))=$base][1]
	                                                 /resolve-uri((@original-href,@href)[1],base-uri(.))"/>
	
	<xsl:template match="html:a/@href|
	                     html:img/src|
	                     html:link[@rel='next']/@href|
	                     smil:text/@src|
	                     smil:audio/@src">
		<xsl:variable name="absolute-href" as="xs:anyURI" select="resolve-uri(.,$original-base)"/>
		<xsl:variable name="tokens" as="xs:string*" select="pf:tokenize-uri($absolute-href)"/>
		<xsl:variable name="absolute-href-without-fragment" as="xs:string" select="pf:recompose-uri($tokens[position()&lt;5])"/>
		<xsl:variable name="fragment" as="xs:string" select="$tokens[5]"/>
		<xsl:variable name="new-href" as="xs:string?"
		              select="$braille-rendition.fileset
		                      //d:file[resolve-uri((@original-href,@href)[1],base-uri(.))=$absolute-href-without-fragment][1]
		                      /pf:relativize-uri(
		                         pf:recompose-uri((pf:tokenize-uri(resolve-uri(@href,base-uri(.)))[position()&lt;5],$fragment)),
		                         $base)"/>
		<xsl:choose>
			<xsl:when test="exists($new-href)">
				<xsl:attribute name="{name(.)}" select="$new-href"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:next-match/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	
</xsl:stylesheet>
