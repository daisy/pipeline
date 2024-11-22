<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:s="org.daisy.pipeline.braille.css.xpath.Style"
                xmlns:t="org.daisy.pipeline.braille.css.xpath.StyledText"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">

	<xsl:import href="abstract-block-translator.xsl"/>
	<xsl:include href="library.xsl"/>

	<xsl:param name="text-transform"/>

	<xsl:template match="css:block">
		<xsl:variable name="text" as="item()*">
			<xsl:apply-templates mode="text-items"/>
		</xsl:variable>
		<xsl:variable name="text" as="item()*" select="pf:text-transform($text-transform, $text)"/>
		<xsl:apply-templates select="node()[1]" mode="treewalk">
			<xsl:with-param name="new-text-nodes" select="$text"/>
		</xsl:apply-templates>
	</xsl:template>

</xsl:stylesheet>
