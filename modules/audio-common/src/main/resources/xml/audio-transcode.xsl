<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:pe="http://www.daisy.org/ns/pipeline/errors"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                xmlns:err="http://www.w3.org/2005/xqt-errors"
                exclude-result-prefixes="#all">

	<xsl:include href="library.xsl"/>
	<xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/library.xsl"/>

	<xsl:param name="new-audio-file-type" as="xs:string" required="yes"/>
	<xsl:param name="new-audio-dir" as="xs:anyURI" required="yes"/>
	<xsl:param name="temp-dir" as="xs:anyURI" required="yes"/>

	<xsl:template match="/d:fileset">
		<!--
		    transcode audio to files in $temp-dir
		-->
		<xsl:variable name="transcode" as="element(d:fileset)">
			<xsl:copy>
				<xsl:for-each select="d:file">
					<xsl:copy>
						<xsl:attribute name="original-href" select="@href"/>
						<xsl:try>
							<xsl:variable name="clip" as="map(*)" select="pf:transcode-audio-file(
							                                                resolve-uri((@original-href,@href)[1],base-uri(.)),
							                                                @media-type,
							                                                $new-audio-file-type,
							                                                $temp-dir)"/>
							<xsl:attribute name="href" select="$clip('href')"/>
							<xsl:if test="map:contains($clip,'clipBegin')">
								<xsl:element name="d:clip">
									<xsl:attribute name="clipBegin" select="$clip('clipBegin')"/>
									<xsl:attribute name="clipEnd" select="$clip('clipEnd')"/>
									<xsl:attribute name="original-clipBegin" select="$clip('original-clipBegin')"/>
									<xsl:attribute name="original-clipEnd" select="$clip('original-clipEnd')"/>
								</xsl:element>
							</xsl:if>
							<xsl:catch errors="pe:AUDIO001">
								<xsl:sequence select="pf:warn(concat('Not a recognized mime type: ',@media-type))"/>
							</xsl:catch>
						</xsl:try>
					</xsl:copy>
				</xsl:for-each>
			</xsl:copy>
		</xsl:variable>
		<!--
		    move audio files to $new-audio-dir
		-->
		<xsl:for-each select="$transcode">
			<xsl:copy>
				<xsl:for-each select="d:file[@href]">
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
					<xsl:for-each select="d:file[@href]">
						<xsl:copy>
							<xsl:attribute name="href" select="resolve-uri(replace(@href,'^.*/([^/]*)$','$1'),
							                                               $new-audio-dir)"/>
							<xsl:attribute name="original-href" select="resolve-uri(@original-href,base-uri(.))"/>
							<xsl:sequence select="d:clip"/>
						</xsl:copy>
					</xsl:for-each>
				</xsl:copy>
			</xsl:for-each>
		</xsl:result-document>
	</xsl:template>

</xsl:stylesheet>
