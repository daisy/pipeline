<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dotify="http://code.google.com/p/dotify/"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all">
	
	<xsl:import href="http://www.daisy.org/pipeline/modules/braille/css-utils/transform/block-translator-template.xsl"/>
	
	<xsl:param name="query"/>
	
	<xsl:template match="css:block" mode="#default before after">
		<xsl:value-of select="dotify:translate(concat($query,'(locale:',@xml:lang,')'), string(/*))"/>
	</xsl:template>
	
</xsl:stylesheet>
