<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions">
	
	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>
	
	<xsl:output method="xml" encoding="UTF-8" indent="yes" name="zip-manifest"/>
	
	<xsl:variable name="fileset.zip" select="collection()[1]"/>
	<xsl:variable name="fileset.in-memory" select="collection()[2]"/>
	
	<xsl:template match="/">
		<xsl:for-each select="distinct-values(//d:file/substring-before(resolve-uri(@href,base-uri(.)),'!/'))">
			<xsl:variable name="href" select="."/>
			<xsl:result-document href="manifest.xml" format="zip-manifest">
				<xsl:element name="c:zip-manifest">
					<xsl:attribute name="xml:base" select="base-uri($fileset.zip/*)"/>
					<xsl:attribute name="href" select="$href"/>
					<xsl:apply-templates select="$fileset.zip//d:file[substring-before(resolve-uri(@href,base-uri(.)),'!/')=$href]"/>
				</xsl:element>
			</xsl:result-document>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:template match="d:file">
		<xsl:element name="c:entry">
			<xsl:variable name="target" select="resolve-uri(@href, base-uri(.))"/>
			<xsl:attribute name="name" select="pf:unescape-uri(substring-after($target,'!/'))"/>
			<xsl:choose>
				<xsl:when test="$fileset.in-memory//d:file[resolve-uri(@href,base-uri(.))=$target]">
					<xsl:attribute name="href" select="@href"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:attribute name="href" select="(@original-href,@href)[1]"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:sequence select="@compression-method|       @encoding|                @normalization-form|
			                      @compression-level|        @escape-uri-attributes|   @omit-xml-declaration|
			                      @byte-order-mark|          @include-content-type|    @standalone|
			                      @cdata-section-elements|   @indent|                  @undeclare-prefixes|
			                      @doctype-public|           @media-type|              @version|
			                      @doctype-system|           @method"/>
		</xsl:element>
	</xsl:template>
	
</xsl:stylesheet>
