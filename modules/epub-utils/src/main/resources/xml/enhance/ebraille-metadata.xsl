<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dc="http://purl.org/dc/elements/1.1/"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:opf="http://www.idpf.org/2007/opf"
                xmlns="http://www.idpf.org/2007/opf"
                xpath-default-namespace="http://www.idpf.org/2007/opf"
                exclude-result-prefixes="#all">

	<xsl:param name="brailleCellType" as="xs:string"/>
	<xsl:param name="brailleSystem" as="xs:string*"/>
	<xsl:param name="ebraille-compatibility" as="xs:string?"/> <!-- soft|strict -->

	<xsl:template match="/">
		<metadata>
			<!-- ==========================
			     required eBraille metadata
			     ========================== -->
			<!-- dcterms:modified will be generated/updated by px:epub3-add-metadata -->
			<!-- generate braille code metadata -->
			<xsl:if test="$ebraille-compatibility">
				<meta property="a11y:brailleCellType">
					<xsl:value-of select="$brailleCellType"/>
				</meta>
				<xsl:for-each select="$brailleSystem">
					<meta property="a11y:brailleSystem">
						<!-- note that there are currently no registered braille codes (see
						     https://daisy.github.io/ebraille/published/registries/codes/), but since
						     a11y:brailleSystem is required, we need to put some value -->
						<xsl:value-of select="."/>
					</meta>
				</xsl:for-each>
			</xsl:if>
		</metadata>
	</xsl:template>

</xsl:stylesheet>
