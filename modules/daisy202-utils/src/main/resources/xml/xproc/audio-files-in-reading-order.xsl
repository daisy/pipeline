<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xpath-default-namespace=""
                exclude-result-prefixes="#all">

	<xsl:import href="http://www.daisy.org/pipeline/modules/file-utils/library.xsl"/>

	<xsl:template match="/">
		<xsl:variable name="audio-files" as="xs:anyURI*"
		              select="for $smil in collection() return
		                      for $src in $smil//body//audio/@src return
		                      resolve-uri(pf:normalize-uri($src),pf:base-uri($src/..))"/>
		<d:fileset>
			<xsl:for-each-group select="$audio-files" group-by="string(.)">
				<d:file href="{current()}"/>
			</xsl:for-each-group>
		</d:fileset>
	</xsl:template>

</xsl:stylesheet>
