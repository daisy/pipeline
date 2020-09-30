<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all" >

	<xsl:template name="time-in-smil" as="xs:dayTimeDuration">
		<xsl:param name="smil" as="document-node(element(s:smil))" required="yes"/>
		<xsl:iterate select="$smil/descendant::*[@clipBegin and @clipEnd]">
			<xsl:param name="sum" as="xs:dayTimeDuration" select="xs:dayTimeDuration('PT0S')"/>
			<xsl:on-completion>
				<xsl:sequence select="$sum"/>
			</xsl:on-completion>
			<xsl:next-iteration>
				<!-- @clipBegin and @clipEnd are no actual xs:time, but they should be compliant with xs:time format. -->
				<xsl:with-param name="sum" select="$sum + (d:time(@clipEnd) - d:time(@clipBegin))"/>
			</xsl:next-iteration>
		</xsl:iterate>
	</xsl:template>

	<xsl:function name="d:time" as="xs:time">
		<xsl:param name="str" as="xs:string"/>
		<xsl:value-of select="xs:time(replace($str,'^([0-9]:)','0$1'))"/>
	</xsl:function>

</xsl:stylesheet>
