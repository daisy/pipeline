<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">

	<xsl:include href="http://www.daisy.org/pipeline/modules/smil-utils/clock-functions.xsl"/>

	<xsl:template name="main">
		<!-- Get the last SMIL file. -->
		<xsl:variable name="last-smil" as="document-node(element(s:smil))?" select="collection()[last()]"/>
		<total-time>
			<xsl:choose>
				<xsl:when test="exists($last-smil)">
					<!-- Assumes that it has the dtb:totalElapsedTime metadata. -->
					<xsl:variable name="time-elapsed" as="xs:decimal"
					              select="pf:smil-clock-value-to-seconds($last-smil/s:smil/s:head/s:meta[@name='dtb:totalElapsedTime']/@content)"/>
					<xsl:variable name="time-in-this-smil" as="xs:decimal" select="pf:smil-total-seconds($last-smil/*)"/>
					<xsl:value-of select="pf:smil-seconds-to-full-clock-value($time-elapsed + $time-in-this-smil)"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="pf:smil-seconds-to-full-clock-value(0)"/>
				</xsl:otherwise>
			</xsl:choose>
		</total-time>
	</xsl:template>

</xsl:stylesheet>
