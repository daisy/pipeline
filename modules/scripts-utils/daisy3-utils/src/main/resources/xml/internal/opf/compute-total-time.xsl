<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:s="http://www.w3.org/2001/SMIL20/"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

	<xsl:include href="../smils/compute-elapsed-time.xsl"/>

	<xsl:template name="main">
		<!-- Get the last SMIL file. -->
		<xsl:variable name="last-smil" as="document-node(element(s:smil))" select="collection()[last()]"/>
		<!-- Assumes that it has the dtb:totalElapsedTime metadata. -->
		<xsl:variable name="time-elapsed" as="xs:dayTimeDuration"
		              select="d:time($last-smil/s:smil/s:head/s:meta[@name='dtb:totalElapsedTime']/@content)
		                      - xs:time('00:00:00')"/>
		<xsl:variable name="time-in-smil" as="xs:dayTimeDuration">
			<xsl:call-template name="time-in-smil">
				<xsl:with-param name="smil" select="$last-smil"/>
			</xsl:call-template>
		</xsl:variable>
		<total-time>
			<xsl:value-of select="d:format-duration($time-elapsed + $time-in-smil)"/>
		</total-time>
	</xsl:template>

</xsl:stylesheet>
