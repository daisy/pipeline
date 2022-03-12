<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

	<xsl:include href="library.xsl"/>

	<xsl:param name="new-audio-file-type" required="yes"/>
	<xsl:param name="new-audio-dir" required="yes"/>
	<xsl:param name="temp-dir" required="yes"/>

	<xsl:template match="/d:fileset">
		<!--
		    transcode audio to files in $temp-dir
		-->
		<xsl:variable name="transcode" as="element(d:fileset)">
			<xsl:copy>
				<xsl:for-each select="d:file">
					<xsl:copy>
						<xsl:attribute name="href" select="pf:transcode-audio-file(
						                                     resolve-uri((@original-href,@href)[1],base-uri(.)),
						                                     @media-type,
						                                     $new-audio-file-type,
						                                     $temp-dir)"/>
						<xsl:attribute name="original-href" select="@href"/>
					</xsl:copy>
				</xsl:for-each>
			</xsl:copy>
		</xsl:variable>
		<!--
		    move audio files to $new-audio-dir
		-->
		<xsl:for-each select="$transcode">
			<xsl:copy>
				<xsl:for-each select="d:file">
					<xsl:copy>
						<xsl:attribute name="href" select="resolve-uri(replace(@href,'^.*/([^/]*)$','$1'),
						                                   $new-audio-dir)"/>
						<xsl:attribute name="original-href" select="@href"/>
						<xsl:attribute name="media-type" select="$new-audio-file-type"/>
					</xsl:copy>
				</xsl:for-each>
			</xsl:copy>
		</xsl:for-each>
		<!--
		    total mapping
		-->
		<xsl:result-document href="mapping">
			<xsl:for-each select="$transcode">
				<xsl:copy>
					<xsl:for-each select="d:file">
						<xsl:copy>
							<xsl:attribute name="href" select="resolve-uri(replace(@href,'^.*/([^/]*)$','$1'),
							                                               $new-audio-dir)"/>
							<xsl:attribute name="original-href" select="resolve-uri(@original-href,base-uri(.))"/>
						</xsl:copy>
					</xsl:for-each>
				</xsl:copy>
			</xsl:for-each>
		</xsl:result-document>
	</xsl:template>

</xsl:stylesheet>
